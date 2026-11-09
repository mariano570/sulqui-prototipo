package com.sulqui.estructuras;

/** Nodo de una estructura enlazada simple: un dato y la referencia al siguiente nodo. */
class Nodo<T> {

    final T dato;
    Nodo<T> siguiente;

    Nodo(T dato) {
        this(dato, null);
    }

    Nodo(T dato, Nodo<T> siguiente) {
        this.dato = dato;
        this.siguiente = siguiente;
    }
}
