package com.sulqui.estructuras;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.IntFunction;

/**
 * Clase base abstracta de las estructuras enlazadas propias del sistema
 * (ListaEnlazada, Pila y Cola).
 *
 * Concentra lo que todas comparten: los punteros al primer y ultimo nodo,
 * el tamanio, el recorrido y las operaciones elementales sobre los extremos.
 * Cada subclase expone solo las operaciones que tienen sentido para ella:
 * la Pila inserta y quita por el mismo extremo (LIFO) y la Cola inserta por
 * el final y quita por el principio (FIFO).
 */
public abstract class EstructuraEnlazada<T> implements Iterable<T> {

    protected Nodo<T> primero;
    protected Nodo<T> ultimo;
    protected int tamanio;

    protected EstructuraEnlazada() {
        vaciar();
    }

    /** Nombre de la estructura, para mensajes y para toString(). */
    public abstract String getTipo();

    public boolean estaVacia() {
        return tamanio == 0;
    }

    public int tamanio() {
        return tamanio;
    }

    public final void vaciar() {
        primero = null;
        ultimo = null;
        tamanio = 0;
    }

    // ---------- operaciones elementales reutilizadas por las subclases: O(1) ----------

    protected void insertarAlInicio(T dato) {
        primero = new Nodo<>(dato, primero);
        if (ultimo == null) ultimo = primero;
        tamanio++;
    }

    protected void insertarAlFinal(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);
        if (estaVacia()) {
            primero = nuevo;
        } else {
            ultimo.siguiente = nuevo;
        }
        ultimo = nuevo;
        tamanio++;
    }

    protected T quitarPrimero() {
        if (estaVacia()) throw new EstructuraVaciaException(getTipo());
        T dato = primero.dato;
        primero = primero.siguiente;
        if (primero == null) ultimo = null;
        tamanio--;
        return dato;
    }

    protected T verPrimero() {
        if (estaVacia()) throw new EstructuraVaciaException(getTipo());
        return primero.dato;
    }

    /** Copia los elementos a un arreglo, respetando el orden de recorrido. */
    public T[] aArreglo(IntFunction<T[]> generador) {
        T[] arreglo = generador.apply(tamanio);
        int i = 0;
        for (Nodo<T> n = primero; n != null; n = n.siguiente) {
            arreglo[i++] = n.dato;
        }
        return arreglo;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            private Nodo<T> actual = primero;

            @Override
            public boolean hasNext() {
                return actual != null;
            }

            @Override
            public T next() {
                if (actual == null) throw new NoSuchElementException();
                T dato = actual.dato;
                actual = actual.siguiente;
                return dato;
            }
        };
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getTipo()).append(" [");
        for (Nodo<T> n = primero; n != null; n = n.siguiente) {
            sb.append(n.dato);
            if (n.siguiente != null) sb.append(", ");
        }
        return sb.append("]").toString();
    }
}
