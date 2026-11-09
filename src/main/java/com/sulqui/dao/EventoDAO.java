package com.sulqui.dao;

import com.sulqui.modelo.Evento;

import java.sql.*;

/** Acceso a datos para la tabla evento (lugar y horario de una reserva confirmada). */
public class EventoDAO extends DaoJdbc<Evento> {

    @Override protected String tabla() { return "evento"; }

    @Override protected String columnaId() { return "id_evento"; }

    @Override
    public int insertar(Evento e) throws SQLException {
        try (Connection con = conexion()) {
            return insertar(con, e);
        }
    }

    /** Version transaccional: se usa al confirmar la reserva (misma transaccion). */
    public int insertar(Connection con, Evento e) throws SQLException {
        return insertarConClave(con, "registrar el evento",
                "INSERT INTO evento (id_reserva, fecha, direccion, horario) VALUES (?, ?, ?, ?)", ps -> {
                    ps.setInt(1, e.getIdReserva());
                    ps.setDate(2, Date.valueOf(e.getFecha()));
                    ps.setString(3, e.getDireccion());
                    ps.setTime(4, Time.valueOf(e.getHorario()));
                });
    }

    public Evento buscarPorReserva(int idReserva) throws SQLException {
        return consultarUno("SELECT * FROM evento WHERE id_reserva = ?", ps -> ps.setInt(1, idReserva));
    }

    @Override
    protected Evento mapear(ResultSet rs) throws SQLException {
        Evento e = new Evento(rs.getInt("id_reserva"), rs.getDate("fecha").toLocalDate(),
                rs.getString("direccion"), rs.getTime("horario").toLocalTime());
        e.setIdEvento(rs.getInt("id_evento"));
        return e;
    }
}
