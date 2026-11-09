package com.sulqui.dao;

import com.sulqui.modelo.SeniaPago;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/** Acceso a datos para las senias / pagos (tabla senia_pago). */
public class SeniaPagoDAO extends DaoJdbc<SeniaPago> {

    @Override protected String tabla() { return "senia_pago"; }

    @Override protected String columnaId() { return "id_senia"; }

    @Override
    public int insertar(SeniaPago s) throws SQLException {
        return insertarConClave("registrar la senia",
                "INSERT INTO senia_pago (id_reserva, monto, fecha, medio_de_pago, estado) VALUES (?, ?, ?, ?, ?)",
                ps -> {
                    ps.setInt(1, s.getIdReserva());
                    ps.setBigDecimal(2, s.getMonto());
                    ps.setDate(3, Date.valueOf(s.getFecha()));
                    ps.setString(4, s.getMedioDePago());
                    ps.setString(5, s.getEstado());
                });
    }

    public ArrayList<SeniaPago> listarPorReserva(int idReserva) throws SQLException {
        return consultarLista("SELECT * FROM senia_pago WHERE id_reserva = ? ORDER BY fecha",
                ps -> ps.setInt(1, idReserva));
    }

    @Override
    protected SeniaPago mapear(ResultSet rs) throws SQLException {
        SeniaPago s = new SeniaPago(rs.getInt("id_reserva"), rs.getBigDecimal("monto"),
                rs.getDate("fecha").toLocalDate(), rs.getString("medio_de_pago"));
        s.setIdSenia(rs.getInt("id_senia"));
        s.setEstado(rs.getString("estado"));
        return s;
    }
}
