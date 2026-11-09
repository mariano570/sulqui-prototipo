package com.sulqui.dao;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Patron DAO (Data Access Object): contrato comun de acceso a datos para
 * cualquier entidad del dominio.
 *
 * Los servicios (capa de negocio) dependen de esta interfaz y no de JDBC ni
 * de SQL: si manana la persistencia cambiara (otro motor, un archivo, un
 * servicio remoto) bastaria con otra implementacion de Dao<T>.
 *
 * Todas las operaciones informan los errores de la base con
 * {@link PersistenciaException}, una especializacion de SQLException que
 * clasifica la causa (conexion, duplicado, referencia, restriccion...).
 *
 * @param <T> entidad que persiste el DAO (Cliente, Reserva, MenuPaquete...)
 */
public interface Dao<T> {

    /** Inserta la entidad y devuelve el identificador generado por la base. */
    int insertar(T entidad) throws SQLException;

    /** Busca por clave primaria; devuelve null si no existe. */
    T buscarPorId(int id) throws SQLException;

    /** Devuelve todos los registros en un ArrayList (cantidad desconocida de antemano). */
    ArrayList<T> listarTodos() throws SQLException;

    /** Actualiza la entidad. Las tablas que no admiten modificacion no lo redefinen. */
    default void actualizar(T entidad) throws SQLException {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " no permite actualizar registros.");
    }

    /** Elimina por clave primaria; devuelve true si existia. */
    boolean eliminar(int id) throws SQLException;

    /** Cantidad de registros de la tabla. */
    int contar() throws SQLException;
}
