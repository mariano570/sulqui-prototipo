package com.sulqui.dao;

import com.sulqui.modelo.Administrador;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Acceso a datos para la tabla administrador. Las cuentas se crean desde el
 * script SQL (con el hash de la contrasenia), por eso insertar no esta disponible
 * desde la aplicacion.
 */
public class AdministradorDAO extends DaoJdbc<Administrador> {

    @Override protected String tabla() { return "administrador"; }

    @Override protected String columnaId() { return "id_admin"; }

    @Override
    public int insertar(Administrador a) {
        throw new UnsupportedOperationException("Los administradores se crean desde sql/02_sulqui_datos_ejemplo.sql.");
    }

    public Administrador buscarPorUsuario(String usuario) throws SQLException {
        return consultarUno("SELECT * FROM administrador WHERE usuario = ?", ps -> ps.setString(1, usuario));
    }

    @Override
    protected Administrador mapear(ResultSet rs) throws SQLException {
        return new Administrador(rs.getInt("id_admin"), rs.getString("usuario"), rs.getString("contrasenia"));
    }
}
