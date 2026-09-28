package com.sulqui.app;

import com.sulqui.dao.ClienteDAO;
import com.sulqui.modelo.Cliente;
import com.sulqui.modelo.Reserva;
import com.sulqui.servicio.ReservaService;

import java.time.LocalDate;
import java.util.List;

/**
 * Clase de demostracion del prototipo Sulqui.
 * Ejercita el flujo principal: alta de cliente, consulta de
 * disponibilidad, solicitud de reserva y confirmacion, mostrando el uso
 * de las clases DAO (persistencia) y de servicio (reglas de negocio).
 */
public class Main {

    public static void main(String[] args) throws Exception {
        ClienteDAO clienteDAO = new ClienteDAO();
        ReservaService reservaService = new ReservaService();

        // 1) Alta de un nuevo cliente (INSERT)
        Cliente nuevo = new Cliente(0, "Marcos Diaz", "3517004444", "marcos.diaz@example.com");
        int idCliente = clienteDAO.insertar(nuevo);
        System.out.println("Cliente creado con id: " + idCliente);

        // 2) Listado de clientes (SELECT)
        List<Cliente> clientes = clienteDAO.listarTodos();
        System.out.println("Clientes registrados: " + clientes.size());
        clientes.forEach(System.out::println);

        // 3) Consultar disponibilidad de una fecha (RF-01)
        LocalDate fechaDeseada = LocalDate.of(2026, 12, 20);
        boolean disponible = reservaService.consultarDisponibilidad(fechaDeseada);
        System.out.println("Disponibilidad " + fechaDeseada + ": " + disponible);

        // 4) Solicitar una reserva (RF-02)
        if (disponible) {
            Reserva reserva = new Reserva(idCliente, 1, fechaDeseada, 20);
            int idReserva = reservaService.solicitarReserva(reserva);
            System.out.println("Reserva solicitada con id: " + idReserva);

            // 5) Confirmar la reserva (RF-03), lo que dispara la notificacion (<<include>>)
            reservaService.confirmarReserva(idReserva, 1, nuevo.getEmail());
        }

        // 6) Borrado de un registro de prueba (DELETE)
        clienteDAO.eliminar(idCliente);
        System.out.println("Cliente de prueba eliminado.");
    }
}
