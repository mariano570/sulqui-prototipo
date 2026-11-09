package com.sulqui.ui;

import com.sulqui.modelo.Cliente;
import com.sulqui.modelo.MenuPaquete;
import com.sulqui.modelo.Reserva;
import com.sulqui.servicio.SulquiException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;

/**
 * Modo Cliente de la terminal (clase boundary). Tres pestanias:
 *  P-02 Consultar disponibilidad (CU-01), P-03 Solicitar reserva (CU-02, CU-03 seleccionar menu)
 *  y P-04 Mis reservas (CU-06 historial).
 */
public class VentanaCliente extends JFrame {

    private static final NumberFormat PESOS = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));

    private final SulquiApp app;
    final JTabbedPane pestanias = new JTabbedPane();

    // P-02
    final JSpinner fechaConsulta = UiUtil.selectorFecha(LocalDate.now().plusDays(30));
    final JLabel resultadoConsulta = new JLabel(" ");
    // P-03
    final JTextField nombre = new JTextField(22);
    final JTextField telefono = new JTextField(22);
    final JTextField email = new JTextField(22);
    final JComboBox<MenuPaquete> menu = new JComboBox<>();
    final JSpinner fechaReserva = UiUtil.selectorFecha(LocalDate.now().plusDays(30));
    final JSpinner comensales = new JSpinner(new SpinnerNumberModel(20, 0, 999, 1));
    final JLabel total = new JLabel("-");
    final JLabel estadoSolicitud = new JLabel(" ");
    // P-04
    final JTextField emailHistorial = new JTextField(22);
    final DefaultTableModel modeloHistorial =
            new DefaultTableModel(new String[]{"N.º", "Fecha", "Menú", "Comensales", "Estado"}, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };

    public VentanaCliente(SulquiApp app) {
        super("Sulqui - Terminal de clientes");
        this.app = app;
        setSize(640, 470);
        setLocationRelativeTo(null);

        pestanias.addTab("1. Consultar fecha", panelDisponibilidad());
        pestanias.addTab("2. Solicitar reserva", panelSolicitud());
        pestanias.addTab("3. Mis reservas", panelHistorial());

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.add(UiUtil.encabezado("Reservas", "Consultá disponibilidad y pedí tu evento a fuego"), BorderLayout.NORTH);
        raiz.add(pestanias, BorderLayout.CENTER);
        setContentPane(raiz);
        cargarMenus();
    }

    private JPanel contenedor(JComponent contenido) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UiUtil.FONDO);
        p.setBorder(new EmptyBorder(16, 20, 16, 20));
        p.add(contenido, BorderLayout.NORTH);
        return p;
    }

    // ---------- P-02 Consultar disponibilidad ----------
    private JPanel panelDisponibilidad() {
        JButton consultar = UiUtil.botonPrimario("Consultar");
        JButton reservar = new JButton("Reservar esta fecha →");
        consultar.addActionListener(e -> consultarDisponibilidad());
        reservar.addActionListener(e -> {
            fechaReserva.setValue(fechaConsulta.getValue());
            pestanias.setSelectedIndex(1);
        });
        resultadoConsulta.setFont(resultadoConsulta.getFont().deriveFont(Font.BOLD, 14f));
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        acciones.setOpaque(false);
        acciones.add(consultar);
        acciones.add(Box.createHorizontalStrut(8));
        acciones.add(reservar);
        return contenedor(UiUtil.formulario(
                "Fecha del evento:", fechaConsulta,
                "", acciones,
                "Resultado:", resultadoConsulta));
    }

    void consultarDisponibilidad() {
        try {
            LocalDate f = UiUtil.fechaDe(fechaConsulta);
            boolean libre = app.reservas.consultarDisponibilidad(f);
            resultadoConsulta.setForeground(libre ? UiUtil.OK : UiUtil.ERROR);
            resultadoConsulta.setText(libre
                    ? "✔ El " + f.format(UiUtil.FMT) + " está disponible."
                    : "✖ El " + f.format(UiUtil.FMT) + " ya tiene un evento confirmado.");
        } catch (Exception ex) {
            UiUtil.error(this, ex);
        }
    }

    // ---------- P-03 Solicitar reserva ----------
    private JPanel panelSolicitud() {
        menu.addActionListener(e -> actualizarTotal());
        comensales.addChangeListener(e -> actualizarTotal());
        JLabel ayuda = new JLabel("Entre 10 y 150 comensales");
        ayuda.setForeground(Color.GRAY);
        JPanel cant = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        cant.setOpaque(false);
        cant.add(comensales);
        cant.add(Box.createHorizontalStrut(10));
        cant.add(ayuda);
        total.setFont(total.getFont().deriveFont(Font.BOLD, 14f));
        JButton enviar = UiUtil.botonPrimario("Enviar solicitud");
        enviar.addActionListener(e -> enviarSolicitud());
        estadoSolicitud.setFont(estadoSolicitud.getFont().deriveFont(Font.BOLD));
        return contenedor(UiUtil.formulario(
                "Nombre y apellido:", nombre,
                "Teléfono:", telefono,
                "Email:", email,
                "Menú:", menu,
                "Fecha del evento:", fechaReserva,
                "Comensales:", cant,
                "Total estimado:", total,
                "", enviar,
                "", estadoSolicitud));
    }

    void enviarSolicitud() {
        try {
            int idCliente = app.clientes.registrarCliente(nombre.getText(), telefono.getText().trim(), email.getText());
            MenuPaquete m = (MenuPaquete) menu.getSelectedItem();
            int id = app.reservas.solicitarReserva(idCliente, m.getIdMenu(),
                    UiUtil.fechaDe(fechaReserva), (Integer) comensales.getValue());
            estadoSolicitud.setForeground(UiUtil.OK);
            estadoSolicitud.setText("✔ Solicitud N.º " + id + " recibida (Pendiente). Te avisaremos por email.");
        } catch (SulquiException ex) {
            estadoSolicitud.setForeground(UiUtil.ERROR);
            estadoSolicitud.setText("✖ " + ex.getMessage());
        } catch (Exception ex) {
            UiUtil.error(this, ex);
        }
    }

    private void actualizarTotal() {
        MenuPaquete m = (MenuPaquete) menu.getSelectedItem();
        if (m == null) return;
        BigDecimal t = m.getPrecioPorPersona().multiply(BigDecimal.valueOf((Integer) comensales.getValue()));
        total.setText(PESOS.format(t));
    }

    private void cargarMenus() {
        try {
            for (MenuPaquete m : app.menus.listarMenus()) menu.addItem(m);
            actualizarTotal();
        } catch (Exception ex) {
            UiUtil.error(this, ex);
        }
    }

    // ---------- P-04 Mis reservas ----------
    private JPanel panelHistorial() {
        JButton buscar = UiUtil.botonPrimario("Buscar");
        buscar.addActionListener(e -> buscarHistorial());
        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        fila.setOpaque(false);
        fila.add(emailHistorial);
        fila.add(Box.createHorizontalStrut(8));
        fila.add(buscar);
        JPanel p = contenedor(UiUtil.formulario("Tu email:", fila));
        p.add(new JScrollPane(new JTable(modeloHistorial)), BorderLayout.CENTER);
        return p;
    }

    void buscarHistorial() {
        try {
            modeloHistorial.setRowCount(0);
            Cliente c = app.clientes.buscar(emailHistorial.getText().trim());
            if (c == null) return;
            for (Reserva r : app.clientes.obtenerReservas(c.getIdCliente())) {
                modeloHistorial.addRow(new Object[]{r.getIdReserva(), r.getFecha().format(UiUtil.FMT),
                        app.menus.obtenerMenu(r.getIdMenu()).getNombre(), r.getCantComensales(), r.getEstado()});
            }
        } catch (Exception ex) {
            UiUtil.error(this, ex);
        }
    }
}
