package com.sulqui;

import com.sulqui.modelo.Administrador;
import com.sulqui.modelo.Cliente;
import com.sulqui.modelo.MenuPaquete;
import com.sulqui.modelo.Persona;
import com.sulqui.notificacion.CanalEmail;
import com.sulqui.notificacion.CanalNotificacion;
import com.sulqui.notificacion.CanalWhatsApp;
import com.sulqui.servicio.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/** P16 a P19: pilares de la POO y jerarquia de excepciones (sin base de datos). */
@DisplayName("Pilares de la POO y excepciones")
class PooTest {

    @Test
    @DisplayName("P16 Herencia y polimorfismo: Persona[] con Cliente y Administrador")
    void p16Persona() {
        Persona[] personas = {
                new Cliente(1, "Ana Gomez", "3517001111", "ana.gomez@example.com"),
                new Administrador(1, "mariano", "hash")
        };
        assertEquals("Cliente #1 (ana.gomez@example.com)", personas[0].describir());
        assertEquals("Administrador #1 (mariano)", personas[1].describir());
        assertThrows(IllegalArgumentException.class, () -> personas[0].setId(-1));   // encapsulamiento
        System.out.println("P16 obtenido: " + personas[0].describir() + " | " + personas[1].describir());
    }

    @Test
    @DisplayName("P17 Polimorfismo: el mismo aviso sale por email y por WhatsApp con distinto destino")
    void p17Canales() {
        NotificacionService servicio = new NotificacionService(new CanalEmail(), new CanalWhatsApp());
        servicio.notificar(new Cliente(1, "Ana Gomez", "3517001111", "ana.gomez@example.com"), "Tu reserva fue CONFIRMADA.");
        assertEquals(2, servicio.getEnviadas().size());
        assertEquals("[Email] Para: ana.gomez@example.com | Tu reserva fue CONFIRMADA. - Sulqui, eventos a fuego.",
                servicio.getEnviadas().get(0));
        assertEquals("[WhatsApp] Para: +543517001111 | Sulqui: Tu reserva fue CONFIRMADA.",
                servicio.getEnviadas().get(1));
        for (CanalNotificacion canal : servicio.getCanales()) assertEquals(1, canal.getEnviados());
        System.out.println("P17 obtenido: " + servicio.getEnviadas());
    }

    @Test
    @DisplayName("P18 Excepcion manejada: cliente sin email -> falla el email y WhatsApp se envia igual")
    void p18CanalSinDestino() {
        NotificacionService servicio = new NotificacionService();
        servicio.notificar(new Cliente(9, "Sin Email", "3517009999", ""), "Prueba");
        assertEquals(1, servicio.getEnviadas().size());
        assertTrue(servicio.getEnviadas().get(0).startsWith("[WhatsApp]"));
        assertEquals(1, servicio.getErrores().size());
        System.out.println("P18 obtenido: enviadas=" + servicio.getEnviadas() + "; errores=" + servicio.getErrores());
    }

    @Test
    @DisplayName("P19 Encapsulamiento y jerarquia de excepciones")
    void p19Encapsulamiento() {
        MenuPaquete m = new MenuPaquete(1, "Asado clasico", new BigDecimal("8500.00"), "Asado");
        assertThrows(IllegalArgumentException.class, () -> m.setPrecioPorPersona(new BigDecimal("-1")));
        assertEquals(new BigDecimal("8500.00"), m.getPrecioPorPersona());          // el estado no cambio

        SulquiException[] errores = {
                new ValidacionException("x"), new RecursoNoEncontradoException("el menu", 99), new NotificacionException("y")
        };
        assertEquals("Dato no valido", errores[0].getTitulo());
        assertEquals("No encontrado", errores[1].getTitulo());
        assertEquals("No existe el menu con identificador 99.", errores[1].getMessage());
        assertEquals("Notificacion no enviada", errores[2].getTitulo());
        System.out.println("P19 obtenido: precio sigue en $" + m.getPrecioPorPersona()
                + "; titulos: " + errores[0].getTitulo() + " / " + errores[1].getTitulo() + " / " + errores[2].getTitulo());
    }
}
