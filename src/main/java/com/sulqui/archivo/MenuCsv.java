package com.sulqui.archivo;

import com.sulqui.modelo.MenuPaquete;

import java.math.BigDecimal;

/** Formato del respaldo del catalogo de menus: id;nombre;precio_persona;tipo_preparacion. */
public class MenuCsv extends ArchivoCsv<MenuPaquete> {

    @Override
    protected String[] columnas() {
        return new String[]{"id_menu", "nombre", "precio_persona", "tipo_preparacion"};
    }

    @Override
    protected String[] aFila(MenuPaquete m) {
        return new String[]{String.valueOf(m.getIdMenu()), m.getNombre(),
                m.getPrecioPorPersona().toPlainString(), m.getTipoPreparacion()};
    }

    @Override
    protected MenuPaquete desdeFila(String[] c, int linea) throws ArchivoException {
        int id;
        try {
            id = c[0].isEmpty() ? 0 : Integer.parseInt(c[0]);        // id vacio = menu nuevo
        } catch (NumberFormatException e) {
            throw new ArchivoException("Linea " + linea + ": el id \"" + c[0] + "\" no es un numero.", e);
        }
        if (c[1].isEmpty()) throw new ArchivoException("Linea " + linea + ": el nombre del menu esta vacio.");
        MenuPaquete m = new MenuPaquete();
        m.setIdMenu(id);
        m.setNombre(c[1]);
        m.setTipoPreparacion(c[3].isEmpty() ? "Otro" : c[3]);
        try {
            m.setPrecioPorPersona(new BigDecimal(c[2].replace(",", ".")));   // valida precio > 0
        } catch (NumberFormatException e) {
            throw new ArchivoException("Linea " + linea + ": el precio \"" + c[2] + "\" no es un importe.", e);
        } catch (IllegalArgumentException e) {
            throw new ArchivoException("Linea " + linea + ": " + e.getMessage(), e);
        }
        return m;
    }
}
