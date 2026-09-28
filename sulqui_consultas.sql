-- ============================================================
-- Sulqui - Insercion, consulta y borrado de registros
-- Trabajo Practico N.2 - Seminario de practica
-- ============================================================
USE sulqui_db;

-- ---------- INSERCION DE DATOS DE EJEMPLO ----------

INSERT INTO administrador (usuario, contrasenia) VALUES
    ('mariano', 'hash_de_contrasenia_1');

INSERT INTO cliente (nombre, telefono, email) VALUES
    ('Ana Gomez',      '3517001111', 'ana.gomez@example.com'),
    ('Carlos Perez',   '3517002222', 'carlos.perez@example.com'),
    ('Lucia Fernandez','3517003333', 'lucia.fernandez@example.com');

INSERT INTO menu_paquete (nombre, precio_persona, tipo_preparacion) VALUES
    ('Asado clasico',        8500.00, 'Asado'),
    ('Comida al disco',      7200.00, 'Al disco'),
    ('Parrillada premium',  11000.00, 'Asado');

INSERT INTO reserva (id_cliente, id_admin, id_menu, fecha, cant_comensales, estado) VALUES
    (1, 1, 1, '2026-11-15', 25, 'Confirmada'),
    (2, 1, 2, '2026-11-22', 15, 'Pendiente'),
    (3, 1, 3, '2026-12-05', 40, 'Confirmada');

INSERT INTO evento (id_reserva, fecha, direccion, horario) VALUES
    (1, '2026-11-15', 'Av. Colon 1234, Cordoba', '13:00:00'),
    (3, '2026-12-05', 'Ruta 20 Km 8, Cordoba',   '20:00:00');

INSERT INTO senia_pago (id_reserva, monto, fecha, medio_de_pago, estado) VALUES
    (1, 30000.00, '2026-10-01', 'Transferencia', 'Registrada'),
    (3, 50000.00, '2026-10-10', 'Efectivo',      'Registrada');

-- ---------- CONSULTAS SIMPLES ----------

-- Listado general de reservas
SELECT * FROM reserva;

-- Consultar disponibilidad de una fecha puntual (RF-01)
SELECT COUNT(*) AS reservas_confirmadas
FROM reserva
WHERE fecha = '2026-11-15' AND estado = 'Confirmada';

-- Historial de eventos de un cliente puntual (RF-06)
SELECT r.id_reserva, r.fecha, r.estado, m.nombre AS menu
FROM reserva r
JOIN menu_paquete m ON m.id_menu = r.id_menu
WHERE r.id_cliente = 1;

-- ---------- ACTUALIZACION (cambio de estado) ----------

-- Confirmar una reserva pendiente (RF-03)
UPDATE reserva SET id_admin = 1, estado = 'Confirmada' WHERE id_reserva = 2;

-- ---------- BORRADO ----------

-- Cancelar (dar de baja logicamente) una reserva pendiente
UPDATE reserva SET estado = 'Cancelada' WHERE id_reserva = 2;

-- Borrado fisico de un registro de ejemplo (uso administrativo / pruebas)
DELETE FROM senia_pago WHERE id_senia = 2;

-- ============================================================
-- Presentacion de consultas SQL (consultas de analisis del negocio)
-- ============================================================

-- 1) Calendario de eventos confirmados de un mes determinado (RF-08)
SELECT r.fecha, ev.horario, ev.direccion, c.nombre AS cliente, r.cant_comensales
FROM reserva r
JOIN evento  ev ON ev.id_reserva = r.id_reserva
JOIN cliente c  ON c.id_cliente  = r.id_cliente
WHERE r.estado = 'Confirmada'
  AND MONTH(r.fecha) = 11 AND YEAR(r.fecha) = 2026
ORDER BY r.fecha;

-- 2) Facturacion estimada por menu (suma de comensales x precio, reservas confirmadas)
SELECT m.nombre AS menu,
       COUNT(r.id_reserva)                              AS cant_reservas,
       SUM(r.cant_comensales)                           AS total_comensales,
       SUM(r.cant_comensales * m.precio_persona)         AS monto_estimado
FROM reserva r
JOIN menu_paquete m ON m.id_menu = r.id_menu
WHERE r.estado = 'Confirmada'
GROUP BY m.nombre
ORDER BY monto_estimado DESC;

-- 3) Clientes con reservas sin seña registrada (seguimiento de cobranza)
SELECT c.nombre, c.telefono, r.id_reserva, r.fecha
FROM reserva r
JOIN cliente c ON c.id_cliente = r.id_cliente
LEFT JOIN senia_pago s ON s.id_reserva = r.id_reserva
WHERE r.estado = 'Confirmada' AND s.id_senia IS NULL;

-- 4) Cantidad de reservas por estado (tablero para el administrador)
SELECT estado, COUNT(*) AS cantidad
FROM reserva
GROUP BY estado;

-- 5) Menu mas solicitado (ranking)
SELECT m.nombre, COUNT(*) AS veces_solicitado
FROM reserva r
JOIN menu_paquete m ON m.id_menu = r.id_menu
GROUP BY m.nombre
ORDER BY veces_solicitado DESC
LIMIT 3;
