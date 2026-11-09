package com.sulqui.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

/** Utilidades visuales compartidas por todas las ventanas Swing. */
final class UiUtil {

    static final Color BRASA = new Color(0xB4, 0x45, 0x1F);
    static final Color CARBON = new Color(0x2B, 0x2B, 0x2B);
    static final Color FONDO = new Color(0xFA, 0xF6, 0xF1);
    static final Color OK = new Color(0x2E, 0x7D, 0x32);
    static final Color ERROR = new Color(0xC6, 0x28, 0x28);
    static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private UiUtil() { }

    /** Banda de titulo superior con el nombre del sistema y la pantalla actual. */
    static JComponent encabezado(String titulo, String subtitulo) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(CARBON);
        p.setBorder(new EmptyBorder(12, 18, 12, 18));
        JLabel t = new JLabel("Sulqui  ·  " + titulo);
        t.setForeground(Color.WHITE);
        t.setFont(t.getFont().deriveFont(Font.BOLD, 18f));
        JLabel s = new JLabel(subtitulo);
        s.setForeground(new Color(0xE8, 0xC8, 0xB0));
        p.add(t, BorderLayout.NORTH);
        p.add(s, BorderLayout.SOUTH);
        return p;
    }

    /** Selector de fecha con formato dd/MM/yyyy. */
    static JSpinner selectorFecha(LocalDate inicial) {
        Date d = Date.from(inicial.atStartOfDay(ZoneId.systemDefault()).toInstant());
        JSpinner sp = new JSpinner(new SpinnerDateModel(d, null, null, Calendar.DAY_OF_MONTH));
        sp.setEditor(new JSpinner.DateEditor(sp, "dd/MM/yyyy"));
        return sp;
    }

    static LocalDate fechaDe(JSpinner sp) {
        return ((Date) sp.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    static JButton botonPrimario(String texto) {
        JButton b = new JButton(texto);
        b.setBackground(BRASA);
        b.setForeground(Color.WHITE);
        b.setFont(b.getFont().deriveFont(Font.BOLD));
        return b;
    }

    /** Formulario de dos columnas (etiqueta / campo). */
    static JPanel formulario(Object... pares) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.WEST;
        for (int i = 0; i < pares.length; i += 2) {
            c.gridy = i / 2;
            c.gridx = 0; c.weightx = 0; c.fill = GridBagConstraints.NONE;
            p.add(new JLabel((String) pares[i]), c);
            c.gridx = 1; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
            p.add((Component) pares[i + 1], c);
        }
        return p;
    }

    /** Muestra un error inesperado; los de la base llegan ya clasificados (PersistenciaException). */
    static void error(Component padre, Exception e) {
        String titulo = e instanceof com.sulqui.dao.PersistenciaException p
                ? p.getTitulo() : "No se pudo completar la operación";
        JOptionPane.showMessageDialog(padre, e.getMessage(), titulo, JOptionPane.WARNING_MESSAGE);
    }
}
