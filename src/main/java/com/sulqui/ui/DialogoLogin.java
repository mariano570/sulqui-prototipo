package com.sulqui.ui;

import com.sulqui.modelo.Administrador;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** P-05 Inicio de sesion del administrador. */
public class DialogoLogin extends JDialog {

    private Administrador resultado;

    private DialogoLogin(Frame padre, SulquiApp app) {
        super(padre, "Ingreso de administrador", true);
        JTextField usuario = new JTextField(16);
        JPasswordField clave = new JPasswordField(16);
        JLabel mensaje = new JLabel(" ");
        mensaje.setForeground(UiUtil.ERROR);
        JButton ingresar = UiUtil.botonPrimario("Ingresar");
        ingresar.addActionListener(e -> {
            try {
                resultado = app.autenticacion.login(usuario.getText().trim(), clave.getPassword());
                if (resultado != null) dispose();
                else mensaje.setText("Usuario o contraseña incorrectos.");
            } catch (Exception ex) {
                UiUtil.error(this, ex);
            }
        });
        getRootPane().setDefaultButton(ingresar);

        JPanel form = UiUtil.formulario("Usuario:", usuario, "Contraseña:", clave);
        JPanel sur = new JPanel(new BorderLayout());
        sur.setOpaque(false);
        sur.add(mensaje, BorderLayout.CENTER);
        sur.add(ingresar, BorderLayout.EAST);
        JPanel cuerpo = new JPanel(new BorderLayout(0, 10));
        cuerpo.setBackground(UiUtil.FONDO);
        cuerpo.setBorder(new EmptyBorder(16, 18, 16, 18));
        cuerpo.add(form, BorderLayout.CENTER);
        cuerpo.add(sur, BorderLayout.SOUTH);

        setLayout(new BorderLayout());
        add(UiUtil.encabezado("Administración", "Ingrese sus credenciales"), BorderLayout.NORTH);
        add(cuerpo, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(padre);
    }

    static Administrador mostrar(Frame padre, SulquiApp app) {
        DialogoLogin d = new DialogoLogin(padre, app);
        d.setVisible(true);
        return d.resultado;
    }
}
