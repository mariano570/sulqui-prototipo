package com.sulqui.modelo;

/**
 * Abstraccion de toda persona que interactua con el sistema.
 *
 * Es abstracta porque no existe una "persona" generica en Sulqui: solo
 * clientes y administradores. Define lo comun (identificador numerico) y
 * obliga a cada subclase a decir cual es su rol y como se la identifica.
 */
public abstract class Persona {

    private int id;   // encapsulado: solo se accede por get/set

    protected Persona() {
        this(0);
    }

    protected Persona(int id) {
        this.id = id;
    }

    public int getId() { return id; }

    public void setId(int id) {
        if (id < 0) throw new IllegalArgumentException("El id no puede ser negativo.");
        this.id = id;
    }

    /** Dato con el que la persona se identifica ante el sistema (email o usuario). */
    public abstract String getIdentificador();

    /** Nombre del rol: "Cliente" o "Administrador". */
    public abstract String getRol();

    /** Metodo comun que usa los metodos abstractos (cada subclase aporta su parte). */
    public String describir() {
        return getRol() + " #" + id + " (" + getIdentificador() + ")";
    }
}
