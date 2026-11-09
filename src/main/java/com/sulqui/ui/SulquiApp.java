package com.sulqui.ui;

import com.sulqui.servicio.*;

import javax.swing.*;

/**
 * Punto de entrada de la aplicacion de escritorio Sulqui (Java Swing).
 * Crea una unica instancia de cada servicio (capa de control) y la comparte
 * entre las ventanas (capa de presentacion / clases boundary).
 */
public class SulquiApp {

    final NotificacionService notificaciones = new NotificacionService();
    final ReservaService reservas = new ReservaService(notificaciones);
    final ClienteService clientes = new ClienteService();
    final MenuService menus = new MenuService();
    final PagoService pagos = new PagoService(notificaciones);
    final AutenticacionService autenticacion = new AutenticacionService();
    final ReporteService reportes = new ReporteService();
    final ArchivoService archivos = new ArchivoService();

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("--consola")) {
            com.sulqui.app.MenuConsola.main(args);     // misma aplicacion, menu por consola
            return;
        }
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) UIManager.setLookAndFeel(info.getClassName());
            }
        } catch (Exception ignorada) {
            // Si Nimbus no esta disponible se usa el aspecto por defecto.
        }
        SulquiApp app = new SulquiApp();
        SwingUtilities.invokeLater(() -> new VentanaInicio(app).setVisible(true));
    }
}
