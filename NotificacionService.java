package com.sulqui.servicio;

/**
 * Implementa el caso de uso incluido (<<include>>) "Enviar notificacion",
 * reutilizado tanto por la confirmacion/rechazo de una reserva como por
 * el registro de una sena/pago.
 * En este prototipo el envio se simula por consola; en una version
 * posterior se reemplaza por el envio real de un correo electronico.
 */
public class NotificacionService {

    public void enviarNotificacion(String destinatario, String mensaje) {
        System.out.println("[NOTIFICACION] Para: " + destinatario + " | " + mensaje);
    }
}
