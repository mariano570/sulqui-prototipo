-- ============================================================
-- Sulqui - Insercion, consulta, actualizacion y borrado de registros
-- Trabajo Practico N.2 - Seminario de practica (apartado 7.4)
-- Ejecutar despues de 02_sulqui_datos_ejemplo.sql
-- ============================================================
USE sulqui_db;

-- ---------- INSERCION ----------
INSERT INTO cliente (nombre, telefono, email)
VALUES ('Marcos Diaz', '3517004444', 'marcos.diaz@example.com');

-- ---------- CONSULTA ----------
-- Listado de reservas de un cliente
SELECT * FROM reserva WHERE id_cliente = 1;

-- Disponibilidad de una fecha puntual (RF-01)
SELECT COUNT(*) AS reservas_confirmadas
FROM reserva
WHERE fecha = '2026-11-15' AND estado = 'Confirmada';

-- Historial de un cliente con nombre del menu (RF-06)
SELECT r.id_reserva, r.fecha, r.estado, m.nombre AS menu
FROM reserva r
JOIN menu_paquete m ON m.id_menu = r.id_menu
WHERE r.id_cliente = 1;

-- ---------- ACTUALIZACION ----------
-- Confirmar una reserva pendiente (RF-03)
UPDATE reserva SET id_admin = 1, estado = 'Confirmada' WHERE id_reserva = 2;

-- Baja logica: cancelar una reserva
UPDATE reserva SET estado = 'Cancelada' WHERE id_reserva = 2;

-- ---------- BORRADO ----------
-- Borrado fisico de un cliente sin reservas asociadas
DELETE FROM cliente WHERE email = 'marcos.diaz@example.com';
