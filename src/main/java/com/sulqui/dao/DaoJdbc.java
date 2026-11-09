package com.sulqui.dao;

import java.sql.*;
import java.util.ArrayList;

/**
 * Implementacion base del patron DAO con JDBC (clase abstracta).
 *
 * Reune el codigo que todos los DAO repetian: pedir la conexion al
 * Singleton, preparar la sentencia, cargar los parametros, recorrer el
 * ResultSet, cerrar los recursos y traducir los errores. Cada subclase solo
 * indica su tabla, su clave primaria y como convertir una fila en objeto
 * (metodo plantilla {@link #mapear(ResultSet)}).
 *
 * Los metodos que reciben una Connection permiten participar de una
 * transaccion abierta con {@link Conexion#enTransaccion}.
 */
public abstract class DaoJdbc<T> implements Dao<T> {

    /** Carga los parametros (?) de una sentencia preparada. */
    @FunctionalInterface
    protected interface Parametros {
        void cargar(PreparedStatement ps) throws SQLException;
        Parametros NINGUNO = ps -> { };
    }

    protected abstract String tabla();

    protected abstract String columnaId();

    /** Convierte la fila actual del ResultSet en un objeto del dominio. */
    protected abstract T mapear(ResultSet rs) throws SQLException;

    protected Connection conexion() throws SQLException {
        return Conexion.getInstancia().obtener();
    }

    // ------------------------------------------------ operaciones genericas del contrato Dao<T>

    @Override
    public T buscarPorId(int id) throws SQLException {
        return consultarUno("SELECT * FROM " + tabla() + " WHERE " + columnaId() + " = ?", ps -> ps.setInt(1, id));
    }

    @Override
    public ArrayList<T> listarTodos() throws SQLException {
        return consultarLista("SELECT * FROM " + tabla() + " ORDER BY " + columnaId(), Parametros.NINGUNO);
    }

    @Override
    public boolean eliminar(int id) throws SQLException {
        return ejecutar("eliminar el registro de " + tabla(),
                "DELETE FROM " + tabla() + " WHERE " + columnaId() + " = ?", ps -> ps.setInt(1, id)) > 0;
    }

    @Override
    public int contar() throws SQLException {
        return contar("SELECT COUNT(*) FROM " + tabla(), Parametros.NINGUNO);
    }

    // ------------------------------------------------ metodos reutilizables por las subclases

    /** SELECT que devuelve a lo sumo un registro. */
    protected T consultarUno(String sql, Parametros parametros) throws SQLException {
        ArrayList<T> lista = consultarLista(sql, parametros);
        return lista.isEmpty() ? null : lista.get(0);
    }

    /** SELECT que devuelve una cantidad variable de registros: se acumulan en un ArrayList. */
    protected ArrayList<T> consultarLista(String sql, Parametros parametros) throws SQLException {
        ArrayList<T> lista = new ArrayList<>();
        try (Connection con = conexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            parametros.cargar(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw PersistenciaException.traducir(e, "consultar " + tabla());
        }
        return lista;
    }

    /** SELECT COUNT(*) u otra consulta que devuelve un entero. */
    protected int contar(String sql, Parametros parametros) throws SQLException {
        try (Connection con = conexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            parametros.cargar(ps);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw PersistenciaException.traducir(e, "consultar " + tabla());
        }
    }

    /** INSERT que devuelve la clave generada (AUTO_INCREMENT). */
    protected int insertarConClave(String operacion, String sql, Parametros parametros) throws SQLException {
        try (Connection con = conexion()) {
            return insertarConClave(con, operacion, sql, parametros);
        } catch (SQLException e) {
            throw PersistenciaException.traducir(e, operacion);
        }
    }

    /** INSERT dentro de una transaccion: usa la conexion recibida y no la cierra. */
    protected int insertarConClave(Connection con, String operacion, String sql, Parametros parametros)
            throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            parametros.cargar(ps);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        } catch (SQLException e) {
            throw PersistenciaException.traducir(e, operacion);
        }
    }

    /** UPDATE o DELETE; devuelve la cantidad de filas afectadas. */
    protected int ejecutar(String operacion, String sql, Parametros parametros) throws SQLException {
        try (Connection con = conexion()) {
            return ejecutar(con, operacion, sql, parametros);
        } catch (SQLException e) {
            throw PersistenciaException.traducir(e, operacion);
        }
    }

    /** UPDATE o DELETE dentro de una transaccion. */
    protected int ejecutar(Connection con, String operacion, String sql, Parametros parametros) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            parametros.cargar(ps);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw PersistenciaException.traducir(e, operacion);
        }
    }
}
