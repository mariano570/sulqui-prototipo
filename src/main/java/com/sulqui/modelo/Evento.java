package com.sulqui.modelo;

import java.time.LocalDate;
import java.time.LocalTime;

public class Evento {
    private int idEvento;
    private int idReserva;
    private LocalDate fecha;
    private String direccion;
    private LocalTime horario;

    public Evento() { }

    public Evento(int idReserva, LocalDate fecha, String direccion, LocalTime horario) {
        this.idReserva = idReserva;
        this.fecha = fecha;
        this.direccion = direccion;
        this.horario = horario;
    }

    public int getIdEvento() { return idEvento; }
    public void setIdEvento(int idEvento) { this.idEvento = idEvento; }
    public int getIdReserva() { return idReserva; }
    public void setIdReserva(int idReserva) { this.idReserva = idReserva; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public LocalTime getHorario() { return horario; }
    public void setHorario(LocalTime horario) { this.horario = horario; }
}
