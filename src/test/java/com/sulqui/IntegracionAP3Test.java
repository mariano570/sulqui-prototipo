package com.sulqui;

import com.sulqui.estructuras.Cola;
import com.sulqui.estructuras.ListaEnlazada;
import com.sulqui.modelo.Cliente;
import com.sulqui.modelo.Reserva;
import com.sulqui.servicio.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/** P20 a P24: estructuras y algoritmos integrados con los servicios y la base (datos de ejemplo). */
@DisplayName("Integracion con la base de datos")
class IntegracionAP3Test {

    NotificacionService notificaciones;
    ReservaService reservas;
    ClienteService clientes;
    MenuService menus;

    @BeforeEach
    void prepararBase() throws Exception {
        BaseDatosPrueba.reiniciarConDatosDeEjemplo();
        notificaciones = new NotificacionService();
        reservas = new ReservaService(notificaciones);
        clientes = new ClienteService();
        menus = new MenuService();
    }

    @Test
    @DisplayName("P20 Cola de pendientes respeta el orden de llegada (#2 antes que #4)")
    void p20ColaPendientes() throws Exception {
        Cola<Reserva> cola = reservas.colaDePendientes();
        assertEquals(2, cola.tamanio());
        assertEquals(2, cola.desencolar().getIdReserva());
        assertEquals(4, cola.desencolar().getIdReserva());
        System.out.println("P20 obtenido: cola de 2 pendientes, atendidas #2 y luego #4");
    }

    @Test
    @DisplayName("P21 Busqueda binaria de clientes por nombre (prefijo \"lu\" y \"z\")")
    void p21BuscarPorNombre() throws Exception {
        ListaEnlazada<Cliente> encontrados = clientes.buscarPorNombre("lu");
        assertEquals(1, encontrados.tamanio());
        assertEquals("Lucia Fernandez", encontrados.obtener(0).getNombre());
        assertTrue(clientes.buscarPorNombre("z").estaVacia());
        assertThrows(ValidacionException.class, () -> clientes.buscarPorNombre(" "));
        System.out.println("P21 obtenido: \"lu\" -> " + encontrados + "; \"z\" -> sin resultados");
    }

    @Test
    @DisplayName("P22 Listados ordenados con merge sort (clientes por nombre, reservas por comensales)")
    void p22Listados() throws Exception {
        ListaEnlazada<Cliente> porNombre = clientes.listarOrdenados(ClienteService.POR_NOMBRE);
        assertEquals("Ana Gomez", porNombre.obtener(0).getNombre());
        assertEquals("Lucia Fernandez", porNombre.obtener(2).getNombre());
        ListaEnlazada<Reserva> porComensales = reservas.reservasOrdenadas(ReservaService.POR_COMENSALES_DESC);
        StringBuilder sb = new StringBuilder();
        for (Reserva r : porComensales) sb.append(r.getCantComensales()).append(' ');
        assertEquals("60 40 25 15", sb.toString().trim());
        System.out.println("P22 obtenido: clientes [Ana Gomez, Carlos Perez, Lucia Fernandez]; comensales " + sb.toString().trim());
    }

    @Test
    @DisplayName("P23 Pila de cambios de precio: cambiar dos veces y deshacer en orden inverso")
    void p23DeshacerPrecio() throws Exception {
        menus.actualizarPrecio(1, new BigDecimal("9000.00"));
        menus.actualizarPrecio(1, new BigDecimal("9500.00"));
        assertEquals(2, menus.cambiosParaDeshacer());
        assertEquals(new BigDecimal("9000.00"), menus.deshacerUltimoCambio().getPrecioAnterior());
        assertEquals(0, new BigDecimal("9000.00").compareTo(menus.obtenerMenu(1).getPrecioPorPersona()));
        menus.deshacerUltimoCambio();
        assertEquals(0, new BigDecimal("8500.00").compareTo(menus.obtenerMenu(1).getPrecioPorPersona()));
        ValidacionException ex = assertThrows(ValidacionException.class, menus::deshacerUltimoCambio);
        assertThrows(ValidacionException.class, () -> menus.actualizarPrecio(1, new BigDecimal("0")));
        assertThrows(RecursoNoEncontradoException.class, () -> menus.actualizarPrecio(99, new BigDecimal("100")));
        System.out.println("P23 obtenido: 8500 -> 9000 -> 9500; deshacer -> 9000; deshacer -> 8500; tercer deshacer: \""
                + ex.getMessage() + "\"");
    }

    @Test
    @DisplayName("P24 Sobrecarga consultarDisponibilidad(dia, mes, anio) y fecha inexistente")
    void p24Sobrecarga() throws Exception {
        assertFalse(reservas.consultarDisponibilidad(15, 11, 2026));
        assertTrue(reservas.consultarDisponibilidad(19, 12, 2026));
        ValidacionException ex = assertThrows(ValidacionException.class, () -> reservas.consultarDisponibilidad(31, 2, 2027));
        RecursoNoEncontradoException ex2 = assertThrows(RecursoNoEncontradoException.class,
                () -> reservas.solicitarReserva(99, 1, java.time.LocalDate.of(2027, 1, 9), 20));
        System.out.println("P24 obtenido: 15/11/2026=false, 19/12/2026=true, 31/2/2027 -> \"" + ex.getMessage()
                + "\"; cliente 99 -> \"" + ex2.getMessage() + "\"");
    }
}
