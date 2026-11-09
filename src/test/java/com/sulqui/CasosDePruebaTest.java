package com.sulqui;

import com.sulqui.dao.Conexion;
import com.sulqui.modelo.Cliente;
import com.sulqui.modelo.EstadoReserva;
import com.sulqui.modelo.Reserva;
import com.sulqui.servicio.ClienteService;
import com.sulqui.servicio.NotificacionService;
import com.sulqui.servicio.ReservaService;
import com.sulqui.servicio.ValidacionException;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/** Casos de prueba funcionales P1 a P8 (apartado 6 del documento). */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Casos de prueba funcionales Sulqui")
class CasosDePruebaTest {

    static final LocalDate FECHA_OCUPADA = LocalDate.of(2027, 3, 13);
    static final LocalDate FECHA_LIBRE = LocalDate.of(2027, 4, 10);

    static NotificacionService notificaciones;
    static ReservaService reservas;
    static ClienteService clientes;
    static int idClienteNuevo;
    static int idReservaNueva;

    @BeforeAll
    static void prepararBase() throws Exception {
        BaseDatosPrueba.reiniciar();
        notificaciones = new NotificacionService();
        reservas = new ReservaService(notificaciones);
        clientes = new ClienteService();
    }

    @Test @Order(1)
    @DisplayName("P1 Alta de cliente con datos validos")
    void p1AltaCliente() throws Exception {
        idClienteNuevo = clientes.registrarCliente("Marcos Diaz", "3517004444", "marcos.diaz@example.com");
        Cliente c = clientes.buscar(idClienteNuevo);
        System.out.println("P1 obtenido: id=" + idClienteNuevo + " -> " + c);
        assertEquals(2, idClienteNuevo);
        assertEquals("Marcos Diaz", c.getNombre());
        assertEquals("3517004444", c.getTelefono());
        assertEquals("marcos.diaz@example.com", c.getEmail());
    }

    @Test @Order(2)
    @DisplayName("P2 Disponibilidad de fecha libre (10/04/2027)")
    void p2FechaLibre() throws Exception {
        boolean disponible = reservas.consultarDisponibilidad(FECHA_LIBRE);
        System.out.println("P2 obtenido: disponible=" + disponible);
        assertTrue(disponible);
    }

    @Test @Order(3)
    @DisplayName("P3 Disponibilidad de fecha ocupada (13/03/2027)")
    void p3FechaOcupada() throws Exception {
        boolean disponible = reservas.consultarDisponibilidad(FECHA_OCUPADA);
        System.out.println("P3 obtenido: disponible=" + disponible);
        assertFalse(disponible);
    }

    @Test @Order(4)
    @DisplayName("P4 Solicitar reserva valida (cliente 1, menu 1, 10/04/2027, 20 comensales)")
    void p4SolicitarReserva() throws Exception {
        idReservaNueva = reservas.solicitarReserva(1, 1, FECHA_LIBRE, 20);
        Reserva r = reservas.buscar(idReservaNueva);
        BigDecimal total = reservas.calcularTotal(r.getIdMenu(), r.getCantComensales());
        System.out.println("P4 obtenido: " + r + ", id_admin=" + r.getIdAdmin() + ", total=$" + total);
        assertEquals(2, idReservaNueva);
        assertEquals(EstadoReserva.PENDIENTE, r.getEstado());
        assertNull(r.getIdAdmin());
        assertEquals(0, new BigDecimal("170000.00").compareTo(total));
    }

    @Test @Order(5)
    @DisplayName("P5 Confirmar reserva pendiente #2 (admin 1) y notificar")
    void p5ConfirmarReserva() throws Exception {
        reservas.confirmarReserva(idReservaNueva, 1);
        Reserva r = reservas.buscar(idReservaNueva);
        System.out.println("P5 obtenido: estado=" + r.getEstado() + ", id_admin=" + r.getIdAdmin()
                + ", notificaciones=" + notificaciones.getEnviadas()
                + ", 10/04/2027 disponible=" + reservas.consultarDisponibilidad(FECHA_LIBRE));
        assertEquals(EstadoReserva.CONFIRMADA, r.getEstado());
        assertEquals(1, r.getIdAdmin());
        assertEquals(2, notificaciones.getEnviadas().size());             // un aviso por canal
        assertTrue(notificaciones.getEnviadas().get(0).startsWith("[Email] Para: ana.gomez@example.com"));
        assertTrue(notificaciones.getEnviadas().get(0).contains("CONFIRMADA"));
        assertTrue(notificaciones.getEnviadas().get(1).startsWith("[WhatsApp] Para: +543517001111"));
        assertFalse(reservas.consultarDisponibilidad(FECHA_LIBRE));
    }

    @Test @Order(6)
    @DisplayName("P6 Eliminar cliente sin reservas (#2); baja de #1 rechazada")
    void p6EliminarCliente() throws Exception {
        clientes.eliminarCliente(idClienteNuevo);
        assertNull(clientes.buscar(idClienteNuevo));
        ValidacionException ex = assertThrows(ValidacionException.class, () -> clientes.eliminarCliente(1));
        System.out.println("P6 obtenido: cliente 2 -> " + clientes.buscar(idClienteNuevo)
                + "; cliente 1 -> \"" + ex.getMessage() + "\"");
        assertNotNull(clientes.buscar(1));
    }

    @Test @Order(7)
    @DisplayName("P7 Solicitar reserva sobre fecha ocupada (13/03/2027)")
    void p7FechaOcupadaRechazada() throws Exception {
        ValidacionException ex = assertThrows(ValidacionException.class,
                () -> reservas.solicitarReserva(1, 1, FECHA_OCUPADA, 30));
        int cantidad = clientes.obtenerReservas(1).size();
        System.out.println("P7 obtenido: \"" + ex.getMessage() + "\"; reservas del cliente 1=" + cantidad);
        assertEquals(2, cantidad);
    }

    @Test @Order(8)
    @DisplayName("P8 Defensa en la BD: INSERT directo con 151 comensales")
    void p8RestriccionBaseDatos() {
        SQLException ex = assertThrows(SQLException.class, () -> {
            try (Connection con = Conexion.getInstancia().obtener(); Statement st = con.createStatement()) {
                st.executeUpdate("INSERT INTO reserva (id_cliente, id_menu, fecha, cant_comensales) "
                        + "VALUES (1, 1, '2027-06-05', 151)");
            }
        });
        System.out.println("P8 obtenido: SQLException -> " + ex.getMessage());
        assertTrue(ex.getMessage().contains("chk_comensales"));
    }
}
