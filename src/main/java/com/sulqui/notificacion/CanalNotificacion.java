package com.sulqui.notificacion;

import com.sulqui.modelo.Cliente;
import com.sulqui.servicio.NotificacionException;

/**
 * Abstraccion de un medio por el cual se avisa al cliente.
 *
 * Define el algoritmo comun de envio (metodo plantilla enviar) y deja
 * abstractos los pasos que cambian segun el canal: a que direccion se
 * envia y como se entrega. NotificacionService trabaja con una lista de
 * CanalNotificacion sin saber de que tipo concreto es cada uno (polimorfismo).
 */
public abstract class CanalNotificacion {

    private final String nombre;
    private int enviados;

    protected CanalNotificacion(String nombre) {
        this.nombre = nombre;
        this.enviados = 0;
    }

    /** Metodo plantilla: igual para todos los canales, no se puede redefinir (final). */
    public final String enviar(Cliente cliente, String mensaje) throws NotificacionException {
        String destino = obtenerDestino(cliente);
        if (destino == null || destino.isBlank()) {
            throw new NotificacionException("El cliente " + cliente.getNombre()
                    + " no tiene dato de contacto para el canal " + nombre + ".");
        }
        String texto = formatear(mensaje);
        entregar(destino, texto);
        enviados++;
        return "[" + nombre + "] Para: " + destino + " | " + texto;
    }

    /** Direccion de destino segun el canal (email, telefono, etc.). */
    protected abstract String obtenerDestino(Cliente cliente);

    /** Entrega efectiva del mensaje (en el prototipo, se simula por consola). */
    protected abstract void entregar(String destino, String texto);

    /** Formato por defecto: el mensaje sin cambios. Las subclases pueden redefinirlo. */
    protected String formatear(String mensaje) {
        return mensaje;
    }

    public String getNombre() { return nombre; }

    public int getEnviados() { return enviados; }
}
