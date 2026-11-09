package com.sulqui.modelo;

/** Estados posibles de una reserva (coinciden con el CHECK chk_estado de la tabla reserva). */
public enum EstadoReserva {
    PENDIENTE("Pendiente"),
    CONFIRMADA("Confirmada"),
    RECHAZADA("Rechazada"),
    CANCELADA("Cancelada"),
    FINALIZADA("Finalizada");

    private final String valorBD;

    EstadoReserva(String valorBD) { this.valorBD = valorBD; }

    public String getValorBD() { return valorBD; }

    public static EstadoReserva desdeBD(String valor) {
        for (EstadoReserva e : values()) {
            if (e.valorBD.equalsIgnoreCase(valor)) return e;
        }
        throw new IllegalArgumentException("Estado desconocido: " + valor);
    }

    @Override
    public String toString() { return valorBD; }
}
