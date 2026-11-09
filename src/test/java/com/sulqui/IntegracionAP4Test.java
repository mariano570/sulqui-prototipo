package com.sulqui;

import com.sulqui.archivo.ArchivoException;
import com.sulqui.dao.*;
import com.sulqui.modelo.*;
import com.sulqui.servicio.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de la entrega 4 (proyecto integrador): patron DAO + Singleton,
 * transacciones, excepciones de MySQL, arreglos y ArrayList, y archivos.
 * Cada prueba parte de la base sulqui_test con los datos de ejemplo:
 * 3 clientes, 3 menus, 4 reservas (#1 y #3 confirmadas, #2 y #4 pendientes),
 * 2 eventos y 1 senia.
 */
@DisplayName("Proyecto integrador (AP4): persistencia, patron DAO, arreglos y archivos")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class IntegracionAP4Test {

    private final NotificacionService notificaciones = new NotificacionService();
    private final ReservaService reservas = new ReservaService(notificaciones);
    private final ReservaDAO reservaDAO = new ReservaDAO();
    private final EventoDAO eventoDAO = new EventoDAO();
    private final MenuDAO menuDAO = new MenuDAO();

    @TempDir
    Path carpeta;

    @BeforeEach
    void reiniciarBase() throws Exception {
        BaseDatosPrueba.reiniciarConDatosDeEjemplo();
    }

    @Test @Order(26)
    @DisplayName("P26 Singleton: una unica instancia de Conexion entrega conexiones validas")
    void p26Singleton() throws Exception {
        Conexion a = Conexion.getInstancia();
        Conexion b = Conexion.getInstancia();
        try (Connection c1 = a.obtener(); Connection c2 = b.obtener()) {
            assertSame(a, b);
            assertTrue(c1.isValid(2) && c2.isValid(2));
            assertNotSame(c1, c2);                       // una conexion por operacion
            System.out.println("P26 obtenido: misma instancia=" + (a == b) + "; conexiones validas y distintas");
        }
    }

    @Test @Order(27)
    @DisplayName("P27 Interfaz Dao<T>: arreglo de DAO tratados de forma polimorfica, listas en ArrayList")
    void p27InterfazDao() throws Exception {
        Dao<?>[] daos = {new ClienteDAO(), new MenuDAO(), new ReservaDAO(), new EventoDAO(), new SeniaPagoDAO()};
        int[] esperado = {3, 3, 4, 2, 1};
        int[] obtenido = new int[daos.length];
        for (int i = 0; i < daos.length; i++) {
            ArrayList<?> filas = daos[i].listarTodos();
            obtenido[i] = filas.size();
            assertEquals(daos[i].contar(), filas.size());
        }
        assertArrayEquals(esperado, obtenido);
        assertThrows(UnsupportedOperationException.class, () -> new SeniaPagoDAO().actualizar(null));
        assertThrows(UnsupportedOperationException.class, () -> new AdministradorDAO().insertar(null));
        System.out.println("P27 obtenido: registros por DAO " + Arrays.toString(obtenido));
    }

    @Test @Order(28)
    @DisplayName("P28 Transaccion confirmada: reserva #2 pasa a Confirmada y se registra su evento")
    void p28TransaccionConfirmada() throws Exception {
        reservas.confirmarReserva(2, 1, "Bv. San Juan 800, Cordoba", LocalTime.of(12, 30));
        Reserva r = reservaDAO.buscarPorId(2);
        Evento ev = eventoDAO.buscarPorReserva(2);
        assertEquals(EstadoReserva.CONFIRMADA, r.getEstado());
        assertEquals("Bv. San Juan 800, Cordoba", ev.getDireccion());
        assertEquals(LocalTime.of(12, 30), ev.getHorario());
        assertEquals(3, eventoDAO.contar());
        assertTrue(notificaciones.getEnviadas().get(0).contains("Lugar: Bv. San Juan 800, Cordoba"));
        System.out.println("P28 obtenido: estado=" + r.getEstado() + ", evento #" + ev.getIdEvento() + " "
                + ev.getHorario() + " " + ev.getDireccion());
    }

    @Test @Order(29)
    @DisplayName("P29 Rollback: si falla el alta del evento, la reserva #4 sigue Pendiente")
    void p29Rollback() throws Exception {
        String direccionLarga = "Calle ".repeat(30);                       // 180 caracteres > VARCHAR(150)
        PersistenciaException ex = assertThrows(PersistenciaException.class,
                () -> reservas.confirmarReserva(4, 1, direccionLarga, LocalTime.of(21, 0)));
        assertEquals(PersistenciaException.Tipo.DATO_INVALIDO, ex.getTipo());
        assertEquals(EstadoReserva.PENDIENTE, reservaDAO.buscarPorId(4).getEstado());
        assertNull(eventoDAO.buscarPorReserva(4));
        assertTrue(notificaciones.getEnviadas().isEmpty());                // no se notifico nada
        System.out.println("P29 obtenido: " + ex.getTipo() + " -> \"" + ex.getMessage()
                + "\"; estado de #4=" + reservaDAO.buscarPorId(4).getEstado());
    }

    @Test @Order(30)
    @DisplayName("P30 PersistenciaException clasifica los errores de MySQL")
    void p30ClasificacionDeErrores() throws Exception {
        ClienteDAO clienteDAO = new ClienteDAO();
        PersistenciaException duplicado = assertThrows(PersistenciaException.class,
                () -> clienteDAO.insertar(new Cliente("Otra Ana", "3517009999", "ana.gomez@example.com")));
        PersistenciaException referencia = assertThrows(PersistenciaException.class, () -> clienteDAO.eliminar(1));
        PersistenciaException restriccion = assertThrows(PersistenciaException.class,
                () -> menuDAO.insertar(new MenuPaquete(0, "Menu gratis", new BigDecimal("-1"), "Asado")));
        String urlOriginal = System.getProperty("db.url");
        PersistenciaException conexion;
        try {
            System.setProperty("db.url", "jdbc:mysql://localhost:3399/sulqui_test?connectTimeout=2000");
            conexion = assertThrows(PersistenciaException.class, () -> clienteDAO.listarTodos());
        } finally {
            System.setProperty("db.url", urlOriginal);
        }
        assertEquals(PersistenciaException.Tipo.DUPLICADO, duplicado.getTipo());
        assertEquals(PersistenciaException.Tipo.REFERENCIA, referencia.getTipo());
        assertEquals(PersistenciaException.Tipo.RESTRICCION, restriccion.getTipo());
        assertTrue(restriccion.getMessage().contains("chk_precio"));
        assertEquals(PersistenciaException.Tipo.CONEXION, conexion.getTipo());
        assertNotNull(duplicado.getCause());                               // conserva la SQLException original
        System.out.println("P30 obtenido: " + duplicado.getTipo() + " (" + duplicado.getErrorCode() + "), "
                + referencia.getTipo() + " (" + referencia.getErrorCode() + "), "
                + restriccion.getTipo() + " (" + restriccion.getErrorCode() + "), " + conexion.getTipo());
    }

    @Test @Order(31)
    @DisplayName("P31 Arreglos: resumen anual por mes, conteo por estado y grilla del calendario")
    void p31Arreglos() throws Exception {
        ReporteService reportes = new ReporteService();
        ResumenAnual r = reportes.resumenAnual(2026);
        int[] eventos = r.getEventos();
        int[] comensales = r.getComensales();
        BigDecimal[] facturacion = r.getFacturacion();
        assertEquals(12, eventos.length);
        assertEquals(1, eventos[10]);                                        // noviembre
        assertEquals(1, eventos[11]);                                        // diciembre
        assertEquals(25, comensales[10]);
        assertEquals(40, comensales[11]);
        assertEquals(0, new BigDecimal("212500.00").compareTo(facturacion[10]));   // 25 x 8.500
        assertEquals(0, new BigDecimal("440000.00").compareTo(facturacion[11]));   // 40 x 11.000
        assertEquals(0, new BigDecimal("652500.00").compareTo(r.totalFacturacion()));
        assertEquals(12, r.mesDeMayorDemanda());
        eventos[10] = 99;                                                    // copia: no altera el reporte
        assertEquals(1, r.getEventos()[10]);

        int[] porEstado = reportes.reservasPorEstado();
        assertArrayEquals(new int[]{2, 2, 0, 0, 0}, porEstado);

        int[][] grilla = reportes.grillaCalendario(YearMonth.of(2026, 11));
        assertEquals(1, grilla[0][6]);                                       // 01/11/2026 es domingo
        assertEquals(-15, grilla[2][6]);                                     // 15/11 tiene evento confirmado
        assertEquals(30, grilla[5][0]);                                      // 30/11 es lunes de la 6.a semana
        System.out.println("P31 obtenido: eventos=" + Arrays.toString(r.getEventos()) + "; facturacion nov/dic="
                + facturacion[10] + "/" + facturacion[11] + "; por estado=" + Arrays.toString(porEstado)
                + "; grilla[2][6]=" + grilla[2][6]);
    }

    @Test @Order(32)
    @DisplayName("P32 Archivos: exportar reservas, respaldar el catalogo y restaurarlo")
    void p32Archivos() throws Exception {
        ArchivoService archivos = new ArchivoService();
        Path reservasCsv = carpeta.resolve("reservas.csv");
        Path respaldo = carpeta.resolve("menus.csv");

        assertEquals(4, archivos.exportarReservas(reservasCsv));
        assertEquals(5, Files.readAllLines(reservasCsv).size());            // encabezado + 4 filas
        assertEquals(4, archivos.leerReservas(reservasCsv).size());

        assertEquals(3, archivos.respaldarCatalogo(respaldo));
        new MenuService().actualizarPrecio(1, new BigDecimal("9999"));      // cambio "erroneo"
        Files.writeString(respaldo, ";Cordero patagonico;14500;Asado\n", java.nio.file.StandardOpenOption.APPEND);

        ArchivoService.ResultadoRestauracion res = archivos.restaurarCatalogo(respaldo);
        assertEquals(3, res.actualizados());
        assertEquals(1, res.agregados());
        assertEquals(0, new BigDecimal("8500.00").compareTo(menuDAO.buscarPorId(1).getPrecioPorPersona()));
        assertEquals(4, menuDAO.contar());
        System.out.println("P32 obtenido: 4 reservas exportadas; 3 menus respaldados; restauracion -> "
                + res.actualizados() + " actualizados, " + res.agregados() + " agregado; precio menu 1 = "
                + menuDAO.buscarPorId(1).getPrecioPorPersona());
    }

    @Test @Order(33)
    @DisplayName("P33 Archivo invalido: se informa la linea con error y el catalogo no cambia")
    void p33ArchivoInvalido() throws Exception {
        ArchivoService archivos = new ArchivoService();
        Path malo = carpeta.resolve("menus_malo.csv");
        Files.writeString(malo, "id_menu;nombre;precio_persona;tipo_preparacion\n"
                + "1;Asado clasico;9000;Asado\n"
                + "2;Comida al disco;-5;Al disco\n");
        ArchivoException precio = assertThrows(ArchivoException.class, () -> archivos.restaurarCatalogo(malo));

        Path sinEncabezado = carpeta.resolve("otro.csv");
        Files.writeString(sinEncabezado, "nombre,precio\nAsado,9000\n");
        ArchivoException encabezado = assertThrows(ArchivoException.class,
                () -> archivos.restaurarCatalogo(sinEncabezado));
        ArchivoException inexistente = assertThrows(ArchivoException.class,
                () -> archivos.restaurarCatalogo(carpeta.resolve("no_existe.csv")));

        assertTrue(precio.getMessage().startsWith("Linea 3:"));
        assertTrue(encabezado.getMessage().contains("encabezado esperado"));
        assertTrue(inexistente.getMessage().startsWith("No existe el archivo"));
        assertEquals("Error de archivo", precio.getTitulo());
        assertEquals(0, new BigDecimal("8500.00").compareTo(menuDAO.buscarPorId(1).getPrecioPorPersona()));
        System.out.println("P33 obtenido: \"" + precio.getMessage() + "\" / encabezado rechazado / \""
                + inexistente.getMessage().substring(0, 20) + "...\"; precio menu 1 sin cambios");
    }

    @Test @Order(34)
    @DisplayName("P34 Rollback al restaurar: un registro rechazado por MySQL deshace todos")
    void p34RollbackRestauracion() throws Exception {
        ArchivoService archivos = new ArchivoService();
        Path respaldo = carpeta.resolve("menus_nombre_largo.csv");
        Files.writeString(respaldo, "id_menu;nombre;precio_persona;tipo_preparacion\n"
                + "1;Asado clasico;9000;Asado\n"                                   // se graba primero...
                + ";" + "Menu de nombre excesivo ".repeat(6) + ";12000;Asado\n");  // ...y esta fila falla
        PersistenciaException ex = assertThrows(PersistenciaException.class, () -> archivos.restaurarCatalogo(respaldo));
        assertEquals(PersistenciaException.Tipo.DATO_INVALIDO, ex.getTipo());
        assertEquals(0, new BigDecimal("8500.00").compareTo(menuDAO.buscarPorId(1).getPrecioPorPersona()));
        assertEquals(3, menuDAO.contar());
        System.out.println("P34 obtenido: " + ex.getTipo() + "; precio menu 1 = "
                + menuDAO.buscarPorId(1).getPrecioPorPersona() + " (sin cambios); menus = " + menuDAO.contar());
    }
}
