package com.sulqui.archivo;

import com.sulqui.modelo.EstadoReserva;
import com.sulqui.modelo.Reserva;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/** Formato del listado de reservas exportado para planilla de calculo. */
public class ReservaCsv extends ArchivoCsv<Reserva> {

    @Override
    protected String[] columnas() {
        return new String[]{"id_reserva", "fecha", "id_cliente", "id_menu", "comensales", "estado"};
    }

    @Override
    protected String[] aFila(Reserva r) {
        return new String[]{String.valueOf(r.getIdReserva()), r.getFecha().toString(),
                String.valueOf(r.getIdCliente()), String.valueOf(r.getIdMenu()),
                String.valueOf(r.getCantComensales()), r.getEstado().getValorBD()};
    }

    @Override
    protected Reserva desdeFila(String[] c, int linea) throws ArchivoException {
        try {
            Reserva r = new Reserva(Integer.parseInt(c[2]), Integer.parseInt(c[3]),
                    LocalDate.parse(c[1]), Integer.parseInt(c[4]));
            r.setIdReserva(Integer.parseInt(c[0]));
            r.setEstado(EstadoReserva.desdeBD(c[5]));
            return r;
        } catch (NumberFormatException | DateTimeParseException e) {
            throw new ArchivoException("Linea " + linea + ": dato con formato invalido (" + e.getMessage() + ").", e);
        } catch (IllegalArgumentException e) {
            throw new ArchivoException("Linea " + linea + ": " + e.getMessage(), e);
        }
    }
}
