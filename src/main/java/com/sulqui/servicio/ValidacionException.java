package com.sulqui.servicio;

/** Se lanza cuando un dato ingresado viola una regla de negocio (RN-01 a RN-05). */
public class ValidacionException extends SulquiException {

    private static final long serialVersionUID = 1L;

    public ValidacionException(String mensaje) {
        super(mensaje);
    }

    @Override
    public String getTitulo() {
        return "Dato no valido";
    }
}
