package com.sulqui.app;

import com.sulqui.archivo.ArchivoException;
import com.sulqui.dao.PersistenciaException;
import com.sulqui.estructuras.Cola;
import com.sulqui.estructuras.EstructuraVaciaException;
import com.sulqui.estructuras.ListaEnlazada;
import com.sulqui.modelo.Administrador;
import com.sulqui.modelo.Cliente;
import com.sulqui.modelo.EstadoReserva;
import com.sulqui.modelo.Evento;
import com.sulqui.modelo.MenuPaquete;
import com.sulqui.modelo.Reserva;
import com.sulqui.servicio.*;

import java.io.InputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Menu de seleccion por consola de Sulqui.
 *
 * Ofrece las mismas funciones que la interfaz Swing, organizadas en un menu
 * principal (cliente) y un submenu de administracion con reportes y archivos. Toda entrada se valida
 * en bucles que se repiten hasta recibir un dato correcto, y cada opcion se
 * ejecuta dentro de un bloque try/catch para que un error nunca cierre el programa.
 *
 * Ejecucion: java -cp sulqui-app.jar com.sulqui.app.MenuConsola
 *        o:  java -jar sulqui-app.jar --consola
 */
public class MenuConsola {

    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final Locale AR = Locale.forLanguageTag("es-AR");   // formato $ 8.500,00
    private static final int MAX_INTENTOS_LOGIN = 3;
    private static final String LINEA = "------------------------------------------------------------";

    private final Scanner entrada;
    private final PrintStream salida;
    private final boolean eco;   // repite lo leido en la salida (solo para grabar sesiones de prueba)
    private final NotificacionService notificaciones = new NotificacionService();
    private final ReservaService reservas = new ReservaService(notificaciones);
    private final ClienteService clientes = new ClienteService();
    private final MenuService menus = new MenuService();
    private final PagoService pagos = new PagoService(notificaciones);
    private final AutenticacionService autenticacion = new AutenticacionService();
    private final ReporteService reportes = new ReporteService();
    private final ArchivoService archivos = new ArchivoService();

    /** Accion de una opcion del menu: puede lanzar excepciones verificadas. */
    @FunctionalInterface
    private interface Accion {
        void ejecutar() throws Exception;
    }

    public MenuConsola(InputStream in, PrintStream out) {
        this(in, out, false);
    }

    public MenuConsola(InputStream in, PrintStream out, boolean eco) {
        this.entrada = new Scanner(in);
        this.salida = out;
        this.eco = eco;
    }

    public static void main(String[] args) {
        new MenuConsola(System.in, System.out).iniciar();
    }

    // ======================================================== MENU PRINCIPAL

    public void iniciar() {
        salida.println(LINEA);
        salida.println("  SULQUI - Reservas de eventos gastronomicos a fuego");
        salida.println(LINEA);
        int opcion = -1;
        try {
            do {
                salida.println();
                salida.println("MENU PRINCIPAL");
                salida.println("  1. Consultar disponibilidad de una fecha");
                salida.println("  2. Solicitar una reserva");
                salida.println("  3. Ver mis reservas");
                salida.println("  4. Ingresar como administrador");
                salida.println("  0. Salir");
                opcion = leerEntero("Opcion: ", 0, 4);
                switch (opcion) {
                    case 1 -> ejecutar(this::consultarDisponibilidad);
                    case 2 -> ejecutar(this::solicitarReserva);
                    case 3 -> ejecutar(this::verMisReservas);
                    case 4 -> ejecutar(this::ingresarAdministracion);
                    case 0 -> salida.println("Gracias por usar Sulqui.");
                    default -> salida.println("Opcion no valida.");   // no deberia ocurrir: leerEntero valida el rango
                }
            } while (opcion != 0);
        } catch (NoSuchElementException e) {
            salida.println();
            salida.println("Fin de la entrada de datos. Se cierra el programa.");
        } finally {
            salida.println("Sesion finalizada.");
        }
    }

    /** Ejecuta una opcion atrapando los errores esperables, para que el menu siga funcionando. */
    private void ejecutar(Accion accion) {
        try {
            accion.ejecutar();
        } catch (SulquiException e) {                 // ValidacionException, RecursoNoEncontradoException...
            salida.println("  [!] " + e.getTitulo() + ": " + e.getMessage());
        } catch (EstructuraVaciaException e) {
            salida.println("  [!] " + e.getMessage());
        } catch (PersistenciaException e) {           // error de MySQL ya clasificado (AP4)
            salida.println("  [ERROR BD] " + e.getTitulo() + ": " + e.getMessage());
        } catch (SQLException e) {
            salida.println("  [ERROR] No se pudo acceder a la base de datos: " + e.getMessage());
        } catch (NoSuchElementException e) {
            throw e;                                   // fin de la entrada: lo maneja iniciar()
        } catch (Exception e) {
            salida.println("  [ERROR] Error inesperado: " + e.getMessage());
        }
    }

    private void consultarDisponibilidad() throws SQLException, ValidacionException {
        salida.println("Ingrese la fecha del evento.");
        int dia = leerEntero("  Dia: ", 1, 31);
        int mes = leerEntero("  Mes: ", 1, 12);
        int anio = leerEntero("  Anio: ", LocalDate.now().getYear(), LocalDate.now().getYear() + 5);
        boolean libre = reservas.consultarDisponibilidad(dia, mes, anio);   // metodo sobrecargado
        if (libre) {
            salida.println("  [OK] La fecha " + dia + "/" + mes + "/" + anio + " esta disponible.");
        } else {
            salida.println("  [X] La fecha " + dia + "/" + mes + "/" + anio + " ya tiene un evento confirmado.");
        }
    }

    private void solicitarReserva() throws SQLException, SulquiException {
        String nombre = leerTexto("Nombre y apellido: ");
        String telefono = leerTexto("Telefono (10 digitos): ");
        String email = leerTexto("Email: ");
        int idCliente = clientes.registrarCliente(nombre, telefono, email);

        List<MenuPaquete> catalogo = menus.listarMenus();
        salida.println("Menus disponibles:");
        for (int i = 0; i < catalogo.size(); i++) {
            MenuPaquete m = catalogo.get(i);
            salida.printf(AR, "  %d. %-20s $ %,10.2f por persona%n", i + 1, m.getNombre(), m.getPrecioPorPersona());
        }
        MenuPaquete elegido = catalogo.get(leerEntero("Elija un menu: ", 1, catalogo.size()) - 1);
        LocalDate fecha = leerFecha("Fecha del evento (dd/mm/aaaa): ");
        int comensales = leerEntero("Cantidad de comensales (10 a 150): ", Integer.MIN_VALUE, Integer.MAX_VALUE);

        int id = reservas.solicitarReserva(idCliente, elegido.getIdMenu(), fecha, comensales);
        salida.printf(AR, "  [OK] Solicitud N.o %d registrada como Pendiente. Total estimado: $ %,.2f%n",
                id, reservas.calcularTotal(elegido.getIdMenu(), comensales));
    }

    private void verMisReservas() throws SQLException, RecursoNoEncontradoException {
        String email = leerTexto("Email: ");
        Cliente c = clientes.buscar(email);                // sobrecarga buscar(String)
        if (c == null) throw new RecursoNoEncontradoException("un cliente", email);
        List<Reserva> lista = clientes.obtenerReservas(c.getIdCliente());
        if (lista.isEmpty()) {
            salida.println("  No tiene reservas registradas.");
            return;
        }
        salida.println("  Reservas de " + c.getNombre() + ":");
        for (Reserva r : lista) {
            salida.println("   #" + r.getIdReserva() + "  " + r.getFecha().format(FORMATO_FECHA)
                    + "  " + r.getCantComensales() + " comensales  " + r.getEstado());
        }
    }

    // ======================================================== ADMINISTRACION

    private void ingresarAdministracion() throws SQLException {
        int intentos = 0;
        Administrador admin = null;
        while (admin == null && intentos < MAX_INTENTOS_LOGIN) {
            String usuario = leerTexto("Usuario: ");
            String clave = leerTexto("Contrasenia: ");
            admin = autenticacion.login(usuario, clave.toCharArray());
            intentos++;
            if (admin == null) {
                salida.println("  [X] Credenciales incorrectas (intento " + intentos + " de " + MAX_INTENTOS_LOGIN + ").");
            }
        }
        if (admin == null) {
            salida.println("  Se supero la cantidad de intentos. Vuelve al menu principal.");
            return;
        }
        salida.println("  Bienvenido, " + admin.describir() + ".");   // metodo heredado de Persona
        menuAdministracion(admin);
    }

    private void menuAdministracion(Administrador admin) {
        int opcion;
        do {
            salida.println();
            salida.println("MENU DE ADMINISTRACION");
            salida.println("  1. Atender solicitudes pendientes (cola, en orden de llegada)");
            salida.println("  2. Ver calendario de un mes");
            salida.println("  3. Registrar senia");
            salida.println("  4. Listar clientes ordenados");
            salida.println("  5. Buscar cliente por nombre");
            salida.println("  6. Listar reservas ordenadas");
            salida.println("  7. Ver catalogo ordenado por precio");
            salida.println("  8. Cambiar precio de un menu");
            salida.println("  9. Deshacer ultimo cambio de precio");
            salida.println(" 10. Resumen anual de eventos (reporte)");
            salida.println(" 11. Exportar reservas a archivo CSV");
            salida.println(" 12. Respaldar catalogo de menus en archivo");
            salida.println(" 13. Restaurar catalogo de menus desde archivo");
            salida.println("  0. Cerrar sesion");
            opcion = leerEntero("Opcion: ", 0, 13);
            switch (opcion) {
                case 1 -> ejecutar(() -> atenderPendientes(admin));
                case 2 -> ejecutar(this::verCalendario);
                case 3 -> ejecutar(this::registrarSenia);
                case 4 -> ejecutar(this::listarClientes);
                case 5 -> ejecutar(this::buscarCliente);
                case 6 -> ejecutar(this::listarReservas);
                case 7 -> ejecutar(this::verCatalogo);
                case 8 -> ejecutar(this::cambiarPrecio);
                case 9 -> ejecutar(this::deshacerPrecio);
                case 10 -> ejecutar(this::resumenAnual);
                case 11 -> ejecutar(this::exportarReservas);
                case 12 -> ejecutar(this::respaldarCatalogo);
                case 13 -> ejecutar(this::restaurarCatalogo);
                case 0 -> salida.println("  Sesion de administrador cerrada.");
                default -> salida.println("Opcion no valida.");
            }
        } while (opcion != 0);
    }

    /** Recorre la cola FIFO de solicitudes: confirmar, rechazar, posponer (vuelve al final) o salir. */
    private void atenderPendientes(Administrador admin) throws SQLException, SulquiException {
        Cola<Reserva> cola = reservas.colaDePendientes();
        if (cola.estaVacia()) {
            salida.println("  No hay solicitudes pendientes.");
            return;
        }
        boolean seguir = true;
        while (seguir && !cola.estaVacia()) {
            Reserva r = cola.verFrente();
            Cliente c = clientes.buscar(r.getIdCliente());
            salida.printf(AR, "  En cola: %d | Siguiente: #%d %s - %s - %d comensales - %s%n", cola.tamanio(),
                    r.getIdReserva(), r.getFecha().format(FORMATO_FECHA), c.getNombre(), r.getCantComensales(),
                    menus.obtenerMenu(r.getIdMenu()).getNombre());
            String accion = leerTexto("  [C]onfirmar  [R]echazar  [P]osponer  [V]olver: ").toUpperCase();
            switch (accion) {
                case "C" -> {
                    String direccion = leerTexto("  Direccion del evento: ");
                    LocalTime horario = leerHora("  Horario (hh:mm): ");
                    reservas.confirmarReserva(r.getIdReserva(), admin.getIdAdmin(), direccion, horario);
                    cola.desencolar();               // sale de la cola solo si la transaccion se confirmo
                    salida.println("  [OK] Reserva #" + r.getIdReserva() + " confirmada.");
                }
                case "R" -> {
                    String motivo = leerTexto("  Motivo: ");
                    reservas.rechazarReserva(cola.desencolar().getIdReserva(), admin.getIdAdmin(), motivo);
                    salida.println("  [OK] Reserva #" + r.getIdReserva() + " rechazada.");
                }
                case "P" -> {
                    cola.encolar(cola.desencolar());     // pasa al final de la cola
                    salida.println("  Reserva #" + r.getIdReserva() + " pospuesta al final de la cola.");
                }
                case "V" -> seguir = false;
                default -> salida.println("  Opcion no valida.");
            }
        }
        if (cola.estaVacia()) salida.println("  No quedan solicitudes pendientes.");
    }

    private void verCalendario() throws SQLException {
        int mes = leerEntero("  Mes: ", 1, 12);
        int anio = leerEntero("  Anio: ", 2025, 2035);
        YearMonth ym = YearMonth.of(anio, mes);
        // Matriz [semana][dia]: los dias con evento vienen en negativo y se marcan con *
        int[][] grilla = reportes.grillaCalendario(ym);
        salida.println("   " + ResumenAnual.nombreMes(mes) + " " + anio);
        salida.println("   Lu  Ma  Mi  Ju  Vi  Sa  Do");
        for (int[] semana : grilla) {
            StringBuilder fila = new StringBuilder("  ");
            boolean vacia = true;
            for (int dia : semana) {
                if (dia == 0) fila.append("    ");
                else {
                    fila.append(String.format("%3d%s", Math.abs(dia), dia < 0 ? "*" : " "));
                    vacia = false;
                }
            }
            if (!vacia) salida.println(fila.toString().stripTrailing());
        }
        List<Reserva> eventos = reservas.calendarioDelMes(ym);
        if (eventos.isEmpty()) {
            salida.println("  Sin eventos confirmados en " + mes + "/" + anio + ".");
            return;
        }
        salida.println("   (*) dias con evento confirmado:");
        for (Reserva r : eventos) {
            Evento ev = reservas.buscarEvento(r.getIdReserva());
            String lugar = ev == null ? "lugar a definir" : ev.getHorario().format(FORMATO_HORA) + " hs - " + ev.getDireccion();
            salida.println("   " + r.getFecha().format(FORMATO_FECHA) + "  #" + r.getIdReserva() + "  "
                    + clientes.buscar(r.getIdCliente()).getNombre() + "  " + r.getCantComensales()
                    + " comensales  " + lugar);
        }
    }

    private void registrarSenia() throws SQLException, SulquiException {
        List<Reserva> confirmadas = reservas.listarConfirmadas();
        if (confirmadas.isEmpty()) {
            salida.println("  No hay reservas confirmadas.");
            return;
        }
        for (Reserva r : confirmadas) {
            salida.println("   #" + r.getIdReserva() + "  " + r.getFecha().format(FORMATO_FECHA) + "  "
                    + clientes.buscar(r.getIdCliente()).getNombre());
        }
        int id = leerEntero("  N.o de reserva: ", 1, Integer.MAX_VALUE);
        BigDecimal monto = leerDecimal("  Monto: $ ");
        int medio = leerEntero("  Medio (1 Efectivo, 2 Transferencia, 3 Mercado Pago): ", 1, 3);
        String nombreMedio = switch (medio) {
            case 1 -> "Efectivo";
            case 2 -> "Transferencia";
            default -> "Mercado Pago";
        };
        pagos.registrarPago(id, monto, nombreMedio);
        salida.println("  [OK] Senia registrada y cliente notificado.");
    }

    private void listarClientes() throws SQLException {
        int criterio = leerEntero("  Ordenar por (1 nombre, 2 email): ", 1, 2);
        ListaEnlazada<Cliente> lista = clientes.listarOrdenados(
                criterio == 1 ? ClienteService.POR_NOMBRE : ClienteService.POR_EMAIL);
        int i = 1;
        for (Cliente c : lista) {
            salida.printf(AR, "   %2d. %-20s %-30s %s%n", i++, c.getNombre(), c.getEmail(), c.getTelefono());
        }
        salida.println("  Total: " + lista.tamanio() + " clientes.");
    }

    private void buscarCliente() throws SQLException, ValidacionException {
        String prefijo = leerTexto("  Nombre (o sus primeras letras): ");
        ListaEnlazada<Cliente> encontrados = clientes.buscarPorNombre(prefijo);
        if (encontrados.estaVacia()) {
            salida.println("  No hay clientes cuyo nombre empiece con \"" + prefijo + "\".");
        } else {
            for (Cliente c : encontrados) salida.println("   " + c);
        }
    }

    private void listarReservas() throws SQLException {
        int criterio = leerEntero("  Ordenar por (1 fecha, 2 cantidad de comensales): ", 1, 2);
        ListaEnlazada<Reserva> lista = reservas.reservasOrdenadas(
                criterio == 1 ? ReservaService.POR_FECHA : ReservaService.POR_COMENSALES_DESC);
        for (Reserva r : lista) {
            salida.printf(AR, "   #%-3d %s  %3d comensales  %s%n", r.getIdReserva(),
                    r.getFecha().format(FORMATO_FECHA), r.getCantComensales(), r.getEstado());
        }
    }

    private void verCatalogo() throws SQLException {
        for (MenuPaquete m : menus.listarPorPrecio()) {
            salida.printf(AR, "   %d. %-20s $ %,10.2f  (%s)%n", m.getIdMenu(), m.getNombre(),
                    m.getPrecioPorPersona(), m.getTipoPreparacion());
        }
    }

    private void cambiarPrecio() throws SQLException, SulquiException {
        verCatalogo();
        int id = leerEntero("  N.o de menu: ", 1, Integer.MAX_VALUE);
        BigDecimal precio = leerDecimal("  Nuevo precio por persona: $ ");
        CambioPrecio cambio = menus.actualizarPrecio(id, precio);
        salida.println("  [OK] " + cambio + " (cambios que se pueden deshacer: " + menus.cambiosParaDeshacer() + ")");
    }

    private void deshacerPrecio() throws SQLException, ValidacionException {
        CambioPrecio deshecho = menus.deshacerUltimoCambio();
        salida.println("  [OK] Se deshizo: " + deshecho + ". Precio restaurado a $ " + deshecho.getPrecioAnterior());
    }

    // ======================================================== REPORTES Y ARCHIVOS (AP4)

    /** Reporte anual: arreglos de 12 posiciones (un casillero por mes) y conteo por estado. */
    private void resumenAnual() throws SQLException {
        int anio = leerEntero("  Anio: ", 2025, 2035);
        ResumenAnual resumen = reportes.resumenAnual(anio);
        int[] eventos = resumen.getEventos();
        int[] comensales = resumen.getComensales();
        BigDecimal[] facturacion = resumen.getFacturacion();
        salida.println("   Mes          Eventos  Comensales   Facturacion estimada");
        for (int i = 0; i < ResumenAnual.MESES; i++) {
            if (eventos[i] == 0) continue;                 // solo los meses con actividad
            salida.printf(AR, "   %-12s %7d %11d   $ %,18.2f%n", ResumenAnual.nombreMes(i + 1),
                    eventos[i], comensales[i], facturacion[i]);
        }
        salida.printf(AR, "   %-12s %7d %11d   $ %,18.2f%n", "TOTAL", resumen.totalEventos(),
                resumen.totalComensales(), resumen.totalFacturacion());
        if (resumen.totalEventos() > 0) {
            salida.printf(AR, "   Mes de mayor demanda: %s | Promedio: %.1f comensales por evento%n",
                    ResumenAnual.nombreMes(resumen.mesDeMayorDemanda()), resumen.promedioComensales());
        }
        int[] porEstado = reportes.reservasPorEstado();
        StringBuilder estados = new StringBuilder("   Reservas por estado:");
        for (EstadoReserva e : EstadoReserva.values()) {
            estados.append(' ').append(e).append('=').append(porEstado[e.ordinal()]);
        }
        salida.println(estados);
    }

    private void exportarReservas() throws SQLException, ArchivoException {
        Path destino = ArchivoService.archivoReservasPorDefecto();
        int cantidad = archivos.exportarReservas(destino);
        salida.println("  [OK] " + cantidad + " reservas exportadas a " + destino);
    }

    private void respaldarCatalogo() throws SQLException, ArchivoException {
        Path destino = ArchivoService.archivoRespaldoPorDefecto();
        int cantidad = archivos.respaldarCatalogo(destino);
        salida.println("  [OK] " + cantidad + " menus respaldados en " + destino);
    }

    private void restaurarCatalogo() throws SQLException, ArchivoException {
        String texto = leerTexto("  Archivo (D = respaldo por defecto, o escriba la ruta): ");
        Path origen = texto.equalsIgnoreCase("D") ? ArchivoService.archivoRespaldoPorDefecto() : Path.of(texto);
        ArchivoService.ResultadoRestauracion r = archivos.restaurarCatalogo(origen);
        salida.println("  [OK] Catalogo restaurado desde " + origen + ": " + r.actualizados()
                + " menus actualizados y " + r.agregados() + " agregados.");
    }

    // ======================================================== LECTURA VALIDADA

    private String leerTexto(String mensaje) {
        String texto;
        do {
            salida.print(mensaje);
            texto = entrada.nextLine().trim();
            if (eco) salida.println(texto);
            if (texto.isEmpty()) salida.println("  El dato es obligatorio.");
        } while (texto.isEmpty());
        return texto;
    }

    private int leerEntero(String mensaje, int minimo, int maximo) {
        while (true) {
            String texto = leerTexto(mensaje);
            try {
                int valor = Integer.parseInt(texto);
                if (valor >= minimo && valor <= maximo) return valor;
                salida.println("  Ingrese un valor entre " + minimo + " y " + maximo + ".");
            } catch (NumberFormatException e) {
                salida.println("  \"" + texto + "\" no es un numero entero.");
            }
        }
    }

    private BigDecimal leerDecimal(String mensaje) {
        while (true) {
            String texto = leerTexto(mensaje).replace(",", ".");
            try {
                return new BigDecimal(texto);
            } catch (NumberFormatException e) {
                salida.println("  \"" + texto + "\" no es un importe valido (ej.: 30000 o 8500.50).");
            }
        }
    }

    private LocalTime leerHora(String mensaje) {
        while (true) {
            String texto = leerTexto(mensaje);
            try {
                return LocalTime.parse(texto.length() == 4 ? "0" + texto : texto, FORMATO_HORA);
            } catch (DateTimeParseException e) {
                salida.println("  \"" + texto + "\" no es un horario valido con formato hh:mm.");
            }
        }
    }

    private LocalDate leerFecha(String mensaje) {
        while (true) {
            String texto = leerTexto(mensaje);
            try {
                return LocalDate.parse(texto, FORMATO_FECHA);
            } catch (DateTimeParseException e) {
                salida.println("  \"" + texto + "\" no es una fecha valida con formato dd/mm/aaaa.");
            }
        }
    }
}
