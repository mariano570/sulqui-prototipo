package com.sulqui.modelo;

import java.math.BigDecimal;

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

    public double calcularPrecio(int cantidadComensales) {
        return precioPorPersona.doubleValue() * cantidadComensales;
    }

    public int getIdMenu() { return idMenu; }
    public void setIdMenu(int idMenu) { this.idMenu = idMenu; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public BigDecimal getPrecioPorPersona() { return precioPorPersona; }
    public void setPrecioPorPersona(BigDecimal precioPorPersona) { this.precioPorPersona = precioPorPersona; }
    public String getTipoPreparacion() { return tipoPreparacion; }
    public void setTipoPreparacion(String tipoPreparacion) { this.tipoPreparacion = tipoPreparacion; }
}
