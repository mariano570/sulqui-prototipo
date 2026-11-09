package com.sulqui.dao;

import com.sulqui.modelo.MenuPaquete;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/** Acceso a datos para el catalogo de menus (tabla menu_paquete). */
public class MenuDAO extends DaoJdbc<MenuPaquete> {

    private static final String INSERT =
            "INSERT INTO menu_paquete (nombre, precio_persona, tipo_preparacion) VALUES (?, ?, ?)";
    private static final String UPDATE =
            "UPDATE menu_paquete SET nombre = ?, precio_persona = ?, tipo_preparacion = ? WHERE id_menu = ?";

    @Override protected String tabla() { return "menu_paquete"; }

    @Override protected String columnaId() { return "id_menu"; }

    @Override
    public int insertar(MenuPaquete m) throws SQLException {
        try (Connection con = conexion()) {
            return insertar(con, m);
        }
    }

    /** Version transaccional (restauracion del catalogo desde un archivo de respaldo). */
    public int insertar(Connection con, MenuPaquete m) throws SQLException {
        return insertarConClave(con, "agregar el menu \"" + m.getNombre() + "\"", INSERT, ps -> {
            ps.setString(1, m.getNombre());
            ps.setBigDecimal(2, m.getPrecioPorPersona());
            ps.setString(3, m.getTipoPreparacion());
        });
    }

    /** El catalogo se muestra por nombre; el orden por precio lo aplica MenuService. */
    @Override
    public ArrayList<MenuPaquete> listarTodos() throws SQLException {
        return consultarLista("SELECT * FROM menu_paquete ORDER BY nombre", Parametros.NINGUNO);
    }

    @Override
    public void actualizar(MenuPaquete m) throws SQLException {
        try (Connection con = conexion()) {
            actualizar(con, m);
        }
    }

    /** Version transaccional; devuelve true si el menu existia. */
    public boolean actualizar(Connection con, MenuPaquete m) throws SQLException {
        return ejecutar(con, "actualizar el menu \"" + m.getNombre() + "\"", UPDATE, ps -> {
            ps.setString(1, m.getNombre());
            ps.setBigDecimal(2, m.getPrecioPorPersona());
            ps.setString(3, m.getTipoPreparacion());
            ps.setInt(4, m.getIdMenu());
        }) > 0;
    }

    @Override
    protected MenuPaquete mapear(ResultSet rs) throws SQLException {
        return new MenuPaquete(rs.getInt("id_menu"), rs.getString("nombre"),
                rs.getBigDecimal("precio_persona"), rs.getString("tipo_preparacion"));
    }
}
