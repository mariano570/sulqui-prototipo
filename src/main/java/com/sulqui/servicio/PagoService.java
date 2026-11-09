package com.sulqui.servicio;

import com.sulqui.dao.ClienteDAO;
import com.sulqui.dao.ReservaDAO;
import com.sulqui.dao.SeniaPagoDAO;
import com.sulqui.modelo.EstadoReserva;
import com.sulqui.modelo.Reserva;
import com.sulqui.modelo.SeniaPago;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/** CU-07 / RF-05: registro de senias y pagos. */
public class PagoService {

    public static final Set<String> MEDIOS_VALIDOS = Set.of("Efectivo", "Transferencia", "Mercado Pago");

    private final SeniaPagoDAO seniaPagoDAO = new SeniaPagoDAO();
    private final ReservaDAO reservaDAO = new ReservaDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final NotificacionService notificacionService;

    public PagoService(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    public int registrarPago(int idReserva, BigDecimal monto, String medioDePago)
            throws SQLException, SulquiException {
        Reserva r = reservaDAO.buscarPorId(idReserva);
        if (r == null) throw new RecursoNoEncontradoException("la reserva", idReserva);
        if (r.getEstado() != EstadoReserva.CONFIRMADA)
            throw new ValidacionException("Solo se registran senias de reservas confirmadas.");
        if (monto == null || monto.signum() <= 0)
            throw new ValidacionException("El monto debe ser mayor a cero.");
        if (!MEDIOS_VALIDOS.contains(medioDePago))
            throw new ValidacionException("Medio de pago no valido: " + medioDePago);
        int id = seniaPagoDAO.insertar(new SeniaPago(idReserva, monto, LocalDate.now(), medioDePago));
        notificacionService.notificar(clienteDAO.buscarPorId(r.getIdCliente()),
                "Registramos tu senia de $" + monto + " (" + medioDePago + ") para la reserva #" + idReserva + ".");
        return id;
    }

    public List<SeniaPago> listarPorReserva(int idReserva) throws SQLException {
        return seniaPagoDAO.listarPorReserva(idReserva);
    }
}
