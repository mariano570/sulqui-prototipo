package com.sulqui.dao;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLNonTransientConnectionException;
import java.sql.SQLTransientConnectionException;

/**
 * Error de la base de datos traducido a un mensaje que el usuario entiende.
 *
 * Extiende SQLException (excepcion verificada) para respetar el contrato de
 * JDBC: quien ya atrapaba SQLException la sigue atrapando, y quien necesita
 * mas detalle consulta {@link #getTipo()}. La causa original se conserva
 * (getCause) para el registro tecnico.
 *
 * La clasificacion usa el SQLSTATE estandar y los codigos de error de MySQL
 * (y de MariaDB, que es compatible).
 */
public class PersistenciaException extends SQLException {

    private static final long serialVersionUID = 1L;

    /** Causa del error, independiente del motor. */
    public enum Tipo {
        CONEXION("Sin conexion con la base de datos"),
        DUPLICADO("Registro duplicado"),
        REFERENCIA("Registro relacionado"),
        RESTRICCION("Restriccion de la base"),
        DATO_INVALIDO("Dato no admitido por la base"),
        OTRO("Error de base de datos");

        private final String titulo;
        Tipo(String titulo) { this.titulo = titulo; }
        public String getTitulo() { return titulo; }
    }

    private final Tipo tipo;
    private final String operacion;

    public PersistenciaException(Tipo tipo, String operacion, String detalle, SQLException causa) {
        super("No se pudo " + operacion + ": " + detalle,
                causa == null ? null : causa.getSQLState(),
                causa == null ? 0 : causa.getErrorCode(), causa);
        this.tipo = tipo;
        this.operacion = operacion;
    }

    public Tipo getTipo() { return tipo; }

    public String getOperacion() { return operacion; }

    public String getTitulo() { return tipo.getTitulo(); }

    /**
     * Convierte una SQLException de JDBC en una PersistenciaException.
     * Si ya fue traducida (por ejemplo, dentro de una transaccion) se devuelve tal cual.
     */
    public static PersistenciaException traducir(SQLException e, String operacion) {
        if (e instanceof PersistenciaException p) return p;
        String estado = e.getSQLState() == null ? "" : e.getSQLState();
        int codigo = e.getErrorCode();

        if (e instanceof SQLNonTransientConnectionException || e instanceof SQLTransientConnectionException
                || estado.startsWith("08") || codigo == 1045 || codigo == 1049) {
            return new PersistenciaException(Tipo.CONEXION, operacion,
                    "verifique que el servidor MySQL este encendido y los datos de config/sulqui.properties.", e);
        }
        if (codigo == 1062) {
            return new PersistenciaException(Tipo.DUPLICADO, operacion,
                    "ya existe un registro con ese valor unico (por ejemplo, el email).", e);
        }
        if (codigo == 1451 || codigo == 1452) {
            return new PersistenciaException(Tipo.REFERENCIA, operacion,
                    "el registro esta relacionado con otros datos (clave foranea).", e);
        }
        if (codigo == 3819 || codigo == 4025) {          // CHECK: 3819 en MySQL 8, 4025 en MariaDB
            return new PersistenciaException(Tipo.RESTRICCION, operacion,
                    "el dato no cumple una regla de la base (" + nombreRestriccion(e.getMessage()) + ").", e);
        }
        if (codigo == 1406 || codigo == 1264 || codigo == 1048 || codigo == 1366 || estado.startsWith("22")) {
            return new PersistenciaException(Tipo.DATO_INVALIDO, operacion,
                    "un dato es demasiado largo, esta vacio o tiene un formato que la base no admite.", e);
        }
        if (e instanceof SQLIntegrityConstraintViolationException || estado.startsWith("23")) {
            return new PersistenciaException(Tipo.RESTRICCION, operacion,
                    "se viola una restriccion de integridad.", e);
        }
        return new PersistenciaException(Tipo.OTRO, operacion, e.getMessage(), e);
    }

    /** Extrae el nombre de la restriccion CHECK del mensaje del motor, si lo trae. */
    private static String nombreRestriccion(String mensaje) {
        if (mensaje == null) return "CHECK";
        int i = mensaje.indexOf('`');
        int j = i < 0 ? -1 : mensaje.indexOf('`', i + 1);
        return (i >= 0 && j > i) ? mensaje.substring(i + 1, j) : "CHECK";
    }
}
