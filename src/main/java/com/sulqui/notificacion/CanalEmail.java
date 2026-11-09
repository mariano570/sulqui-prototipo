package com.sulqui.notificacion;

import com.sulqui.modelo.Cliente;

/**
 * Notificacion por correo electronico. En produccion se envia por SMTP
 * (puerto 587, STARTTLS); en el prototipo se simula por consola.
 */
public class CanalEmail extends CanalNotificacion {

    private final String remitente;

    public CanalEmail() {
        this("reservas.sulqui@example.com");
    }

    public CanalEmail(String remitente) {
        super("Email");
        this.remitente = remitente;
    }

    @Override
    protected String obtenerDestino(Cliente cliente) {
        return cliente.getEmail();
    }

    @Override
    protected void entregar(String destino, String texto) {
        System.out.println("[EMAIL] De: " + remitente + " | Para: " + destino + " | " + texto);
    }

    @Override
    protected String formatear(String mensaje) {
        return mensaje + " - Sulqui, eventos a fuego.";
    }
}
