package com.sulqui.ui;

import com.sulqui.modelo.Administrador;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** P-01 Pantalla de inicio de la terminal: elige modo Cliente o Administrador. */
public class VentanaInicio extends JFrame {

    public VentanaInicio(SulquiApp app) {
        super("Sulqui - Reservas de eventos a fuego");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(560, 360);
        setLocationRelativeTo(null);

        JPanel centro = new JPanel(new GridLayout(1, 2, 18, 0));
        centro.setBackground(UiUtil.FONDO);
        centro.setBorder(new EmptyBorder(30, 30, 30, 30));

        JButton cliente = tarjeta("Quiero reservar", "Consultar fechas, pedir una reserva y ver mis reservas");
        JButton admin = tarjeta("Administración", "Acceso exclusivo con usuario y contraseña");
        cliente.addActionListener(e -> new VentanaCliente(app).setVisible(true));
        admin.addActionListener(e -> {
            Administrador a = DialogoLogin.mostrar(this, app);
            if (a != null) new VentanaAdministrador(app, a).setVisible(true);
        });
        centro.add(cliente);
        centro.add(admin);

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.add(UiUtil.encabezado("Bienvenido", "Servicio gastronómico a fuego para eventos"), BorderLayout.NORTH);
        raiz.add(centro, BorderLayout.CENTER);
        setContentPane(raiz);
    }

    private JButton tarjeta(String titulo, String detalle) {
        JButton b = new JButton("<html><center><b style='font-size:15px'>" + titulo
                + "</b><br><br><span style='font-size:10px'>" + detalle + "</span></center></html>");
        b.setBackground(Color.WHITE);
        b.setFocusPainted(false);
        return b;
    }
}
