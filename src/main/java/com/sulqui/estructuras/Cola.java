package com.sulqui.estructuras;

/**
 * Cola (FIFO: el primero en entrar es el primero en salir).
 * Se usa para atender las solicitudes de reserva pendientes en el orden
 * en que llegaron.
 */
public class Cola<T> extends EstructuraEnlazada<T> {

    public Cola() {
        super();
    }

    @Override
    public String getTipo() {
        return "cola";
    }

    /** Agrega un elemento al final de la cola. O(1). */
    public void encolar(T dato) {
        insertarAlFinal(dato);
    }

    /** Quita y devuelve el elemento del frente. O(1). */
    public T desencolar() {
        return quitarPrimero();
    }

    /** Devuelve el elemento del frente sin quitarlo. O(1). */
    public T verFrente() {
        return verPrimero();
    }
}
