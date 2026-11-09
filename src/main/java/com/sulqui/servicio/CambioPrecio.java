package com.sulqui.servicio;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Registro de un cambio de precio del catalogo; se apila para poder deshacerlo. */
public class CambioPrecio {

    private final int idMenu;
    private final String nombreMenu;
    private final BigDecimal precioAnterior;
    private final BigDecimal precioNuevo;
    private final LocalDateTime momento;

    public CambioPrecio(int idMenu, String nombreMenu, BigDecimal precioAnterior, BigDecimal precioNuevo) {
        this.idMenu = idMenu;
        this.nombreMenu = nombreMenu;
        this.precioAnterior = precioAnterior;
        this.precioNuevo = precioNuevo;
        this.momento = LocalDateTime.now();
    }

    public int getIdMenu() { return idMenu; }
    public String getNombreMenu() { return nombreMenu; }
    public BigDecimal getPrecioAnterior() { return precioAnterior; }
    public BigDecimal getPrecioNuevo() { return precioNuevo; }
    public LocalDateTime getMomento() { return momento; }

    @Override
    public String toString() {
        return nombreMenu + ": $" + precioAnterior + " -> $" + precioNuevo;
    }
}
