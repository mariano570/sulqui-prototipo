package com.sulqui;

import com.sulqui.estructuras.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

/** P10 a P15: estructuras de datos propias y algoritmos de ordenacion y busqueda (sin base de datos). */
@DisplayName("Estructuras de datos y algoritmos")
class EstructurasYAlgoritmosTest {

    @Test
    @DisplayName("P10 Pila: apilar 3 elementos y desapilar en orden inverso (LIFO)")
    void p10Pila() {
        Pila<String> pila = new Pila<>();
        pila.apilar("A");
        pila.apilar("B");
        pila.apilar("C");
        assertEquals("pila [C, B, A]", pila.toString());
        assertEquals("C", pila.verTope());
        assertEquals("C", pila.desapilar());
        assertEquals("B", pila.desapilar());
        assertEquals("A", pila.desapilar());
        assertTrue(pila.estaVacia());
        System.out.println("P10 obtenido: desapilados C, B, A; pila vacia=" + pila.estaVacia());
    }

    @Test
    @DisplayName("P11 Cola: encolar 3 elementos, desencolar en el mismo orden (FIFO) y posponer")
    void p11Cola() {
        Cola<Integer> cola = new Cola<>();
        cola.encolar(2);
        cola.encolar(4);
        cola.encolar(5);
        cola.encolar(cola.desencolar());           // posponer: el 2 pasa al final
        assertEquals("cola [4, 5, 2]", cola.toString());
        assertEquals(4, cola.desencolar());
        assertEquals(5, cola.desencolar());
        assertEquals(2, cola.desencolar());
        System.out.println("P11 obtenido: tras posponer -> [4, 5, 2]; desencolados 4, 5, 2");
    }

    @Test
    @DisplayName("P12 Pila y cola vacias lanzan EstructuraVaciaException")
    void p12EstructuraVacia() {
        EstructuraVaciaException e1 = assertThrows(EstructuraVaciaException.class, () -> new Pila<String>().desapilar());
        EstructuraVaciaException e2 = assertThrows(EstructuraVaciaException.class, () -> new Cola<String>().verFrente());
        assertEquals("La pila esta vacia.", e1.getMessage());
        assertEquals("La cola esta vacia.", e2.getMessage());
        System.out.println("P12 obtenido: \"" + e1.getMessage() + "\" / \"" + e2.getMessage() + "\"");
    }

    @Test
    @DisplayName("P13 Lista enlazada: agregar, obtener, eliminar y buscar; indice invalido")
    void p13ListaEnlazada() {
        ListaEnlazada<String> lista = new ListaEnlazada<>();
        lista.agregar("Asado");
        lista.agregar("Disco");
        lista.agregarAlInicio("Premium");
        assertEquals("lista [Premium, Asado, Disco]", lista.toString());
        assertEquals("Asado", lista.obtener(1));
        assertEquals("Disco", lista.eliminar(2));
        lista.agregar("Vegetariano");                       // verifica que 'ultimo' quedo bien
        assertEquals("lista [Premium, Asado, Vegetariano]", lista.toString());
        assertEquals("Vegetariano", lista.buscar(s -> s.startsWith("Veg")));
        assertNull(lista.buscar(s -> s.startsWith("X")));
        assertThrows(IndexOutOfBoundsException.class, () -> lista.obtener(3));
        System.out.println("P13 obtenido: " + lista + "; obtener(3) -> IndexOutOfBoundsException");
    }

    @Test
    @DisplayName("P14 Merge sort sobre la lista enlazada: ordena y es estable")
    void p14MergeSort() {
        ListaEnlazada<int[]> lista = new ListaEnlazada<>();   // {valor, orden de llegada}
        int[] valores = {40, 15, 60, 15, 25, 150, 10};
        for (int i = 0; i < valores.length; i++) lista.agregar(new int[]{valores[i], i});
        lista.ordenar(Comparator.comparingInt(a -> a[0]));
        StringBuilder sb = new StringBuilder();
        int anterior = Integer.MIN_VALUE;
        for (int[] par : lista) {
            assertTrue(par[0] >= anterior);
            anterior = par[0];
            sb.append(par[0]).append(' ');
        }
        assertEquals(1, lista.obtener(1)[1]);   // el primer 15 (llegada 1) sigue antes que el segundo (llegada 3)
        assertEquals(3, lista.obtener(2)[1]);
        ListaEnlazada<Integer> vacia = new ListaEnlazada<>();
        vacia.ordenar(Comparator.naturalOrder());
        assertTrue(vacia.estaVacia());
        System.out.println("P14 obtenido: " + sb.toString().trim() + " (estable)");
    }

    @Test
    @DisplayName("P15 Ordenamiento por insercion y busqueda binaria/secuencial en arreglos")
    void p15InsercionYBusqueda() {
        String[] nombres = {"Lucia Fernandez", "Ana Gomez", "Martina Rossi", "Carlos Perez", "Marcos Diaz"};
        Algoritmos.ordenarPorInsercion(nombres, String.CASE_INSENSITIVE_ORDER);
        assertArrayEquals(new String[]{"Ana Gomez", "Carlos Perez", "Lucia Fernandez", "Marcos Diaz", "Martina Rossi"}, nombres);

        int i = Algoritmos.busquedaBinaria(nombres, n -> n.compareToIgnoreCase("Lucia Fernandez"));
        assertEquals(2, i);
        assertEquals(-1, Algoritmos.busquedaBinaria(nombres, n -> n.compareToIgnoreCase("Pedro")));
        assertEquals(3, Algoritmos.busquedaBinaria(nombres, n -> Algoritmos.compararPrefijo(n, "mar")));   // primera coincidencia
        assertEquals(-1, Algoritmos.busquedaBinaria(new String[0], n -> 0));
        assertEquals(4, Algoritmos.busquedaSecuencial(nombres, n -> n.equals("Martina Rossi") ? 0 : 1));
        System.out.println("P15 obtenido: ordenado=" + String.join(", ", nombres)
                + "; binaria(\"Lucia Fernandez\")=2; binaria(prefijo \"mar\")=3; binaria(\"Pedro\")=-1");
    }
}
