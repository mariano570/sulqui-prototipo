package com.sulqui.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SeniaPago {
    private int idSenia;
    private int idReserva;
    private BigDecimal monto;
    private LocalDate fecha;
    private String medioDePago;
    private String estado;

    public SeniaPago() { }

    public SeniaPago(int idReserva, BigDecimal monto, LocalDate fecha, String medioDePago) {
        this.idReserva = idReserva;
        this.monto = monto;
        this.fecha = fecha;
        this.medioDePago = medioDePago;
        this.estado = "Registrada";
    }

    public int getIdSenia() { return idSenia; }
    public void setIdSenia(int idSenia) { this.idSenia = idSenia; }
    public int getIdReserva() { return idReserva; }
    public void setIdReserva(int idReserva) { this.idReserva = idReserva; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public String getMedioDePago() { return medioDePago; }
    public void setMedioDePago(String medioDePago) { this.medioDePago = medioDePago; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
