package com.sulqui.servicio;

import com.sulqui.dao.ClienteDAO;
import com.sulqui.dao.ReservaDAO;
import com.sulqui.estructuras.Algoritmos;
import com.sulqui.estructuras.ListaEnlazada;
import com.sulqui.modelo.Cliente;
import com.sulqui.modelo.Reserva;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

/** Reglas de negocio de clientes: alta, baja, historial, listados ordenados y busqueda. */
public class ClienteService {

    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final Pattern TELEFONO = Pattern.compile("^\\d{10}$");

    /** Criterios de orden para el listado de clientes. */
    public static final Comparator<Cliente> POR_NOMBRE = Comparator.naturalOrder();   // Comparable de Cliente
    public static final Comparator<Cliente> POR_EMAIL =
            Comparator.comparing(Cliente::getEmail, String.CASE_INSENSITIVE_ORDER);

    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final ReservaDAO reservaDAO = new ReservaDAO();

    /** Alta de cliente; si el email ya existe devuelve el id existente (cliente recurrente). */
    public int registrarCliente(String nombre, String telefono, String email)
            throws SQLException, ValidacionException {
        if (nombre == null || nombre.isBlank()) throw new ValidacionException("El nombre es obligatorio.");
        if (telefono == null || !TELEFONO.matcher(telefono).matches())
            throw new ValidacionException("El telefono debe tener 10 digitos (ej.: 3517004444).");
        if (email == null || !EMAIL.matcher(email).matches())
            throw new ValidacionException("El email no tiene un formato valido.");
        Cliente existente = clienteDAO.buscarPorEmail(email);
        if (existente != null) return existente.getIdCliente();
        return clienteDAO.insertar(new Cliente(nombre.trim(), telefono, email.trim()));
    }

    /** Busca un cliente por su id. */
    public Cliente buscar(int idCliente) throws SQLException {
        return clienteDAO.buscarPorId(idCliente);
    }

    /** Sobrecarga: busca un cliente por su email. */
    public Cliente buscar(String email) throws SQLException {
        return clienteDAO.buscarPorEmail(email);
    }

    /** Clientes en una lista enlazada ordenada con merge sort segun el criterio indicado. */
    public ListaEnlazada<Cliente> listarOrdenados(Comparator<Cliente> criterio) throws SQLException {
        ListaEnlazada<Cliente> lista = new ListaEnlazada<>();
        for (Cliente c : clienteDAO.listarTodos()) {
            lista.agregar(c);
        }
        lista.ordenar(criterio);
        return lista;
    }

    /**
     * Busca clientes cuyo nombre empieza con el texto indicado. Ordena por
     * nombre y aplica busqueda binaria para ubicar la primera coincidencia;
     * luego recorre hacia adelante mientras el prefijo coincida.
     */
    public ListaEnlazada<Cliente> buscarPorNombre(String prefijo) throws SQLException, ValidacionException {
        if (prefijo == null || prefijo.isBlank()) throw new ValidacionException("Ingrese al menos una letra.");
        Cliente[] ordenados = listarOrdenados(POR_NOMBRE).aArreglo(Cliente[]::new);
        ListaEnlazada<Cliente> resultado = new ListaEnlazada<>();
        int i = Algoritmos.busquedaBinaria(ordenados, c -> Algoritmos.compararPrefijo(c.getNombre(), prefijo.trim()));
        if (i == -1) return resultado;
        while (i < ordenados.length && Algoritmos.compararPrefijo(ordenados[i].getNombre(), prefijo.trim()) == 0) {
            resultado.agregar(ordenados[i]);
            i++;
        }
        return resultado;
    }

    /** CU-06 / RF-06: historial de reservas del cliente. */
    public List<Reserva> obtenerReservas(int idCliente) throws SQLException {
        return reservaDAO.listarPorCliente(idCliente);
    }

    /** Solo se permite borrar clientes sin reservas (integridad referencial). */
    public void eliminarCliente(int idCliente) throws SQLException, ValidacionException {
        if (clienteDAO.tieneReservas(idCliente)) {
            throw new ValidacionException("El cliente " + idCliente + " tiene reservas asociadas y no puede eliminarse.");
        }
        clienteDAO.eliminar(idCliente);
    }
}
