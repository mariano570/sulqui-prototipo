package com.sulqui.ui;

import com.sulqui.archivo.ArchivoException;
import com.sulqui.dao.PersistenciaException;
import com.sulqui.modelo.Administrador;
import com.sulqui.modelo.EstadoReserva;
import com.sulqui.modelo.Evento;
import com.sulqui.modelo.Cliente;
import com.sulqui.modelo.MenuPaquete;
import com.sulqui.modelo.Reserva;
import com.sulqui.servicio.ArchivoService;
import com.sulqui.servicio.PagoService;
import com.sulqui.servicio.ResumenAnual;
import com.sulqui.servicio.SulquiException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * Panel del administrador (clase boundary). Pestanias:
 *  P-06 Solicitudes pendientes (CU-05 confirmar/rechazar), P-07 Calendario (CU-04),
 *  P-08 Registrar senia (CU-07), P-09 Catalogo de menus (CU-08) y
 *  P-10 Reportes y archivos (AP4: resumen anual con arreglos, exportacion,
 *  respaldo y restauracion del catalogo en archivos CSV).
 */
public class VentanaAdministrador extends JFrame {

    private final SulquiApp app;
    private final Administrador admin;
    final JTabbedPane pestanias = new JTabbedPane();
    final JLabel estado = new JLabel(" ");

    final DefaultTableModel modeloPendientes = modelo("N.º", "Fecha", "Cliente", "Teléfono", "Menú", "Comensales", "Total estimado");
    final JTable tablaPendientes = new JTable(modeloPendientes);
    final JSpinner mes = new JSpinner(new SpinnerNumberModel(YearMonth.now().getMonthValue(), 1, 12, 1));
    final JSpinner anio = new JSpinner(new SpinnerNumberModel(YearMonth.now().getYear(), 2025, 2035, 1));
    final DefaultTableModel modeloCalendario = modelo("Fecha", "N.º", "Cliente", "Menú", "Comensales", "Horario", "Dirección");
    final JComboBox<String> reservaSenia = new JComboBox<>();
    final JTextField montoSenia = new JTextField(12);
    final JComboBox<String> medioSenia = new JComboBox<>(PagoService.MEDIOS_VALIDOS.stream().sorted().toArray(String[]::new));
    final DefaultTableModel modeloMenus = modelo("N.º", "Nombre", "Precio por persona", "Preparación");
    final JSpinner anioReporte = new JSpinner(new SpinnerNumberModel(YearMonth.now().getYear(), 2025, 2035, 1));
    final DefaultTableModel modeloResumen = modelo("Mes", "Eventos", "Comensales", "Facturación estimada");
    final JLabel totalesResumen = new JLabel(" ");
    final JLabel estadosResumen = new JLabel(" ");
    private static final NumberFormat PESOS = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR"));

    public VentanaAdministrador(SulquiApp app, Administrador admin) {
        super("Sulqui - Panel de administración");
        this.app = app;
        this.admin = admin;
        setSize(900, 540);
        setLocationRelativeTo(null);

        pestanias.addTab("Solicitudes pendientes", panelPendientes());
        pestanias.addTab("Calendario", panelCalendario());
        pestanias.addTab("Registrar seña", panelSenia());
        pestanias.addTab("Catálogo de menús", panelMenus());
        pestanias.addTab("Reportes y archivos", panelReportes());

        estado.setBorder(new EmptyBorder(6, 12, 6, 12));
        estado.setFont(estado.getFont().deriveFont(Font.BOLD));
        JPanel raiz = new JPanel(new BorderLayout());
        raiz.add(UiUtil.encabezado("Administración", "Sesión iniciada: " + admin.getUsuario()), BorderLayout.NORTH);
        raiz.add(pestanias, BorderLayout.CENTER);
        raiz.add(estado, BorderLayout.SOUTH);
        setContentPane(raiz);
        refrescarTodo();
    }

    private static DefaultTableModel modelo(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    private JPanel contenedor() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(UiUtil.FONDO);
        p.setBorder(new EmptyBorder(12, 14, 12, 14));
        return p;
    }

    void refrescarTodo() {
        cargarPendientes();
        cargarCalendario();
        cargarConfirmadasParaSenia();
        cargarMenus();
        cargarResumen();
    }

    // ---------- P-06 Solicitudes pendientes ----------
    private JPanel panelPendientes() {
        JButton confirmar = UiUtil.botonPrimario("Confirmar");
        JButton rechazar = new JButton("Rechazar…");
        confirmar.addActionListener(e -> confirmarSeleccionada());
        rechazar.addActionListener(e -> {
            String motivo = JOptionPane.showInputDialog(this, "Motivo del rechazo:", "Fecha no disponible");
            if (motivo != null) rechazarSeleccionada(motivo);
        });
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.setOpaque(false);
        botones.add(rechazar);
        botones.add(confirmar);
        JPanel p = contenedor();
        p.add(new JScrollPane(tablaPendientes), BorderLayout.CENTER);
        p.add(botones, BorderLayout.SOUTH);
        return p;
    }

    private Integer idSeleccionado() {
        int fila = tablaPendientes.getSelectedRow();
        if (fila < 0) {
            mostrar(false, "Seleccione una solicitud de la tabla.");
            return null;
        }
        return (Integer) modeloPendientes.getValueAt(fila, 0);
    }

    /** Pide lugar y horario en un dialogo y confirma la reserva seleccionada. */
    void confirmarSeleccionada() {
        DialogoEvento dialogo = dialogoParaSeleccionada();
        if (dialogo == null) return;
        dialogo.setVisible(true);                       // modal: espera a que el usuario responda
        if (dialogo.isAceptado()) confirmarSeleccionada(dialogo.getDireccion(), dialogo.getHorario());
    }

    DialogoEvento dialogoParaSeleccionada() {
        Integer id = idSeleccionado();
        if (id == null) return null;
        int fila = tablaPendientes.getSelectedRow();
        return new DialogoEvento(this, "Reserva N.º " + id + " — " + modeloPendientes.getValueAt(fila, 1)
                + " — " + modeloPendientes.getValueAt(fila, 2) + " (" + modeloPendientes.getValueAt(fila, 5) + " comensales)");
    }

    /** Confirma la reserva y registra el evento en una sola transaccion (ReservaService). */
    void confirmarSeleccionada(String direccion, LocalTime horario) {
        Integer id = idSeleccionado();
        if (id == null) return;
        try {
            app.reservas.confirmarReserva(id, admin.getIdAdmin(), direccion, horario);
            mostrar(true, "Reserva N.º " + id + " confirmada para las " + horario + " hs en " + direccion
                    + "; cliente notificado.");
            refrescarTodo();
        } catch (SulquiException ex) {
            mostrar(false, ex.getMessage());
        } catch (PersistenciaException ex) {
            mostrar(false, ex.getTitulo() + " — " + ex.getMessage() + " La reserva sigue pendiente.");
        } catch (Exception ex) {
            UiUtil.error(this, ex);
        }
    }

    void rechazarSeleccionada(String motivo) {
        Integer id = idSeleccionado();
        if (id == null) return;
        try {
            app.reservas.rechazarReserva(id, admin.getIdAdmin(), motivo);
            mostrar(true, "Reserva N.º " + id + " rechazada y cliente notificado.");
            refrescarTodo();
        } catch (SulquiException ex) {
            mostrar(false, ex.getMessage());
        } catch (Exception ex) {
            UiUtil.error(this, ex);
        }
    }

    private void cargarPendientes() {
        try {
            modeloPendientes.setRowCount(0);
            for (Reserva r : app.reservas.listarPendientes()) {
                Cliente c = app.clientes.buscar(r.getIdCliente());
                MenuPaquete m = app.menus.obtenerMenu(r.getIdMenu());
                modeloPendientes.addRow(new Object[]{r.getIdReserva(), r.getFecha().format(UiUtil.FMT),
                        c.getNombre(), c.getTelefono(), m.getNombre(), r.getCantComensales(),
                        "$ " + app.reservas.calcularTotal(r.getIdMenu(), r.getCantComensales())});
            }
        } catch (Exception ex) {
            UiUtil.error(this, ex);
        }
    }

    // ---------- P-07 Calendario ----------
    private JPanel panelCalendario() {
        JButton ver = UiUtil.botonPrimario("Ver mes");
        ver.addActionListener(e -> cargarCalendario());
        anio.setEditor(new JSpinner.NumberEditor(anio, "#"));
        JPanel filtro = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        filtro.setOpaque(false);
        filtro.add(new JLabel("Mes:"));
        filtro.add(mes);
        filtro.add(new JLabel("Año:"));
        filtro.add(anio);
        filtro.add(ver);
        JPanel p = contenedor();
        p.add(filtro, BorderLayout.NORTH);
        JTable tabla = new JTable(modeloCalendario);
        int[] anchos = {110, 40, 120, 130, 85, 65, 250};          // la direccion necesita mas espacio
        for (int i = 0; i < anchos.length; i++) tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        p.add(new JScrollPane(tabla), BorderLayout.CENTER);
        return p;
    }

    void cargarCalendario() {
        try {
            modeloCalendario.setRowCount(0);
            YearMonth ym = YearMonth.of((Integer) anio.getValue(), (Integer) mes.getValue());
            for (Reserva r : app.reservas.calendarioDelMes(ym)) {
                Evento ev = app.reservas.buscarEvento(r.getIdReserva());
                modeloCalendario.addRow(new Object[]{
                        r.getFecha().getDayOfWeek().getDisplayName(TextStyle.SHORT, new Locale("es")) + " "
                                + r.getFecha().format(UiUtil.FMT),
                        r.getIdReserva(), app.clientes.buscar(r.getIdCliente()).getNombre(),
                        app.menus.obtenerMenu(r.getIdMenu()).getNombre(), r.getCantComensales(),
                        ev == null ? "—" : ev.getHorario().toString(), ev == null ? "a definir" : ev.getDireccion()});
            }
        } catch (Exception ex) {
            UiUtil.error(this, ex);
        }
    }

    // ---------- P-08 Registrar senia ----------
    private JPanel panelSenia() {
        JButton registrar = UiUtil.botonPrimario("Registrar seña");
        registrar.addActionListener(e -> registrarSenia());
        JPanel p = contenedor();
        p.add(UiUtil.formulario(
                "Reserva confirmada:", reservaSenia,
                "Monto ($):", montoSenia,
                "Medio de pago:", medioSenia,
                "", registrar), BorderLayout.NORTH);
        return p;
    }

    void registrarSenia() {
        try {
            String sel = (String) reservaSenia.getSelectedItem();
            if (sel == null) { mostrar(false, "No hay reservas confirmadas."); return; }
            int id = Integer.parseInt(sel.substring(sel.indexOf('#') + 1, sel.indexOf(' ')));
            BigDecimal monto = new BigDecimal(montoSenia.getText().trim().replace(",", "."));
            app.pagos.registrarPago(id, monto, (String) medioSenia.getSelectedItem());
            mostrar(true, "Seña de $ " + monto + " registrada para la reserva N.º " + id + ".");
        } catch (NumberFormatException ex) {
            mostrar(false, "El monto debe ser un número (ej.: 30000).");
        } catch (SulquiException ex) {
            mostrar(false, ex.getMessage());
        } catch (Exception ex) {
            UiUtil.error(this, ex);
        }
    }

    private void cargarConfirmadasParaSenia() {
        try {
            reservaSenia.removeAllItems();
            for (Reserva r : app.reservas.listarConfirmadas()) {
                reservaSenia.addItem("#" + r.getIdReserva() + " " + r.getFecha().format(UiUtil.FMT)
                        + " - " + app.clientes.buscar(r.getIdCliente()).getNombre());
            }
        } catch (Exception ex) {
            UiUtil.error(this, ex);
        }
    }

    // ---------- P-09 Catalogo de menus ----------
    private JPanel panelMenus() {
        JButton nuevo = UiUtil.botonPrimario("Nuevo menú…");
        nuevo.addActionListener(e -> nuevoMenu());
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        botones.setOpaque(false);
        botones.add(nuevo);
        JPanel p = contenedor();
        p.add(new JScrollPane(new JTable(modeloMenus)), BorderLayout.CENTER);
        p.add(botones, BorderLayout.SOUTH);
        return p;
    }

    private void nuevoMenu() {
        JTextField n = new JTextField(16);
        JTextField precio = new JTextField(10);
        JComboBox<String> tipo = new JComboBox<>(new String[]{"Asado", "Al disco", "Otro"});
        int op = JOptionPane.showConfirmDialog(this, UiUtil.formulario("Nombre:", n, "Precio por persona:", precio,
                "Preparación:", tipo), "Nuevo menú", JOptionPane.OK_CANCEL_OPTION);
        if (op != JOptionPane.OK_OPTION) return;
        try {
            app.menus.agregarMenu(n.getText(), new BigDecimal(precio.getText().trim()), (String) tipo.getSelectedItem());
            mostrar(true, "Menú agregado al catálogo.");
            cargarMenus();
        } catch (NumberFormatException ex) {
            mostrar(false, "El precio debe ser un número.");
        } catch (SulquiException ex) {
            mostrar(false, ex.getMessage());
        } catch (Exception ex) {
            UiUtil.error(this, ex);
        }
    }

    private void cargarMenus() {
        try {
            modeloMenus.setRowCount(0);
            for (MenuPaquete m : app.menus.listarMenus()) {
                modeloMenus.addRow(new Object[]{m.getIdMenu(), m.getNombre(), "$ " + m.getPrecioPorPersona(),
                        m.getTipoPreparacion()});
            }
        } catch (Exception ex) {
            UiUtil.error(this, ex);
        }
    }

    // ---------- P-10 Reportes y archivos (AP4) ----------
    private JPanel panelReportes() {
        JButton generar = UiUtil.botonPrimario("Generar resumen");
        generar.addActionListener(e -> cargarResumen());
        anioReporte.setEditor(new JSpinner.NumberEditor(anioReporte, "#"));
        JPanel filtro = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        filtro.setOpaque(false);
        filtro.add(new JLabel("Año:"));
        filtro.add(anioReporte);
        filtro.add(generar);

        JButton exportar = new JButton("Exportar reservas (CSV)");
        JButton respaldar = new JButton("Respaldar catálogo");
        JButton restaurar = new JButton("Restaurar catálogo…");
        exportar.addActionListener(e -> exportarReservas());
        respaldar.addActionListener(e -> respaldarCatalogo());
        restaurar.addActionListener(e -> elegirYRestaurar());
        JPanel archivos = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        archivos.setOpaque(false);
        archivos.add(new JLabel("Archivos:"));
        archivos.add(exportar);
        archivos.add(respaldar);
        archivos.add(restaurar);

        totalesResumen.setFont(totalesResumen.getFont().deriveFont(Font.BOLD));
        JPanel pie = new JPanel(new GridLayout(3, 1, 0, 4));
        pie.setOpaque(false);
        pie.add(totalesResumen);
        pie.add(estadosResumen);
        pie.add(archivos);

        JPanel p = contenedor();
        p.add(filtro, BorderLayout.NORTH);
        p.add(new JScrollPane(new JTable(modeloResumen)), BorderLayout.CENTER);
        p.add(pie, BorderLayout.SOUTH);
        return p;
    }

    /** Llena la tabla con los arreglos del ResumenAnual (un casillero por mes). */
    void cargarResumen() {
        try {
            ResumenAnual r = app.reportes.resumenAnual((Integer) anioReporte.getValue());
            int[] eventos = r.getEventos();
            int[] comensales = r.getComensales();
            BigDecimal[] facturacion = r.getFacturacion();
            modeloResumen.setRowCount(0);
            for (int i = 0; i < ResumenAnual.MESES; i++) {
                modeloResumen.addRow(new Object[]{ResumenAnual.nombreMes(i + 1), eventos[i], comensales[i],
                        PESOS.format(facturacion[i])});
            }
            totalesResumen.setText("Total " + r.getAnio() + ": " + r.totalEventos() + " eventos, "
                    + r.totalComensales() + " comensales, " + PESOS.format(r.totalFacturacion())
                    + (r.totalEventos() > 0 ? "  ·  Mes de mayor demanda: " + ResumenAnual.nombreMes(r.mesDeMayorDemanda()) : ""));
            int[] porEstado = app.reportes.reservasPorEstado();
            StringBuilder sb = new StringBuilder("Reservas por estado: ");
            for (EstadoReserva e : EstadoReserva.values()) {
                sb.append(e).append(' ').append(porEstado[e.ordinal()]).append("   ");
            }
            estadosResumen.setText(sb.toString().trim());
        } catch (Exception ex) {
            UiUtil.error(this, ex);
        }
    }

    void exportarReservas() {
        try {
            Path destino = ArchivoService.archivoReservasPorDefecto();
            int n = app.archivos.exportarReservas(destino);
            mostrar(true, n + " reservas exportadas a " + destino);
        } catch (ArchivoException ex) {
            mostrar(false, ex.getMessage());
        } catch (Exception ex) {
            UiUtil.error(this, ex);
        }
    }

    void respaldarCatalogo() {
        try {
            Path destino = ArchivoService.archivoRespaldoPorDefecto();
            int n = app.archivos.respaldarCatalogo(destino);
            mostrar(true, n + " menús respaldados en " + destino);
        } catch (ArchivoException ex) {
            mostrar(false, ex.getMessage());
        } catch (Exception ex) {
            UiUtil.error(this, ex);
        }
    }

    private void elegirYRestaurar() {
        JFileChooser selector = new JFileChooser(ArchivoService.archivoRespaldoPorDefecto().toAbsolutePath().getParent().toFile());
        selector.setDialogTitle("Restaurar catálogo de menús");
        if (selector.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            restaurarCatalogo(selector.getSelectedFile().toPath());
        }
    }

    /** Restaura el catalogo (todo o nada) y refresca las pantallas. */
    void restaurarCatalogo(Path origen) {
        try {
            ArchivoService.ResultadoRestauracion r = app.archivos.restaurarCatalogo(origen);
            mostrar(true, "Catálogo restaurado desde " + origen.getFileName() + ": " + r.actualizados()
                    + " menús actualizados y " + r.agregados() + " agregados.");
            refrescarTodo();
        } catch (ArchivoException ex) {
            mostrar(false, ex.getMessage() + " No se modificó el catálogo.");
        } catch (PersistenciaException ex) {
            mostrar(false, ex.getTitulo() + " — " + ex.getMessage() + " Se deshicieron todos los cambios.");
        } catch (Exception ex) {
            UiUtil.error(this, ex);
        }
    }

    private void mostrar(boolean ok, String texto) {
        estado.setForeground(ok ? UiUtil.OK : UiUtil.ERROR);
        estado.setText((ok ? "✔ " : "✖ ") + texto);
    }
}
