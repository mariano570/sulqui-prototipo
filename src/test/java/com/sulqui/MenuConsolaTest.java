package com.sulqui;

import com.sulqui.app.MenuConsola;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * P25: sesion completa del menu de consola con datos invalidos y validos
 * (actualizada en la AP4 con el lugar del evento, reportes y archivos).
 * La salida se guarda en evidencia/sesion_menu_consola.txt.
 */
@DisplayName("Menu de seleccion por consola")
class MenuConsolaTest {

    static final String ENTRADAS = String.join("\n",
            // 1. Consultar disponibilidad: fecha ocupada
            "1", "15", "11", "2026",
            // 1. Consultar disponibilidad: dia no numerico y fuera de rango, luego fecha libre
            "1", "abc", "32", "19", "12", "2026",
            // 2. Solicitar reserva: fecha mal escrita y 151 comensales (rechazada)
            "2", "Martina Rossi", "3517005555", "martina.rossi@example.com", "2", "31/02/2027", "19/12/2026", "151",
            // 2. Solicitar reserva valida
            "2", "Martina Rossi", "3517005555", "martina.rossi@example.com", "2", "19/12/2026", "30",
            // 3. Mis reservas
            "3", "ana.gomez@example.com",
            // 4. Administracion: clave incorrecta y luego correcta
            "4", "mariano", "clave-equivocada", "mariano", "sulqui2026",
            // Cola: posponer #2, confirmar #4 (lugar y horario, con hora invalida), rechazar #5, volver
            "1", "P", "C", "Av. Velez Sarsfield 500, Cordoba", "25:00", "20:30",
            "R", "Sin disponibilidad del equipo", "V",
            // Buscar cliente por nombre / listar clientes / listar reservas / catalogo
            "5", "lu", "4", "1", "6", "2", "7",
            // Cambiar precio (con importe invalido) y deshacer dos veces (la segunda no hay nada)
            "8", "1", "abc", "9200", "9", "9",
            // Registrar senia de la reserva #4 recien confirmada
            "3", "4", "60000", "2",
            // AP4: resumen anual 2026 (arreglos), exportar reservas, respaldar y restaurar catalogo
            "10", "2026", "11", "12", "13", "D",
            // Calendario de diciembre 2026 (grilla con matriz), cerrar sesion y salir
            "2", "12", "2026", "0", "0", "");

    @Test
    @DisplayName("P25 Sesion completa del menu: opciones, datos invalidos y excepciones manejadas")
    void p25SesionCompleta() throws Exception {
        BaseDatosPrueba.reiniciarConDatosDeEjemplo();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream salida = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        PrintStream original = System.out;
        System.setOut(salida);                         // las notificaciones simuladas tambien quedan registradas
        try {
            new MenuConsola(new ByteArrayInputStream(ENTRADAS.getBytes(StandardCharsets.UTF_8)), salida, true).iniciar();
        } finally {
            System.setOut(original);
        }
        String sesion = buffer.toString(StandardCharsets.UTF_8);
        Files.createDirectories(Path.of("evidencia"));
        Files.writeString(Path.of("evidencia", "sesion_menu_consola.txt"), sesion);

        assertTrue(sesion.contains("La fecha 15/11/2026 ya tiene un evento confirmado."));
        assertTrue(sesion.contains("\"abc\" no es un numero entero."));
        assertTrue(sesion.contains("Ingrese un valor entre 1 y 31."));
        assertTrue(sesion.contains("[OK] La fecha 19/12/2026 esta disponible."));
        assertTrue(sesion.contains("\"31/02/2027\" no es una fecha valida"));
        assertTrue(sesion.contains("Dato no valido: La cantidad de comensales debe estar entre 10 y 150 (ingresado: 151)."));
        assertTrue(sesion.contains("Solicitud N.o 5 registrada como Pendiente"));
        assertTrue(sesion.contains("Credenciales incorrectas (intento 1 de 3)"));
        assertTrue(sesion.contains("Reserva #2 pospuesta al final de la cola."));
        assertTrue(sesion.contains("[OK] Reserva #4 confirmada."));
        assertTrue(sesion.contains("[OK] Reserva #5 rechazada."));
        assertTrue(sesion.contains("[WHATSAPP] +543517005555"));
        assertTrue(sesion.contains("Lucia Fernandez"));
        assertTrue(sesion.contains("No hay cambios de precio para deshacer."));
        assertTrue(sesion.contains("Senia registrada y cliente notificado."));
        assertTrue(sesion.contains("Sesion finalizada."));
        // AP4
        assertTrue(sesion.contains("\"25:00\" no es un horario valido"));
        assertTrue(sesion.contains("Lugar: Av. Velez Sarsfield 500, Cordoba"));
        assertTrue(sesion.contains("Mes de mayor demanda: Diciembre"));
        assertTrue(sesion.contains("reservas exportadas a"));
        assertTrue(sesion.contains("menus respaldados en"));
        assertTrue(sesion.contains("Catalogo restaurado desde"));
        assertTrue(sesion.contains("20:30 hs - Av. Velez Sarsfield 500, Cordoba"));
        System.out.println("P25 obtenido: sesion de " + sesion.lines().count()
                + " lineas guardada en evidencia/sesion_menu_consola.txt; 23 verificaciones correctas");
    }
}
