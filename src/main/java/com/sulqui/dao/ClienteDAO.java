package com.sulqui.dao;

import com.sulqui.modelo.Cliente;

import java.sql.ResultSet;
import java.sql.SQLException;

/** Acceso a datos (CRUD) para la entidad Cliente (tabla cliente). */
public class ClienteDAO extends DaoJdbc<Cliente> {

    @Override protected String tabla() { return "cliente"; }

    @Override protected String columnaId() { return "id_cliente"; }

    @Override
    public int insertar(Cliente c) throws SQLException {
        return insertarConClave("registrar el cliente",
                "INSERT INTO cliente (nombre, telefono, email) VALUES (?, ?, ?)", ps -> {
                    ps.setString(1, c.getNombre());
                    ps.setString(2, c.getTelefono());
                    ps.setString(3, c.getEmail());
                });
    }

    public Cliente buscarPorEmail(String email) throws SQLException {
        return consultarUno("SELECT * FROM cliente WHERE email = ?", ps -> ps.setString(1, email));
    }

    public boolean tieneReservas(int idCliente) throws SQLException {
        return contar("SELECT COUNT(*) FROM reserva WHERE id_cliente = ?", ps -> ps.setInt(1, idCliente)) > 0;
    }

    @Override
    public void actualizar(Cliente c) throws SQLException {
        ejecutar("actualizar el cliente",
                "UPDATE cliente SET nombre = ?, telefono = ?, email = ? WHERE id_cliente = ?", ps -> {
                    ps.setString(1, c.getNombre());
                    ps.setString(2, c.getTelefono());
                    ps.setString(3, c.getEmail());
                    ps.setInt(4, c.getIdCliente());
                });
    }

    @Override
    protected Cliente mapear(ResultSet rs) throws SQLException {
        return new Cliente(rs.getInt("id_cliente"), rs.getString("nombre"),
                rs.getString("telefono"), rs.getString("email"));
    }
}
