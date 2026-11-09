package com.sulqui.servicio;

import com.sulqui.dao.ClienteDAO;
import com.sulqui.dao.Conexion;
import com.sulqui.dao.EventoDAO;
import com.sulqui.dao.MenuDAO;
import com.sulqui.dao.ReservaDAO;
import com.sulqui.estructuras.Cola;
import com.sulqui.estructuras.ListaEnlazada;
import com.sulqui.modelo.Cliente;
import com.sulqui.modelo.EstadoReserva;
import com.sulqui.modelo.Evento;
import com.sulqui.modelo.MenuPaquete;
import com.sulqui.modelo.Reserva;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;

/**
 * Clase de control que concentra las reglas de negocio de las reservas.
 *
 * Reglas implementadas:
 *  RN-01  No puede haber dos reservas confirmadas en la misma fecha.
 *  RN-02  La cantidad de comensales debe estar entre 10 y 150 (inclusive).
 *  RN-03  La fecha del evento debe ser posterior a la fecha actual.
 *  RN-04  Solo una reserva Pendiente puede confirmarse o rechazarse.
 */
public class ReservaService {

    public static final int MIN_COMENSALES = 10;
    public static final int MAX_COMENSALES = 150;

    /** Criterios de ordenamiento disponibles para los listados de reservas. */
    public static final Comparator<Reserva> POR_FECHA = Comparator.comparing(Reserva::getFecha);
    public static final Comparator<Reserva> POR_COMENSALES_DESC =
            Comparator.comparingInt(Reserva::getCantComensales).reversed();

    private final ReservaDAO reservaDAO;
    private final ClienteDAO clienteDAO;
    private final MenuDAO menuDAO;
    private final EventoDAO eventoDAO = new EventoDAO();
    private final NotificacionService notificacionService;

    public ReservaService(NotificacionService notificacionService) {
        this(new ReservaDAO(), new ClienteDAO(), new MenuDAO(), notificacionService);
    }

    public ReservaService(ReservaDAO reservaDAO, ClienteDAO clienteDAO, MenuDAO menuDAO,
                          NotificacionService notificacionService) {
        this.reservaDAO = reservaDAO;
        this.clienteDAO = clienteDAO;
        this.menuDAO = menuDAO;
        this.notificacionService = notificacionService;
    }

    /** CU-01 / RF-01 (RN-01). */
    public boolean consultarDisponibilidad(LocalDate fecha) throws SQLException {
        return !reservaDAO.existeReservaConfirmada(fecha);
    }

    /** Sobrecarga: misma consulta recibiendo dia, mes y anio por separado (menu de consola). */
    public boolean consultarDisponibilidad(int dia, int mes, int anio) throws SQLException, ValidacionException {
        try {
            return consultarDisponibilidad(LocalDate.of(anio, mes, dia));
        } catch (DateTimeException e) {
            throw new ValidacionException("La fecha " + dia + "/" + mes + "/" + anio + " no existe.");
        }
    }

    /** RN-02: particion de equivalencia [10..150] valida; <10 y >150 invalidas. */
    public void validarComensales(int cantidad) throws ValidacionException {
        if (cantidad < MIN_COMENSALES || cantidad > MAX_COMENSALES) {
            throw new ValidacionException("La cantidad de comensales debe estar entre "
                    + MIN_COMENSALES + " y " + MAX_COMENSALES + " (ingresado: " + cantidad + ").");
        }
    }

    /** CU-02 / RF-02: registra una solicitud en estado Pendiente y devuelve su id. */
    public int solicitarReserva(int idCliente, int idMenu, LocalDate fecha, int comensales)
            throws SQLException, SulquiException {
        validarComensales(comensales);
        if (fecha == null || !fecha.isAfter(LocalDate.now())) {
            throw new ValidacionException("La fecha del evento debe ser posterior a hoy.");
        }
        if (clienteDAO.buscarPorId(idCliente) == null) {
            throw new RecursoNoEncontradoException("el cliente", idCliente);
        }
        if (menuDAO.buscarPorId(idMenu) == null) {
            throw new RecursoNoEncontradoException("el menu", idMenu);
        }
        if (!consultarDisponibilidad(fecha)) {
            throw new ValidacionException("La fecha " + fecha + " ya tiene un evento confirmado.");
        }
        return reservaDAO.insertar(new Reserva(idCliente, idMenu, fecha, comensales));
    }

    /** CU-05 / RF-03: confirma una reserva pendiente e incluye "Enviar notificacion". */
    public void confirmarReserva(int idReserva, int idAdmin) throws SQLException, SulquiException {
        Reserva r = obtenerPendiente(idReserva);
        if (!consultarDisponibilidad(r.getFecha())) {
            throw new ValidacionException("La fecha " + r.getFecha() + " ya fue confirmada para otro evento.");
        }
        reservaDAO.actualizarEstado(idReserva, idAdmin, EstadoReserva.CONFIRMADA);
        notificar(r, "Tu reserva #" + idReserva + " para el " + r.getFecha() + " fue CONFIRMADA.");
    }

    /**
     * CU-05 / RF-03 (version completa, AP4): confirma la reserva y registra el
     * evento con su direccion y horario en una UNICA transaccion. Si el alta
     * del evento falla, se deshace tambien el cambio de estado (rollback):
     * nunca queda una reserva confirmada sin evento en el calendario.
     */
    public void confirmarReserva(int idReserva, int idAdmin, String direccion, LocalTime horario)
            throws SQLException, SulquiException {
        Reserva r = obtenerPendiente(idReserva);                                   // RN-04
        if (!consultarDisponibilidad(r.getFecha())) {                              // RN-01
            throw new ValidacionException("La fecha " + r.getFecha() + " ya fue confirmada para otro evento.");
        }
        if (direccion == null || direccion.isBlank()) throw new ValidacionException("La direccion del evento es obligatoria.");
        if (horario == null) throw new ValidacionException("El horario del evento es obligatorio.");
        Conexion.getInstancia().enTransaccion("confirmar la reserva #" + idReserva, con -> {
            reservaDAO.actualizarEstado(con, idReserva, idAdmin, EstadoReserva.CONFIRMADA);
            return eventoDAO.insertar(con, new Evento(idReserva, r.getFecha(), direccion.trim(), horario));
        });
        notificar(r, "Tu reserva #" + idReserva + " para el " + r.getFecha() + " a las " + horario
                + " fue CONFIRMADA. Lugar: " + direccion.trim() + ".");
    }

    /** Lugar y horario de una reserva confirmada (null si todavia no se cargaron). */
    public Evento buscarEvento(int idReserva) throws SQLException {
        return eventoDAO.buscarPorReserva(idReserva);
    }

    /** CU-05 / RF-03: rechaza una reserva pendiente e incluye "Enviar notificacion". */
    public void rechazarReserva(int idReserva, int idAdmin, String motivo) throws SQLException, SulquiException {
        Reserva r = obtenerPendiente(idReserva);
        reservaDAO.actualizarEstado(idReserva, idAdmin, EstadoReserva.RECHAZADA);
        notificar(r, "Tu reserva #" + idReserva + " fue RECHAZADA. Motivo: " + motivo);
    }

    /** Total estimado = comensales x precio por persona del menu incluido. */
    public BigDecimal calcularTotal(int idMenu, int comensales) throws SQLException {
        MenuPaquete m = menuDAO.buscarPorId(idMenu);
        return m.getPrecioPorPersona().multiply(BigDecimal.valueOf(comensales));
    }

    /**
     * Cola FIFO con las solicitudes pendientes en orden de llegada: el
     * administrador las atiende de a una, empezando por la mas antigua.
     */
    public Cola<Reserva> colaDePendientes() throws SQLException {
        Cola<Reserva> cola = new Cola<>();
        for (Reserva r : reservaDAO.listarPendientesPorLlegada()) {
            cola.encolar(r);
        }
        return cola;
    }

    /** Todas las reservas en una lista enlazada, ordenadas con merge sort segun el criterio. */
    public ListaEnlazada<Reserva> reservasOrdenadas(Comparator<Reserva> criterio) throws SQLException {
        ListaEnlazada<Reserva> lista = new ListaEnlazada<>();
        for (Reserva r : reservaDAO.listarTodos()) {
            lista.agregar(r);
        }
        lista.ordenar(criterio);
        return lista;
    }

    public Reserva buscar(int idReserva) throws SQLException {
        return reservaDAO.buscarPorId(idReserva);
    }

    public List<Reserva> listarPendientes() throws SQLException {
        return reservaDAO.listarPorEstado(EstadoReserva.PENDIENTE);
    }

    public List<Reserva> listarConfirmadas() throws SQLException {
        return reservaDAO.listarPorEstado(EstadoReserva.CONFIRMADA);
    }

    /** CU-04 / RF-08: calendario de eventos confirmados del mes. */
    public List<Reserva> calendarioDelMes(YearMonth mes) throws SQLException {
        return reservaDAO.listarConfirmadasEntre(mes.atDay(1), mes.atEndOfMonth());
    }

    private Reserva obtenerPendiente(int idReserva) throws SQLException, SulquiException {
        Reserva r = reservaDAO.buscarPorId(idReserva);
        if (r == null) throw new RecursoNoEncontradoException("la reserva", idReserva);
        if (r.getEstado() != EstadoReserva.PENDIENTE) {
            throw new ValidacionException("La reserva " + idReserva + " no esta pendiente (estado: " + r.getEstado() + ").");
        }
        return r;
    }

    private void notificar(Reserva r, String mensaje) throws SQLException {
        Cliente c = clienteDAO.buscarPorId(r.getIdCliente());
        notificacionService.notificar(c, mensaje);
    }
}
