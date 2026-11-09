package com.sulqui.servicio;

import com.sulqui.archivo.ArchivoException;
import com.sulqui.archivo.MenuCsv;
import com.sulqui.archivo.ReservaCsv;
import com.sulqui.dao.Conexion;
import com.sulqui.dao.MenuDAO;
import com.sulqui.dao.ReservaDAO;
import com.sulqui.modelo.MenuPaquete;
import com.sulqui.modelo.Reserva;

import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Guarda y recupera informacion relevante en archivos (CSV):
 *  - exportar el listado de reservas para trabajarlo en una planilla;
 *  - respaldar el catalogo de menus y restaurarlo (por ejemplo, despues de
 *    cambios de precio erroneos o al instalar la aplicacion en otro equipo).
 *
 * La restauracion es "todo o nada": primero se valida el archivo completo y
 * luego se graba en una unica transaccion; si un registro falla en la base,
 * se deshacen todos (rollback).
 */
public class ArchivoService {

    public static final Path CARPETA_DATOS = Path.of("datos");

    private final ReservaDAO reservaDAO = new ReservaDAO();
    private final MenuDAO menuDAO = new MenuDAO();
    private final ReservaCsv formatoReservas = new ReservaCsv();
    private final MenuCsv formatoMenus = new MenuCsv();

    /** Resultado de una restauracion: cuantos menus se actualizaron y cuantos se agregaron. */
    public record ResultadoRestauracion(int actualizados, int agregados) {
        public int total() { return actualizados + agregados; }
    }

    public static Path archivoReservasPorDefecto() {
        return CARPETA_DATOS.resolve("exportaciones").resolve("reservas_" + LocalDate.now() + ".csv");
    }

    public static Path archivoRespaldoPorDefecto() {
        return CARPETA_DATOS.resolve("respaldos").resolve("catalogo_menus.csv");
    }

    /** Exporta todas las reservas; devuelve la cantidad exportada. */
    public int exportarReservas(Path destino) throws SQLException, ArchivoException {
        ArrayList<Reserva> reservas = reservaDAO.listarTodos();
        return formatoReservas.exportar(reservas, destino);
    }

    /** Lee un archivo de reservas exportado (para verificarlo o procesarlo). */
    public ArrayList<Reserva> leerReservas(Path origen) throws ArchivoException {
        return formatoReservas.importar(origen);
    }

    /** Guarda el catalogo completo de menus; devuelve la cantidad respaldada. */
    public int respaldarCatalogo(Path destino) throws SQLException, ArchivoException {
        return formatoMenus.exportar(menuDAO.listarTodos(), destino);
    }

    /**
     * Restaura el catalogo desde un respaldo: los menus con id existente se
     * actualizan y los demas se agregan, todo dentro de una transaccion.
     */
    public ResultadoRestauracion restaurarCatalogo(Path origen) throws SQLException, ArchivoException {
        ArrayList<MenuPaquete> menus = formatoMenus.importar(origen);       // 1) valida el archivo completo
        if (menus.isEmpty()) throw new ArchivoException("El archivo " + origen.getFileName() + " no tiene menus.");
        return Conexion.getInstancia().enTransaccion("restaurar el catalogo de menus", con -> {
            int actualizados = 0;
            int agregados = 0;
            for (MenuPaquete m : menus) {                                  // 2) graba todo o nada
                if (m.getIdMenu() > 0 && menuDAO.actualizar(con, m)) {
                    actualizados++;
                } else {
                    menuDAO.insertar(con, m);
                    agregados++;
                }
            }
            return new ResultadoRestauracion(actualizados, agregados);
        });
    }
}
