package com.sulqui.estructuras;

import java.util.Comparator;
import java.util.function.Predicate;

/**
 * Lista enlazada simple generica. Ademas de las operaciones basicas,
 * ofrece busqueda secuencial y ordenamiento por mezcla (merge sort)
 * aplicado directamente sobre los nodos.
 */
public class ListaEnlazada<T> extends EstructuraEnlazada<T> {

    public ListaEnlazada() {
        super();
    }

    @Override
    public String getTipo() {
        return "lista";
    }

    public void agregar(T dato) {
        insertarAlFinal(dato);
    }

    public void agregarAlInicio(T dato) {
        insertarAlInicio(dato);
    }

    /** Devuelve el elemento de la posicion indicada (0 = primero). O(n). */
    public T obtener(int indice) {
        validarIndice(indice);
        Nodo<T> actual = primero;
        for (int i = 0; i < indice; i++) {
            actual = actual.siguiente;
        }
        return actual.dato;
    }

    /** Quita y devuelve el elemento de la posicion indicada. O(n). */
    public T eliminar(int indice) {
        validarIndice(indice);
        if (indice == 0) return quitarPrimero();
        Nodo<T> anterior = primero;
        for (int i = 0; i < indice - 1; i++) {
            anterior = anterior.siguiente;
        }
        Nodo<T> borrado = anterior.siguiente;
        anterior.siguiente = borrado.siguiente;
        if (borrado == ultimo) ultimo = anterior;
        tamanio--;
        return borrado.dato;
    }

    /** Busqueda secuencial: primer elemento que cumple la condicion, o null. O(n). */
    public T buscar(Predicate<? super T> condicion) {
        Nodo<T> actual = primero;
        while (actual != null) {
            if (condicion.test(actual.dato)) return actual.dato;
            actual = actual.siguiente;
        }
        return null;
    }

    /** Cantidad de elementos que cumplen la condicion. O(n). */
    public int contar(Predicate<? super T> condicion) {
        int cantidad = 0;
        for (T dato : this) {
            if (condicion.test(dato)) cantidad++;
        }
        return cantidad;
    }

    /**
     * Ordena la lista con merge sort sobre los nodos: O(n log n) en todos los
     * casos y estable (los elementos iguales conservan su orden relativo).
     */
    public void ordenar(Comparator<? super T> criterio) {
        primero = ordenarPorMezcla(primero, criterio);
        ultimo = primero;
        while (ultimo != null && ultimo.siguiente != null) {
            ultimo = ultimo.siguiente;
        }
    }

    private Nodo<T> ordenarPorMezcla(Nodo<T> cabeza, Comparator<? super T> criterio) {
        if (cabeza == null || cabeza.siguiente == null) return cabeza;   // caso base
        Nodo<T> medio = buscarMedio(cabeza);
        Nodo<T> segundaMitad = medio.siguiente;
        medio.siguiente = null;                                          // divide en dos
        Nodo<T> izquierda = ordenarPorMezcla(cabeza, criterio);          // recursion
        Nodo<T> derecha = ordenarPorMezcla(segundaMitad, criterio);
        return fusionar(izquierda, derecha, criterio);
    }

    /** Tecnica de dos punteros: uno avanza de a uno y otro de a dos. */
    private Nodo<T> buscarMedio(Nodo<T> cabeza) {
        Nodo<T> lento = cabeza;
        Nodo<T> rapido = cabeza.siguiente;
        while (rapido != null && rapido.siguiente != null) {
            lento = lento.siguiente;
            rapido = rapido.siguiente.siguiente;
        }
        return lento;
    }

    private Nodo<T> fusionar(Nodo<T> a, Nodo<T> b, Comparator<? super T> criterio) {
        Nodo<T> centinela = new Nodo<>(null);
        Nodo<T> cola = centinela;
        while (a != null && b != null) {
            if (criterio.compare(a.dato, b.dato) <= 0) {   // <= mantiene la estabilidad
                cola.siguiente = a;
                a = a.siguiente;
            } else {
                cola.siguiente = b;
                b = b.siguiente;
            }
            cola = cola.siguiente;
        }
        cola.siguiente = (a != null) ? a : b;
        return centinela.siguiente;
    }

    private void validarIndice(int indice) {
        if (indice < 0 || indice >= tamanio) {
            throw new IndexOutOfBoundsException("Indice " + indice + " fuera de rango (tamanio " + tamanio + ").");
        }
    }
}
