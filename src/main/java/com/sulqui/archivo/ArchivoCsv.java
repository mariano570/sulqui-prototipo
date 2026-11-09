package com.sulqui.archivo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Lectura y escritura de archivos de texto separados por punto y coma (CSV),
 * en UTF-8, que se abren directamente con una planilla de calculo.
 *
 * Clase abstracta con metodos plantilla: el recorrido del archivo, el
 * encabezado, el manejo de errores y el cierre de recursos son comunes;
 * cada subclase define las columnas y como convierte un objeto en una fila
 * y una fila en un objeto.
 */
public abstract class ArchivoCsv<T> {

    public static final String SEPARADOR = ";";

    /** Nombres de las columnas, en el orden en que se escriben. */
    protected abstract String[] columnas();

    /** Convierte un objeto en los valores de una fila (mismo orden que columnas()). */
    protected abstract String[] aFila(T objeto);

    /** Convierte los valores de una fila en un objeto; lanza ArchivoException si un dato no es valido. */
    protected abstract T desdeFila(String[] campos, int numeroLinea) throws ArchivoException;

    /** Escribe el encabezado y una fila por objeto; devuelve la cantidad de filas escritas. */
    public final int exportar(List<T> objetos, Path destino) throws ArchivoException {
        try {
            if (destino.getParent() != null) Files.createDirectories(destino.getParent());
            try (BufferedWriter out = Files.newBufferedWriter(destino, StandardCharsets.UTF_8)) {
                out.write(String.join(SEPARADOR, columnas()));
                out.newLine();
                for (T objeto : objetos) {
                    String[] fila = aFila(objeto);
                    for (int i = 0; i < fila.length; i++) fila[i] = limpiar(fila[i]);
                    out.write(String.join(SEPARADOR, fila));
                    out.newLine();
                }
            }
            return objetos.size();
        } catch (IOException e) {
            throw new ArchivoException("No se pudo escribir el archivo " + destino + ": " + e.getMessage(), e);
        }
    }

    /** Lee el archivo completo; si una sola linea es invalida no devuelve nada (todo o nada). */
    public final ArrayList<T> importar(Path origen) throws ArchivoException {
        ArrayList<T> objetos = new ArrayList<>();
        try (BufferedReader in = Files.newBufferedReader(origen, StandardCharsets.UTF_8)) {
            String encabezado = in.readLine();
            if (encabezado == null || !encabezado.replace("﻿", "").trim()
                    .equalsIgnoreCase(String.join(SEPARADOR, columnas()))) {
                throw new ArchivoException("El archivo " + origen.getFileName()
                        + " no tiene el encabezado esperado: " + String.join(SEPARADOR, columnas()));
            }
            String linea;
            int numero = 1;
            while ((linea = in.readLine()) != null) {
                numero++;
                if (linea.isBlank()) continue;
                String[] campos = linea.split(SEPARADOR, -1);
                if (campos.length != columnas().length) {
                    throw new ArchivoException("Linea " + numero + ": se esperaban " + columnas().length
                            + " columnas y hay " + campos.length + ".");
                }
                for (int i = 0; i < campos.length; i++) campos[i] = campos[i].trim();
                objetos.add(desdeFila(campos, numero));
            }
        } catch (NoSuchFileException e) {
            throw new ArchivoException("No existe el archivo " + origen + ".", e);
        } catch (IOException e) {
            throw new ArchivoException("No se pudo leer el archivo " + origen + ": " + e.getMessage(), e);
        }
        return objetos;
    }

    /** Evita que un texto con el separador o con saltos de linea rompa la estructura del archivo. */
    private static String limpiar(String valor) {
        return valor == null ? "" : valor.replace(SEPARADOR, ",").replace("\n", " ").replace("\r", " ");
    }
}
