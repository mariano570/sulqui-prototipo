package com.sulqui.modelo;

import java.math.BigDecimal;

/** Entidad de dominio (POJO). El calculo del total lo realiza ReservaService. */
public class MenuPaquete {
    private int idMenu;
    private String nombre;
    private BigDecimal precioPorPersona;
    private String tipoPreparacion;

    public MenuPaquete() { }

    public MenuPaquete(int idMenu, String nombre, BigDecimal precioPorPersona, String tipoPreparacion) {
        this.idMenu = idMenu;
        this.nombre = nombre;
        this.precioPorPersona = precioPorPersona;
        this.tipoPreparacion = tipoPreparacion;
    }

    public int getIdMenu() { return idMenu; }
    public void setIdMenu(int idMenu) { this.idMenu = idMenu; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public BigDecimal getPrecioPorPersona() { return precioPorPersona; }
    /** Encapsulamiento: el objeto protege su invariante (precio siempre positivo). */
    public void setPrecioPorPersona(BigDecimal precioPorPersona) {
        if (precioPorPersona == null || precioPorPersona.signum() <= 0) {
            throw new IllegalArgumentException("El precio por persona debe ser mayor a cero.");
        }
        this.precioPorPersona = precioPorPersona;
    }
    public String getTipoPreparacion() { return tipoPreparacion; }
    public void setTipoPreparacion(String tipoPreparacion) { this.tipoPreparacion = tipoPreparacion; }

    @Override
    public String toString() { return nombre + " ($" + precioPorPersona + " por persona)"; }
}
