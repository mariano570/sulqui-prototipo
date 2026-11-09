package com.sulqui.servicio;

import java.math.BigDecimal;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Arrays;
import java.util.Locale;

/**
 * Resultado del reporte anual de eventos confirmados.
 *
 * Usa arreglos de tamanio fijo porque la dimension se conoce de antemano: un
 * casillero por mes (indice 0 = enero ... 11 = diciembre). Los getters
 * devuelven copias para que nadie pueda modificar el reporte desde afuera
 * (encapsulamiento).
 */
public class ResumenAnual {

    public static final int MESES = 12;

    private final int anio;
    private final int[] eventos = new int[MESES];
    private final int[] comensales = new int[MESES];
    private final BigDecimal[] facturacion = new BigDecimal[MESES];

    public ResumenAnual(int anio) {
        this.anio = anio;
        Arrays.fill(facturacion, BigDecimal.ZERO);       // inicializacion de un arreglo de objetos
    }

    /** Suma un evento confirmado en el casillero de su mes (1 a 12). */
    void registrar(int mes, int cantComensales, BigDecimal monto) {
        int i = mes - 1;
        eventos[i]++;
        comensales[i] += cantComensales;
        facturacion[i] = facturacion[i].add(monto);
    }

    public int getAnio() { return anio; }

    public int[] getEventos() { return eventos.clone(); }

    public int[] getComensales() { return comensales.clone(); }

    public BigDecimal[] getFacturacion() { return facturacion.clone(); }

    public int totalEventos() {
        int total = 0;
        for (int e : eventos) total += e;
        return total;
    }

    public int totalComensales() {
        int total = 0;
        for (int c : comensales) total += c;
        return total;
    }

    public BigDecimal totalFacturacion() {
        BigDecimal total = BigDecimal.ZERO;
        for (BigDecimal f : facturacion) total = total.add(f);
        return total;
    }

    /** Mes (1 a 12) con mas comensales; 0 si no hubo eventos. Recorrido lineal del arreglo. */
    public int mesDeMayorDemanda() {
        int indiceMax = -1;
        for (int i = 0; i < MESES; i++) {
            if (comensales[i] > 0 && (indiceMax == -1 || comensales[i] > comensales[indiceMax])) indiceMax = i;
        }
        return indiceMax + 1;
    }

    /** Promedio de comensales por evento en el anio. */
    public double promedioComensales() {
        int total = totalEventos();
        return total == 0 ? 0 : (double) totalComensales() / total;
    }

    public static String nombreMes(int mes) {
        String n = Month.of(mes).getDisplayName(TextStyle.FULL, Locale.forLanguageTag("es-AR"));
        return Character.toUpperCase(n.charAt(0)) + n.substring(1);
    }
}
