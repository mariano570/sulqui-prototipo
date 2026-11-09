-- ============================================================
-- Sulqui - Datos de ejemplo
-- Trabajo Practico N.2 - Seminario de practica
-- Ejecutar despues de 01_sulqui_schema.sql
-- ============================================================
USE sulqui_db;

-- Contrasenia de ejemplo del administrador: sulqui2026 (se guarda su hash SHA-256)
INSERT INTO administrador (usuario, contrasenia) VALUES
    ('mariano', SHA2('sulqui2026', 256));

INSERT INTO cliente (nombre, telefono, email) VALUES
    ('Ana Gomez',       '3517001111', 'ana.gomez@example.com'),
    ('Carlos Perez',    '3517002222', 'carlos.perez@example.com'),
    ('Lucia Fernandez', '3517003333', 'lucia.fernandez@example.com');

INSERT INTO menu_paquete (nombre, precio_persona, tipo_preparacion) VALUES
    ('Asado clasico',       8500.00, 'Asado'),
    ('Comida al disco',     7200.00, 'Al disco'),
    ('Parrillada premium', 11000.00, 'Asado');

INSERT INTO reserva (id_cliente, id_admin, id_menu, fecha, cant_comensales, estado) VALUES
    (1, 1,    1, '2026-11-15', 25, 'Confirmada'),
    (2, NULL, 2, '2026-11-22', 15, 'Pendiente'),
    (3, 1,    3, '2026-12-05', 40, 'Confirmada'),
    (1, NULL, 3, '2026-12-12', 60, 'Pendiente');

INSERT INTO evento (id_reserva, fecha, direccion, horario) VALUES
    (1, '2026-11-15', 'Av. Colon 1234, Cordoba', '13:00:00'),
    (3, '2026-12-05', 'Ruta 20 Km 8, Cordoba',   '20:00:00');

INSERT INTO senia_pago (id_reserva, monto, fecha, medio_de_pago, estado) VALUES
    (1, 30000.00, '2026-10-01', 'Transferencia', 'Registrada');
