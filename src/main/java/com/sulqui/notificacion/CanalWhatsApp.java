package com.sulqui.notificacion;

import com.sulqui.modelo.Cliente;

/**
 * Notificacion por WhatsApp, el canal que hoy usan los clientes de Sulqui.
 * El destino es el telefono con prefijo internacional de Argentina (+54).
 * En el prototipo se simula por consola.
 */
public class CanalWhatsApp extends CanalNotificacion {

    private static final String PREFIJO_PAIS = "+54";
    private static final int MAX_CARACTERES = 160;

    public CanalWhatsApp() {
        super("WhatsApp");
    }

    @Override
    protected String obtenerDestino(Cliente cliente) {
        String tel = cliente.getTelefono();
        return (tel == null || tel.isBlank()) ? null : PREFIJO_PAIS + tel;
    }

    @Override
    protected void entregar(String destino, String texto) {
        System.out.println("[WHATSAPP] " + destino + " | " + texto);
    }

    /** Mensaje breve: si supera 160 caracteres se recorta. */
    @Override
    protected String formatear(String mensaje) {
        String texto = "Sulqui: " + mensaje;
        return texto.length() <= MAX_CARACTERES ? texto : texto.substring(0, MAX_CARACTERES - 3) + "...";
    }
}
