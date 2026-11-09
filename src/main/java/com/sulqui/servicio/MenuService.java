package com.sulqui.servicio;

import com.sulqui.dao.MenuDAO;
import com.sulqui.estructuras.Algoritmos;
import com.sulqui.estructuras.Pila;
import com.sulqui.modelo.MenuPaquete;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;

/** CU-08 / RF-04: gestion del catalogo de menus, con historial de cambios de precio (pila). */
public class MenuService {

    private final MenuDAO menuDAO = new MenuDAO();
    private final Pila<CambioPrecio> historialPrecios = new Pila<>();

    public List<MenuPaquete> listarMenus() throws SQLException { return menuDAO.listarTodos(); }

    /** Catalogo ordenado por precio (de menor a mayor) con ordenamiento por insercion. */
    public MenuPaquete[] listarPorPrecio() throws SQLException {
        MenuPaquete[] menus = menuDAO.listarTodos().toArray(new MenuPaquete[0]);
        Algoritmos.ordenarPorInsercion(menus, Comparator.comparing(MenuPaquete::getPrecioPorPersona));
        return menus;
    }

    public MenuPaquete obtenerMenu(int idMenu) throws SQLException { return menuDAO.buscarPorId(idMenu); }

    public int agregarMenu(String nombre, BigDecimal precioPorPersona, String tipo)
            throws SQLException, ValidacionException {
        if (nombre == null || nombre.isBlank()) throw new ValidacionException("El nombre del menu es obligatorio.");
        if (precioPorPersona == null || precioPorPersona.signum() <= 0)
            throw new ValidacionException("El precio por persona debe ser mayor a cero.");
        return menuDAO.insertar(new MenuPaquete(0, nombre.trim(), precioPorPersona, tipo));
    }

    /** Cambia el precio y apila el cambio para poder deshacerlo. */
    public CambioPrecio actualizarPrecio(int idMenu, BigDecimal nuevoPrecio) throws SQLException, SulquiException {
        MenuPaquete m = menuDAO.buscarPorId(idMenu);
        if (m == null) throw new RecursoNoEncontradoException("el menu", idMenu);
        BigDecimal anterior = m.getPrecioPorPersona();
        try {
            m.setPrecioPorPersona(nuevoPrecio);      // el propio objeto valida el precio
        } catch (IllegalArgumentException e) {
            throw new ValidacionException(e.getMessage());   // se traduce a una excepcion del sistema
        }
        menuDAO.actualizar(m);
        CambioPrecio cambio = new CambioPrecio(idMenu, m.getNombre(), anterior, nuevoPrecio);
        historialPrecios.apilar(cambio);
        return cambio;
    }

    /** Deshace el ultimo cambio de precio (LIFO). */
    public CambioPrecio deshacerUltimoCambio() throws SQLException, ValidacionException {
        if (historialPrecios.estaVacia()) {
            throw new ValidacionException("No hay cambios de precio para deshacer.");
        }
        CambioPrecio ultimo = historialPrecios.desapilar();
        MenuPaquete m = menuDAO.buscarPorId(ultimo.getIdMenu());
        m.setPrecioPorPersona(ultimo.getPrecioAnterior());
        menuDAO.actualizar(m);
        return ultimo;
    }

    public int cambiosParaDeshacer() { return historialPrecios.tamanio(); }
}
