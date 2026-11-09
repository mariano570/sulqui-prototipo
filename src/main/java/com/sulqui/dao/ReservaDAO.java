package com.sulqui.dao;

import com.sulqui.modelo.EstadoReserva;
import com.sulqui.modelo.Reserva;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;

/** Acceso a datos (CRUD) para la entidad Reserva (tabla reserva). */
public class ReservaDAO extends DaoJdbc<Reserva> {

    @Override protected String tabla() { return "reserva"; }

    @Override protected String columnaId() { return "id_reserva"; }

    @Override
    public int insertar(Reserva r) throws SQLException {
        return insertarConClave("registrar la reserva",
                "INSERT INTO reserva (id_cliente, id_menu, fecha, cant_comensales, estado) VALUES (?, ?, ?, ?, ?)",
                ps -> {
                    ps.setInt(1, r.getIdCliente());
                    ps.setInt(2, r.getIdMenu());
                    ps.setDate(3, Date.valueOf(r.getFecha()));
                    ps.setInt(4, r.getCantComensales());
                    ps.setString(5, r.getEstado().getValorBD());
                });
    }

    /** RF-01: indica si existe una reserva confirmada para la fecha indicada. */
    public boolean existeReservaConfirmada(LocalDate fecha) throws SQLException {
        return contar("SELECT COUNT(*) FROM reserva WHERE fecha = ? AND estado = 'Confirmada'",
                ps -> ps.setDate(1, Date.valueOf(fecha))) > 0;
    }

    public ArrayList<Reserva> listarPorEstado(EstadoReserva estado) throws SQLException {
        return consultarLista("SELECT * FROM reserva WHERE estado = ? ORDER BY fecha",
                ps -> ps.setString(1, estado.getValorBD()));
    }

    /** Pendientes en orden de llegada (para cargar la cola FIFO de atencion). */
    public ArrayList<Reserva> listarPendientesPorLlegada() throws SQLException {
        return consultarLista("SELECT * FROM reserva WHERE estado = 'Pendiente' ORDER BY fecha_solicitud, id_reserva",
                Parametros.NINGUNO);
    }

    /** RF-06: historial de reservas de un cliente. */
    public ArrayList<Reserva> listarPorCliente(int idCliente) throws SQLException {
        return consultarLista("SELECT * FROM reserva WHERE id_cliente = ? ORDER BY fecha DESC",
                ps -> ps.setInt(1, idCliente));
    }

    /** RF-08: reservas confirmadas entre dos fechas (calendario y reportes). */
    public ArrayList<Reserva> listarConfirmadasEntre(LocalDate desde, LocalDate hasta) throws SQLException {
        return consultarLista("SELECT * FROM reserva WHERE estado = 'Confirmada' AND fecha BETWEEN ? AND ? ORDER BY fecha",
                ps -> {
                    ps.setDate(1, Date.valueOf(desde));
                    ps.setDate(2, Date.valueOf(hasta));
                });
    }

    /** RF-03: cambia el estado de una reserva y registra al administrador que la gestiono. */
    public void actualizarEstado(int idReserva, int idAdmin, EstadoReserva nuevoEstado) throws SQLException {
        try (Connection con = conexion()) {
            actualizarEstado(con, idReserva, idAdmin, nuevoEstado);
        }
    }

    /** Version transaccional: usa la conexion de la transaccion en curso. */
    public void actualizarEstado(Connection con, int idReserva, int idAdmin, EstadoReserva nuevoEstado)
            throws SQLException {
        ejecutar(con, "actualizar el estado de la reserva",
                "UPDATE reserva SET id_admin = ?, estado = ? WHERE id_reserva = ?", ps -> {
                    ps.setInt(1, idAdmin);
                    ps.setString(2, nuevoEstado.getValorBD());
                    ps.setInt(3, idReserva);
                });
    }

    @Override
    protected Reserva mapear(ResultSet rs) throws SQLException {
        Reserva r = new Reserva();
        r.setIdReserva(rs.getInt("id_reserva"));
        r.setIdCliente(rs.getInt("id_cliente"));
        int idAdmin = rs.getInt("id_admin");
        r.setIdAdmin(rs.wasNull() ? null : idAdmin);
        r.setIdMenu(rs.getInt("id_menu"));
        r.setFecha(rs.getDate("fecha").toLocalDate());
        r.setCantComensales(rs.getInt("cant_comensales"));
        r.setEstado(EstadoReserva.desdeBD(rs.getString("estado")));
        Timestamp ts = rs.getTimestamp("fecha_solicitud");
        if (ts != null) r.setFechaSolicitud(ts.toLocalDateTime());
        return r;
    }
}
