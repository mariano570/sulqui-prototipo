package com.sulqui.servicio;

import com.sulqui.modelo.Cliente;
import com.sulqui.notificacion.CanalEmail;
import com.sulqui.notificacion.CanalNotificacion;
import com.sulqui.notificacion.CanalWhatsApp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementa el caso de uso incluido (<<include>>) "Enviar notificacion".
 *
 * Recorre los canales configurados y le pide a cada uno que envie el
 * mensaje. El servicio no sabe si el canal es email o WhatsApp: cada
 * objeto responde a enviar() a su manera (polimorfismo). Si un canal
 * falla, se registra el error y se sigue con los demas.
 */
public class NotificacionService {

    private final List<CanalNotificacion> canales;
    private final List<String> enviadas = new ArrayList<>();
    private final List<String> errores = new ArrayList<>();

    /** Canales por defecto: email y WhatsApp. */
    public NotificacionService() {
        this(new CanalEmail(), new CanalWhatsApp());
    }

    public NotificacionService(CanalNotificacion... canales) {
        this.canales = List.of(canales);
    }

    public void notificar(Cliente cliente, String mensaje) {
        for (CanalNotificacion canal : canales) {
            try {
                enviadas.add(canal.enviar(cliente, mensaje));
            } catch (NotificacionException e) {
                errores.add(e.getMessage());
                System.err.println("[AVISO] " + e.getTitulo() + ": " + e.getMessage());
            }
        }
    }

    public List<String> getEnviadas() { return Collections.unmodifiableList(enviadas); }

    public List<String> getErrores() { return Collections.unmodifiableList(errores); }

    public List<CanalNotificacion> getCanales() { return canales; }
}
