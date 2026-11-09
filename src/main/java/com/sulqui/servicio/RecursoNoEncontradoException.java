package com.sulqui.servicio;

/** Se lanza cuando se busca un cliente, menu o reserva que no existe. */
public class RecursoNoEncontradoException extends SulquiException {

    private static final long serialVersionUID = 1L;

    private final String recurso;
    private final Object identificador;

    public RecursoNoEncontradoException(String recurso, Object identificador) {
        super("No existe " + recurso + " con identificador " + identificador + ".");
        this.recurso = recurso;
        this.identificador = identificador;
    }

    public String getRecurso() { return recurso; }

    public Object getIdentificador() { return identificador; }

    @Override
    public String getTitulo() {
        return "No encontrado";
    }
}
