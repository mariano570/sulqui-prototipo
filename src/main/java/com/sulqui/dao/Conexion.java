package com.sulqui.dao;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Punto unico de acceso a la base de datos MySQL "sulqui_db" (patron Singleton).
 *
 * Existe una sola instancia en toda la aplicacion: lee la configuracion una
 * vez y la comparten todos los DAO. No guarda una unica Connection abierta:
 * entrega una conexion nueva por operacion, que el DAO cierra con
 * try-with-resources. Asi una conexion caida no deja inutilizada la
 * aplicacion y las ventanas Swing no comparten un objeto que no es seguro
 * entre hilos.
 *
 * Los datos de conexion se leen, en este orden de prioridad, de:
 *   1) propiedades del sistema (-Ddb.url, -Ddb.usuario, -Ddb.contrasenia),
 *   2) el archivo config/sulqui.properties (ver config/sulqui.properties.example),
 *   3) los valores por defecto de esta clase.
 */
public final class Conexion {

    private static final String URL_DEFECTO =
            "jdbc:mysql://localhost:3306/sulqui_db?serverTimezone=America/Argentina/Cordoba";

    private static volatile Conexion instancia;      // unica instancia (inicializacion diferida)

    private final Properties config;

    /** Constructor privado: nadie fuera de la clase puede crear otra instancia. */
    private Conexion() {
        this.config = cargarConfiguracion();
    }

    /** Devuelve la unica instancia; el doble chequeo la hace segura entre hilos. */
    public static Conexion getInstancia() {
        if (instancia == null) {
            synchronized (Conexion.class) {
                if (instancia == null) instancia = new Conexion();
            }
        }
        return instancia;
    }

    /** Abre una conexion nueva; quien la pide la cierra (try-with-resources). */
    public Connection obtener() throws SQLException {
        try {
            return DriverManager.getConnection(valor("db.url", URL_DEFECTO),
                    valor("db.usuario", "sulqui_app"), valor("db.contrasenia", ""));
        } catch (SQLException e) {
            throw PersistenciaException.traducir(e, "conectar con la base de datos");
        }
    }

    /** Trabajo que se ejecuta dentro de una transaccion, con la conexion que esta la controla. */
    @FunctionalInterface
    public interface Trabajo<R> {
        R ejecutar(Connection con) throws SQLException;
    }

    /**
     * Ejecuta varias sentencias como una unidad: si todas terminan bien se
     * confirman (commit); si alguna falla se deshacen todas (rollback) y el
     * error se informa como PersistenciaException.
     */
    public <R> R enTransaccion(String operacion, Trabajo<R> trabajo) throws SQLException {
        try (Connection con = obtener()) {
            con.setAutoCommit(false);
            try {
                R resultado = trabajo.ejecutar(con);
                con.commit();
                return resultado;
            } catch (SQLException | RuntimeException e) {
                con.rollback();                              // nada queda a medio grabar
                if (e instanceof SQLException sql) throw PersistenciaException.traducir(sql, operacion);
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    /** URL efectiva (sin credenciales), para mostrar en mensajes y diagnosticos. */
    public String getUrl() {
        return valor("db.url", URL_DEFECTO);
    }

    private String valor(String clave, String defecto) {
        String sistema = System.getProperty(clave);
        if (sistema != null) return sistema;
        return config.getProperty(clave, defecto);
    }

    private static Properties cargarConfiguracion() {
        Properties p = new Properties();
        Path archivo = Path.of("config", "sulqui.properties");
        if (Files.exists(archivo)) {
            try (InputStream in = Files.newInputStream(archivo)) {
                p.load(in);
            } catch (IOException e) {
                System.err.println("No se pudo leer " + archivo + ": " + e.getMessage());
            }
        }
        return p;
    }
}
