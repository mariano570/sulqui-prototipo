package com.sulqui;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * Crea desde cero la base de prueba "sulqui_test" con el mismo script de
 * produccion (sql/01_sulqui_schema.sql) y carga un conjunto de datos minimo
 * y conocido, para que cada resultado esperado sea verificable.
 *
 * Datos de partida (fixture):
 *   administrador #1 'mariano'
 *   cliente #1 Ana Gomez (ana.gomez@example.com)
 *   menu #1 'Asado clasico' a $8.500 por persona
 *   reserva #1 CONFIRMADA para el 13/03/2027 (cliente 1, menu 1, 25 comensales)
 */
public final class BaseDatosPrueba {

    public static final String URL_SERVIDOR = System.getProperty("test.servidor", "jdbc:mysql://localhost:3306/");
    public static final String USUARIO = System.getProperty("db.usuario", "root");
    public static final String CLAVE = System.getProperty("db.contrasenia", "");

    private BaseDatosPrueba() { }

    public static void reiniciar() throws Exception {
        String script = Files.readString(Path.of("sql", "01_sulqui_schema.sql"))
                .replace("sulqui_db", "sulqui_test");
        try (Connection con = DriverManager.getConnection(URL_SERVIDOR, USUARIO, CLAVE);
             Statement st = con.createStatement()) {
            for (String sentencia : sinComentarios(script).split(";")) {
                if (!sentencia.isBlank()) st.execute(sentencia);
            }
            st.execute("INSERT INTO administrador (usuario, contrasenia) VALUES ('mariano', SHA2('sulqui2026', 256))");
            st.execute("INSERT INTO cliente (nombre, telefono, email) VALUES ('Ana Gomez', '3517001111', 'ana.gomez@example.com')");
            st.execute("INSERT INTO menu_paquete (nombre, precio_persona, tipo_preparacion) VALUES ('Asado clasico', 8500.00, 'Asado')");
            st.execute("INSERT INTO reserva (id_cliente, id_admin, id_menu, fecha, cant_comensales, estado) "
                    + "VALUES (1, 1, 1, '2027-03-13', 25, 'Confirmada')");
        }
        // Los DAO de la aplicacion apuntan a la base de prueba
        System.setProperty("db.url", URL_SERVIDOR + "sulqui_test");
        System.setProperty("db.usuario", USUARIO);
        System.setProperty("db.contrasenia", CLAVE);
    }

    /** Base de prueba con los datos de ejemplo del repositorio (sql/02_sulqui_datos_ejemplo.sql). */
    public static void reiniciarConDatosDeEjemplo() throws Exception {
        String script = Files.readString(Path.of("sql", "01_sulqui_schema.sql"))
                + "\n" + Files.readString(Path.of("sql", "02_sulqui_datos_ejemplo.sql"));
        script = script.replace("sulqui_db", "sulqui_test");
        try (Connection con = DriverManager.getConnection(URL_SERVIDOR, USUARIO, CLAVE);
             Statement st = con.createStatement()) {
            for (String sentencia : sinComentarios(script).split(";")) {
                if (!sentencia.isBlank()) st.execute(sentencia);
            }
        }
        System.setProperty("db.url", URL_SERVIDOR + "sulqui_test");
        System.setProperty("db.usuario", USUARIO);
        System.setProperty("db.contrasenia", CLAVE);
    }

    private static String sinComentarios(String sql) {
        StringBuilder sb = new StringBuilder();
        for (String linea : sql.split("\n")) {
            String l = linea.contains("--") ? linea.substring(0, linea.indexOf("--")) : linea;
            sb.append(l).append('\n');
        }
        return sb.toString();
    }
}
