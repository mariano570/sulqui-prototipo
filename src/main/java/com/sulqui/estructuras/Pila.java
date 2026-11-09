package com.sulqui.estructuras;

/**
 * Pila (LIFO: el ultimo en entrar es el primero en salir).
 * Se usa para guardar el historial de cambios de precio del catalogo y
 * poder deshacer el ultimo cambio.
 */
public class Pila<T> extends EstructuraEnlazada<T> {

    public Pila() {
        super();
    }

    @Override
    public String getTipo() {
        return "pila";
    }

    /** Agrega un elemento en el tope. O(1). */
    public void apilar(T dato) {
        insertarAlInicio(dato);
    }

    /** Quita y devuelve el elemento del tope. O(1). */
    public T desapilar() {
        return quitarPrimero();
    }

    /** Devuelve el elemento del tope sin quitarlo. O(1). */
    public T verTope() {
        return verPrimero();
    }
}
