package com.sulqui.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Dialogo modal que pide la direccion y el horario del evento al confirmar
 * una reserva (AP4). Con esos datos ReservaService confirma la reserva y
 * registra el evento en una misma transaccion.
 */
class DialogoEvento extends JDialog {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    final JTextField direccion = new JTextField(26);
    final JTextField horario = new JTextField("13:00", 6);
    private final JLabel aviso = new JLabel(" ");
    private boolean aceptado;

    DialogoEvento(Window padre, String descripcionReserva) {
        super(padre, "Confirmar reserva", ModalityType.APPLICATION_MODAL);
        JButton aceptar = UiUtil.botonPrimario("Confirmar");
        JButton cancelar = new JButton("Cancelar");
        aceptar.addActionListener(e -> aceptar());
        cancelar.addActionListener(e -> dispose());
        aviso.setForeground(UiUtil.ERROR);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.setOpaque(false);
        botones.add(cancelar);
        botones.add(aceptar);

        JPanel cuerpo = new JPanel(new BorderLayout(0, 8));
        cuerpo.setBackground(UiUtil.FONDO);
        cuerpo.setBorder(new EmptyBorder(12, 14, 12, 14));
        cuerpo.add(new JLabel(descripcionReserva), BorderLayout.NORTH);
        cuerpo.add(UiUtil.formulario("Dirección del evento:", direccion, "Horario (hh:mm):", horario,
                "", aviso), BorderLayout.CENTER);
        cuerpo.add(botones, BorderLayout.SOUTH);

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.add(UiUtil.encabezado("Confirmar reserva", "Lugar y horario del evento"), BorderLayout.NORTH);
        raiz.add(cuerpo, BorderLayout.CENTER);
        setContentPane(raiz);
        getRootPane().setDefaultButton(aceptar);
        pack();
        setLocationRelativeTo(padre);
    }

    /** Valida el formulario; si es correcto cierra el dialogo marcandolo como aceptado. */
    void aceptar() {
        if (direccion.getText().isBlank()) {
            aviso.setText("Ingrese la dirección del evento.");
            return;
        }
        if (getHorario() == null) {
            aviso.setText("El horario debe tener el formato hh:mm (ej.: 13:00).");
            return;
        }
        aceptado = true;
        dispose();
    }

    boolean isAceptado() { return aceptado; }

    String getDireccion() { return direccion.getText().trim(); }

    LocalTime getHorario() {
        try {
            return LocalTime.parse(horario.getText().trim(), HORA);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
