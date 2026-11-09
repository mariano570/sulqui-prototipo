package com.sulqui.modelo;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad de dominio (POJO). No contiene reglas de negocio: los cambios de
 * estado los realiza ReservaService, que valida la transicion y persiste.
 * Cada reserva incluye exactamente un MenuPaquete (multiplicidad 1, id_menu).
 */
public class Reserva {
    private int idReserva;
    private int idCliente;
    private Integer idAdmin;
    private int idMenu;
    private LocalDate fecha;
    private int cantComensales;
    private EstadoReserva estado;
    private LocalDateTime fechaSolicitud;

    public Reserva() { }

    public Reserva(int idCliente, int idMenu, LocalDate fecha, int cantComensales) {
        this.idCliente = idCliente;
        this.idMenu = idMenu;
        this.fecha = fecha;
        this.cantComensales = cantComensales;
        this.estado = EstadoReserva.PENDIENTE;
    }

    public int getIdReserva() { return idReserva; }
    public void setIdReserva(int idReserva) { this.idReserva = idReserva; }
    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }
    public Integer getIdAdmin() { return idAdmin; }
    public void setIdAdmin(Integer idAdmin) { this.idAdmin = idAdmin; }
    public int getIdMenu() { return idMenu; }
    public void setIdMenu(int idMenu) { this.idMenu = idMenu; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public int getCantComensales() { return cantComensales; }
    public void setCantComensales(int cantComensales) { this.cantComensales = cantComensales; }
    public EstadoReserva getEstado() { return estado; }
    public void setEstado(EstadoReserva estado) { this.estado = estado; }
    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(LocalDateTime fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }

    @Override
    public String toString() {
        return "Reserva{" + idReserva + ", cliente=" + idCliente + ", fecha=" + fecha
                + ", comensales=" + cantComensales + ", estado=" + estado + "}";
    }
}
