package com.sulqui.modelo;

/**
 * Cliente de Sulqui. Hereda de Persona e implementa Comparable para que
 * su orden natural sea alfabetico por nombre (usado al ordenar listados).
 */
public class Cliente extends Persona implements Comparable<Cliente> {

    private String nombre;
    private String telefono;
    private String email;

    public Cliente() {
        super();
    }

    /** Constructor para un cliente nuevo, todavia sin id (lo asigna la base). */
    public Cliente(String nombre, String telefono, String email) {
        this(0, nombre, telefono, email);
    }

    public Cliente(int idCliente, String nombre, String telefono, String email) {
        super(idCliente);
        this.nombre = nombre;
        this.telefono = telefono;
        this.email = email;
    }

    public int getIdCliente() { return getId(); }
    public void setIdCliente(int idCliente) { setId(idCliente); }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public String getIdentificador() { return email; }

    @Override
    public String getRol() { return "Cliente"; }

    @Override
    public int compareTo(Cliente otro) {
        return String.CASE_INSENSITIVE_ORDER.compare(this.nombre, otro.nombre);
    }

    @Override
    public String toString() {
        return "Cliente{" + getId() + ", " + nombre + ", " + telefono + ", " + email + "}";
    }
}
