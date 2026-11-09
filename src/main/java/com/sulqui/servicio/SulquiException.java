package com.sulqui.servicio;

/**
 * Raiz de la jerarquia de excepciones propias del sistema (excepcion verificada).
 * Es abstracta: nunca se lanza directamente, sino alguna de sus subclases.
 * Permite que la interfaz capture cualquier error de negocio con un unico
 * bloque catch (SulquiException e), aprovechando el polimorfismo.
 */
public abstract class SulquiException extends Exception {

    private static final long serialVersionUID = 1L;

    protected SulquiException(String mensaje) {
        super(mensaje);
    }

    /** Titulo breve para mostrar al usuario; cada subclase lo redefine. */
    public abstract String getTitulo();
}
