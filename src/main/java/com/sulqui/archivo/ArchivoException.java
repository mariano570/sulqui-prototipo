package com.sulqui.archivo;

import com.sulqui.servicio.SulquiException;

/**
 * Error al leer o escribir un archivo (no existe, no se puede escribir o una
 * linea tiene un formato invalido). Forma parte de la jerarquia SulquiException,
 * por lo que el menu y las ventanas la muestran sin cerrar el programa.
 */
public class ArchivoException extends SulquiException {

    private static final long serialVersionUID = 1L;

    public ArchivoException(String mensaje) {
        super(mensaje);
    }

    public ArchivoException(String mensaje, Throwable causa) {
        super(mensaje);
        initCause(causa);
    }

    @Override
    public String getTitulo() {
        return "Error de archivo";
    }
}
