package com.sulqui.servicio;

/** Se lanza cuando un canal no puede entregar una notificacion (por ejemplo, falta el destino). */
public class NotificacionException extends SulquiException {

    private static final long serialVersionUID = 1L;

    public NotificacionException(String mensaje) {
        super(mensaje);
    }

    @Override
    public String getTitulo() {
        return "Notificacion no enviada";
    }
}
