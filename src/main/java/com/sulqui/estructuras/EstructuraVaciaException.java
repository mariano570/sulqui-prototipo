package com.sulqui.estructuras;

/** Se lanza al intentar quitar o consultar un elemento de una pila o cola vacia. */
public class EstructuraVaciaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EstructuraVaciaException(String estructura) {
        super("La " + estructura + " esta vacia.");
    }
}
