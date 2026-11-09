package com.sulqui.servicio;

import com.sulqui.dao.MenuDAO;
import com.sulqui.dao.ReservaDAO;
import com.sulqui.estructuras.Algoritmos;
import com.sulqui.modelo.EstadoReserva;
import com.sulqui.modelo.MenuPaquete;
import com.sulqui.modelo.Reserva;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * Reportes de gestion (RF-08). Muestra el uso complementario de ArrayList y arreglos:
 *
 *  - ArrayList: lo que devuelve la base, cuya cantidad no se conoce de antemano
 *    (reservas confirmadas del anio, catalogo de menus).
 *  - Arreglos: estructuras de tamanio fijo y acceso por indice (12 meses,
 *    5 estados de reserva, grilla de 6 semanas x 7 dias del calendario) y el
 *    catalogo convertido a arreglo para ordenarlo y buscar con busqueda binaria.
 */
public class ReporteService {

    public static final int SEMANAS = 6;
    public static final int DIAS = 7;

    private final ReservaDAO reservaDAO = new ReservaDAO();
    private final MenuDAO menuDAO = new MenuDAO();

    /** Eventos, comensales y facturacion estimada de cada mes del anio. */
    public ResumenAnual resumenAnual(int anio) throws SQLException {
        // ArrayList -> arreglo: el catalogo se ordena por id para buscarlo en O(log n)
        ArrayList<MenuPaquete> catalogo = menuDAO.listarTodos();
        MenuPaquete[] menus = catalogo.toArray(new MenuPaquete[0]);
        Algoritmos.ordenarPorInsercion(menus, Comparator.comparingInt(MenuPaquete::getIdMenu));

        ResumenAnual resumen = new ResumenAnual(anio);
        ArrayList<Reserva> confirmadas = reservaDAO.listarConfirmadasEntre(
                LocalDate.of(anio, 1, 1), LocalDate.of(anio, 12, 31));
        for (Reserva r : confirmadas) {
            int idMenu = r.getIdMenu();
            int i = Algoritmos.busquedaBinaria(menus, m -> Integer.compare(m.getIdMenu(), idMenu));
            BigDecimal precio = i >= 0 ? menus[i].getPrecioPorPersona() : BigDecimal.ZERO;
            resumen.registrar(r.getFecha().getMonthValue(), r.getCantComensales(),
                    precio.multiply(BigDecimal.valueOf(r.getCantComensales())));
        }
        return resumen;
    }

    /**
     * Cantidad de reservas por estado. El arreglo se indexa con el ordinal del
     * enumerado: cuenta[EstadoReserva.PENDIENTE.ordinal()] = pendientes, etc.
     */
    public int[] reservasPorEstado() throws SQLException {
        int[] cuenta = new int[EstadoReserva.values().length];
        for (Reserva r : reservaDAO.listarTodos()) {
            cuenta[r.getEstado().ordinal()]++;
        }
        return cuenta;
    }

    /**
     * Grilla del calendario del mes como matriz [semana][dia] (lunes a domingo).
     * Cada casillero tiene el dia del mes, en negativo si ese dia hay un evento
     * confirmado, o 0 si el casillero no pertenece al mes.
     */
    public int[][] grillaCalendario(YearMonth mes) throws SQLException {
        int[][] grilla = new int[SEMANAS][DIAS];
        boolean[] ocupado = new boolean[mes.lengthOfMonth() + 1];          // indice = dia del mes
        for (Reserva r : reservaDAO.listarConfirmadasEntre(mes.atDay(1), mes.atEndOfMonth())) {
            ocupado[r.getFecha().getDayOfMonth()] = true;
        }
        int columna = mes.atDay(1).getDayOfWeek().getValue() - 1;         // lunes = 0
        int fila = 0;
        for (int dia = 1; dia <= mes.lengthOfMonth(); dia++) {
            grilla[fila][columna] = ocupado[dia] ? -dia : dia;
            columna++;
            if (columna == DIAS) {
                columna = 0;
                fila++;
            }
        }
        return grilla;
    }
}
