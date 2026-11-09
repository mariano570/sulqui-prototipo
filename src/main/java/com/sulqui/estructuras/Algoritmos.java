package com.sulqui.estructuras;

import java.util.Comparator;
import java.util.function.ToIntFunction;

/**
 * Algoritmos de ordenacion y busqueda sobre arreglos.
 * Clase utilitaria: constructor privado y metodos estaticos.
 */
public final class Algoritmos {

    private Algoritmos() { }

    /**
     * Ordenamiento por insercion. O(n^2) en el peor caso, pero muy eficiente
     * para arreglos chicos o casi ordenados (por ejemplo, el catalogo de menus).
     */
    public static <T> void ordenarPorInsercion(T[] arreglo, Comparator<? super T> criterio) {
        for (int i = 1; i < arreglo.length; i++) {
            T actual = arreglo[i];
            int j = i - 1;
            while (j >= 0 && criterio.compare(arreglo[j], actual) > 0) {
                arreglo[j + 1] = arreglo[j];   // desplaza a la derecha
                j--;
            }
            arreglo[j + 1] = actual;
        }
    }

    /**
     * Busqueda binaria sobre un arreglo ORDENADO. O(log n).
     *
     * @param comparacion funcion que compara un elemento con la clave buscada:
     *                    negativo si el elemento es menor, 0 si coincide,
     *                    positivo si es mayor.
     * @return indice del primer elemento que coincide, o -1 si no hay ninguno.
     */
    public static <T> int busquedaBinaria(T[] ordenado, ToIntFunction<? super T> comparacion) {
        int izquierda = 0;
        int derecha = ordenado.length - 1;
        int encontrado = -1;
        while (izquierda <= derecha) {
            int medio = izquierda + (derecha - izquierda) / 2;   // evita desbordamiento
            int resultado = comparacion.applyAsInt(ordenado[medio]);
            if (resultado == 0) {
                encontrado = medio;
                derecha = medio - 1;          // sigue a la izquierda: primera coincidencia
            } else if (resultado < 0) {
                izquierda = medio + 1;
            } else {
                derecha = medio - 1;
            }
        }
        return encontrado;
    }

    /** Busqueda secuencial: sirve con arreglos sin ordenar. O(n). */
    public static <T> int busquedaSecuencial(T[] arreglo, ToIntFunction<? super T> comparacion) {
        for (int i = 0; i < arreglo.length; i++) {
            if (comparacion.applyAsInt(arreglo[i]) == 0) return i;
        }
        return -1;
    }

    /**
     * Comparacion por prefijo de texto, sin distinguir mayusculas: devuelve 0 si
     * el texto empieza con el prefijo. En un arreglo ordenado por ese texto, los
     * que comparten prefijo quedan contiguos, por eso sirve para busqueda binaria.
     */
    public static int compararPrefijo(String texto, String prefijo) {
        String t = texto.toLowerCase();
        String p = prefijo.toLowerCase();
        if (t.startsWith(p)) return 0;
        return t.compareTo(p);
    }
}
