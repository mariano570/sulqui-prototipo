package com.sulqui.servicio;

import com.sulqui.dao.ReservaDAO;
import com.sulqui.modelo.Reserva;

import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Reglas de negocio asociadas a la gestion de reservas.
 * Concentra la validacion de disponibilidad (RF-01) para que no se
 * dupliquen reservas confirmadas en la misma fecha, y delega en
 * NotificacionService el aviso al cliente (segun el caso de uso
 * <<include>> "Enviar notificacion" definido en el TP1).
 */
public class ReservaService {

    private final ReservaDAO reservaDAO = new ReservaDAO();
    private final NotificacionService notificacionService = new NotificacionService();

    public boolean consultarDisponibilidad(LocalDate fecha) throws SQLException {
        return !reservaDAO.existeReservaConfirmada(fecha);
    }

    public int solicitarReserva(Reserva reserva) throws SQLException {
        int id = reservaDAO.insertar(reserva);
        return id;
    }

    public void confirmarReserva(int idReserva, int idAdmin, String emailCliente) throws SQLException {
        reservaDAO.actualizarEstado(idReserva, idAdmin, "Confirmada");
        notificacionService.enviarNotificacion(emailCliente,
                "Tu reserva #" + idReserva + " fue CONFIRMADA.");
    }

    public void rechazarReserva(int idReserva, int idAdmin, String emailCliente) throws SQLException {
        reservaDAO.actualizarEstado(idReserva, idAdmin, "Rechazada");
        notificacionService.enviarNotificacion(emailCliente,
                "Tu reserva #" + idReserva + " fue RECHAZADA.");
    }
}
