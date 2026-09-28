package com.sulqui.dao;

import com.sulqui.modelo.Reserva;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Acceso a datos (CRUD) para la entidad Reserva. */
public class ReservaDAO {

    public int insertar(Reserva r) throws SQLException {
        String sql = "INSERT INTO reserva (id_cliente, id_menu, fecha, cant_comensales, estado) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, r.getIdCliente());
            ps.setInt(2, r.getIdMenu());
            ps.setDate(3, Date.valueOf(r.getFecha()));
            ps.setInt(4, r.getCantComensales());
            ps.setString(5, r.getEstado());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    /** RF-01: consulta si existe una reserva confirmada para la fecha indicada. */
    public boolean existeReservaConfirmada(LocalDate fecha) throws SQLException {
        String sql = "SELECT COUNT(*) FROM reserva WHERE fecha = ? AND estado = 'Confirmada'";
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(fecha));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public List<Reserva> listarPorEstado(String estado) throws SQLException {
        List<Reserva> lista = new ArrayList<>();
        String sql = "SELECT * FROM reserva WHERE estado = ? ORDER BY fecha";
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, estado);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    /** RF-03: cambia el estado de una reserva (Confirmada / Rechazada) y asigna al administrador. */
    public void actualizarEstado(int idReserva, int idAdmin, String nuevoEstado) throws SQLException {
        String sql = "UPDATE reserva SET id_admin = ?, estado = ? WHERE id_reserva = ?";
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idAdmin);
            ps.setString(2, nuevoEstado);
            ps.setInt(3, idReserva);
            ps.executeUpdate();
        }
    }

    public void eliminar(int idReserva) throws SQLException {
        String sql = "DELETE FROM reserva WHERE id_reserva = ?";
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idReserva);
            ps.executeUpdate();
        }
    }

    private Reserva mapear(ResultSet rs) throws SQLException {
        Reserva r = new Reserva();
        r.setIdReserva(rs.getInt("id_reserva"));
        r.setIdCliente(rs.getInt("id_cliente"));
        int idAdmin = rs.getInt("id_admin");
        r.setIdAdmin(rs.wasNull() ? null : idAdmin);
        r.setIdMenu(rs.getInt("id_menu"));
        r.setFecha(rs.getDate("fecha").toLocalDate());
        r.setCantComensales(rs.getInt("cant_comensales"));
        r.setEstado(rs.getString("estado"));
        Timestamp ts = rs.getTimestamp("fecha_solicitud");
        if (ts != null) r.setFechaSolicitud(ts.toLocalDateTime());
        return r;
    }
}
