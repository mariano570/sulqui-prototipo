package com.sulqui;

import com.sulqui.modelo.EstadoReserva;
import com.sulqui.modelo.Reserva;
import com.sulqui.servicio.NotificacionService;
import com.sulqui.servicio.ReservaService;
import com.sulqui.servicio.ValidacionException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * P9 - Particion de equivalencia con analisis de valores frontera sobre la
 * cantidad de comensales (RN-02: valido si 10 <= n <= 150).
 *
 *   CE1  n < 10        invalida   valores: -5, 0, 9 (frontera inferior - 1)
 *   CE2  10 <= n <= 150 valida    valores: 10 (frontera), 11, 80, 149, 150 (frontera)
 *   CE3  n > 150       invalida   valores: 151 (frontera superior + 1), 500
 */
@DisplayName("P9 Particion de equivalencia: cantidad de comensales")
class ParticionComensalesTest {

    static final LocalDate FECHA = LocalDate.of(2027, 5, 8);
    static ReservaService reservas;

    @BeforeAll
    static void prepararBase() throws Exception {
        BaseDatosPrueba.reiniciar();
        reservas = new ReservaService(new NotificacionService());
    }

    @ParameterizedTest(name = "{0} comensales [{1}] -> {2}")
    @CsvSource({
            "-5,  CE1, RECHAZA",
            "0,   CE1, RECHAZA",
            "9,   CE1, RECHAZA",
            "10,  CE2, ACEPTA",
            "11,  CE2, ACEPTA",
            "80,  CE2, ACEPTA",
            "149, CE2, ACEPTA",
            "150, CE2, ACEPTA",
            "151, CE3, RECHAZA",
            "500, CE3, RECHAZA"
    })
    void cantidadDeComensales(int comensales, String clase, String esperado) throws Exception {
        if (esperado.equals("ACEPTA")) {
            int id = reservas.solicitarReserva(1, 1, FECHA, comensales);
            Reserva r = reservas.buscar(id);
            System.out.println("P9 " + clase + " n=" + comensales + " obtenido: reserva #" + id + " " + r.getEstado());
            assertEquals(EstadoReserva.PENDIENTE, r.getEstado());
            assertEquals(comensales, r.getCantComensales());
        } else {
            ValidacionException ex = assertThrows(ValidacionException.class,
                    () -> reservas.solicitarReserva(1, 1, FECHA, comensales));
            System.out.println("P9 " + clase + " n=" + comensales + " obtenido: \"" + ex.getMessage() + "\"");
            assertTrue(ex.getMessage().startsWith("La cantidad de comensales debe estar entre 10 y 150"));
        }
    }
}
