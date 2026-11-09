package com.sulqui.ui;

import com.sulqui.modelo.Administrador;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 * Genera las capturas de las pantallas reales (evidencia y prototipos de
 * interfaz) sobre la base de datos con los datos de ejemplo.
 * Uso: java ... com.sulqui.ui.CapturaPantallas <carpeta-destino>
 */
public class CapturaPantallas {

    static File destino;
    static Robot robot;

    public static void main(String[] args) throws Exception {
        destino = new File(args.length > 0 ? args[0] : "evidencia/pantallas");
        destino.mkdirs();
        robot = new Robot();
        for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            if ("Nimbus".equals(info.getName())) UIManager.setLookAndFeel(info.getClassName());
        }
        SulquiApp app = new SulquiApp();

        // P-01 Inicio
        VentanaInicio inicio = enEdt(() -> new VentanaInicio(app));
        mostrarYCapturar(inicio, "P01_inicio");

        // P-05 Login (modal: se abre en otro hilo y se captura desde aqui)
        new Thread(() -> SwingUtilities.invokeLater(() -> DialogoLogin.mostrar(inicio, app))).start();
        esperar(1200);
        JDialog login = null;
        for (Window w : Window.getWindows()) if (w instanceof DialogoLogin && w.isShowing()) login = (JDialog) w;
        final JDialog dl = login;
        SwingUtilities.invokeAndWait(() -> {
            JTextField u = buscar(dl.getContentPane(), JTextField.class, false);
            u.setText("mariano");
            buscar(dl.getContentPane(), JPasswordField.class, true).setText("sulqui2026");
        });
        capturar(dl, "P05_login");
        SwingUtilities.invokeAndWait(dl::dispose);
        SwingUtilities.invokeAndWait(inicio::dispose);

        // P-02 a P-04 Modo cliente
        VentanaCliente vc = enEdt(() -> new VentanaCliente(app));
        mostrar(vc);
        SwingUtilities.invokeAndWait(() -> {
            vc.fechaConsulta.setValue(fecha(2026, 11, 15));
            vc.consultarDisponibilidad();
        });
        capturar(vc, "P02_disponibilidad_ocupada");
        SwingUtilities.invokeAndWait(() -> {
            vc.fechaConsulta.setValue(fecha(2026, 12, 19));
            vc.consultarDisponibilidad();
        });
        capturar(vc, "P02_disponibilidad_libre");

        SwingUtilities.invokeAndWait(() -> {
            vc.pestanias.setSelectedIndex(1);
            vc.nombre.setText("Martina Rossi");
            vc.telefono.setText("3517005555");
            vc.email.setText("martina.rossi@example.com");
            vc.menu.setSelectedIndex(1);
            vc.fechaReserva.setValue(fecha(2026, 12, 19));
            vc.comensales.setValue(151);
            vc.enviarSolicitud();
        });
        capturar(vc, "P03_solicitud_error_151");
        SwingUtilities.invokeAndWait(() -> {
            vc.comensales.setValue(30);
            vc.enviarSolicitud();
        });
        capturar(vc, "P03_solicitud_ok");

        SwingUtilities.invokeAndWait(() -> {
            vc.pestanias.setSelectedIndex(2);
            vc.emailHistorial.setText("ana.gomez@example.com");
            vc.buscarHistorial();
        });
        capturar(vc, "P04_mis_reservas");
        SwingUtilities.invokeAndWait(vc::dispose);

        // P-06 a P-09 Administrador
        Administrador admin = app.autenticacion.login("mariano", "sulqui2026".toCharArray());
        VentanaAdministrador va = enEdt(() -> new VentanaAdministrador(app, admin));
        mostrar(va);
        SwingUtilities.invokeAndWait(() -> va.tablaPendientes.setRowSelectionInterval(0, 0));
        capturar(va, "P06_pendientes");
        // AP4: dialogo de lugar y horario (modal: se abre en otro hilo y se captura desde aqui)
        DialogoEvento[] dialogo = new DialogoEvento[1];
        SwingUtilities.invokeAndWait(() -> {
            dialogo[0] = va.dialogoParaSeleccionada();
            dialogo[0].direccion.setText("Bv. San Juan 800, Cordoba");
            dialogo[0].horario.setText("12:30");
        });
        new Thread(() -> SwingUtilities.invokeLater(() -> dialogo[0].setVisible(true))).start();
        esperar(1200);
        capturar(dialogo[0], "P06_datos_evento");
        SwingUtilities.invokeAndWait(dialogo[0]::aceptar);
        esperar(500);
        SwingUtilities.invokeAndWait(() -> va.confirmarSeleccionada(dialogo[0].getDireccion(), dialogo[0].getHorario()));
        capturar(va, "P06_confirmada");
        SwingUtilities.invokeAndWait(() -> {
            va.pestanias.setSelectedIndex(1);
            va.mes.setValue(11);
            va.anio.setValue(2026);
            va.cargarCalendario();
        });
        capturar(va, "P07_calendario");
        SwingUtilities.invokeAndWait(() -> {
            va.pestanias.setSelectedIndex(2);
            for (int i = 0; i < va.reservaSenia.getItemCount(); i++) {
                if (va.reservaSenia.getItemAt(i).startsWith("#3 ")) va.reservaSenia.setSelectedIndex(i);
            }
            va.montoSenia.setText("50000");
            va.medioSenia.setSelectedItem("Transferencia");
            va.registrarSenia();
        });
        capturar(va, "P08_senia");
        SwingUtilities.invokeAndWait(() -> va.pestanias.setSelectedIndex(3));
        capturar(va, "P09_menus");

        // P-10 Reportes y archivos (AP4)
        SwingUtilities.invokeAndWait(() -> {
            va.pestanias.setSelectedIndex(4);
            va.anioReporte.setValue(2026);
            va.cargarResumen();
            va.respaldarCatalogo();
        });
        capturar(va, "P10_reportes_respaldo");
        java.nio.file.Path invalido = java.nio.file.Files.createTempFile("catalogo_invalido", ".csv");
        java.nio.file.Files.writeString(invalido, "id_menu;nombre;precio_persona;tipo_preparacion\n"
                + "1;Asado clasico;9000;Asado\n2;Comida al disco;-5;Al disco\n");
        SwingUtilities.invokeAndWait(() -> va.restaurarCatalogo(invalido));
        capturar(va, "P10_restaurar_error");
        SwingUtilities.invokeAndWait(va::exportarReservas);
        capturar(va, "P10_exportar");
        SwingUtilities.invokeAndWait(va::dispose);
        System.out.println("Capturas generadas en " + destino.getAbsolutePath());
        System.exit(0);
    }

    static Date fecha(int a, int m, int d) {
        return Date.from(LocalDate.of(a, m, d).atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    interface Fabrica<T> { T crear(); }

    static <T> T enEdt(Fabrica<T> f) throws Exception {
        Object[] r = new Object[1];
        SwingUtilities.invokeAndWait(() -> r[0] = f.crear());
        @SuppressWarnings("unchecked") T t = (T) r[0];
        return t;
    }

    static void mostrar(Window w) throws Exception {
        SwingUtilities.invokeAndWait(() -> { w.setLocation(20, 20); w.setVisible(true); });
        esperar(900);
    }

    static void mostrarYCapturar(Window w, String nombre) throws Exception {
        mostrar(w);
        capturar(w, nombre);
    }

    static void capturar(Window w, String nombre) throws Exception {
        esperar(700);
        Rectangle r = w.getBounds();
        BufferedImage img = robot.createScreenCapture(r);
        ImageIO.write(img, "png", new File(destino, nombre + ".png"));
    }

    static void esperar(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignorada) { }
    }

    static <T extends Component> T buscar(Container c, Class<T> tipo, boolean exacto) {
        for (Component h : c.getComponents()) {
            if (exacto ? h.getClass() == tipo : (tipo.isInstance(h) && !(h instanceof JPasswordField))) return tipo.cast(h);
            if (h instanceof Container) {
                T t = buscar((Container) h, tipo, exacto);
                if (t != null) return t;
            }
        }
        return null;
    }
}
