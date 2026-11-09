-- ============================================================
-- Sulqui - Consultas SQL de negocio (apartado 7.5)
-- Trabajo Practico N.2 - Seminario de practica
-- Las consultas 1 a 3 se presentan en el documento; la 4 y la 5
-- se documentan aqui como complemento.
-- ============================================================
USE sulqui_db;

-- 1) Calendario de eventos confirmados de un mes determinado (RF-08)
SELECT r.fecha, ev.horario, ev.direccion, c.nombre AS cliente, r.cant_comensales
FROM reserva r
JOIN evento  ev ON ev.id_reserva = r.id_reserva
JOIN cliente c  ON c.id_cliente  = r.id_cliente
WHERE r.estado = 'Confirmada'
  AND MONTH(r.fecha) = 11 AND YEAR(r.fecha) = 2026
ORDER BY r.fecha;

-- 2) Facturacion estimada por menu sobre reservas confirmadas
SELECT m.nombre AS menu,
       COUNT(r.id_reserva)                       AS cant_reservas,
       SUM(r.cant_comensales)                    AS total_comensales,
       SUM(r.cant_comensales * m.precio_persona) AS monto_estimado
FROM reserva r
JOIN menu_paquete m ON m.id_menu = r.id_menu
WHERE r.estado = 'Confirmada'
GROUP BY m.nombre
ORDER BY monto_estimado DESC;

-- 3) Reservas confirmadas sin senia registrada (seguimiento de cobranza)
SELECT c.nombre, c.telefono, r.id_reserva, r.fecha
FROM reserva r
JOIN cliente c ON c.id_cliente = r.id_cliente
LEFT JOIN senia_pago s ON s.id_reserva = r.id_reserva
WHERE r.estado = 'Confirmada' AND s.id_senia IS NULL;

-- 4) Cantidad de reservas por estado (tablero del administrador)
--    Necesidad: saber de un vistazo cuantas solicitudes esperan respuesta.
SELECT estado, COUNT(*) AS cantidad
FROM reserva
GROUP BY estado
ORDER BY cantidad DESC;

-- 5) Ranking de menus mas solicitados (todas las reservas, cualquier estado)
--    Necesidad: decidir que menus destacar o actualizar en el catalogo.
SELECT m.nombre, COUNT(*) AS veces_solicitado
FROM reserva r
JOIN menu_paquete m ON m.id_menu = r.id_menu
GROUP BY m.nombre
ORDER BY veces_solicitado DESC
LIMIT 3;
