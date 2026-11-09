-- ============================================================
-- Sulqui - Esquema de base de datos (MySQL)
-- Seminario de practica - Proyecto integrador (AP4)
-- Orden de ejecucion: 01_schema -> 02_datos_ejemplo -> (03_operaciones_crud, 04_consultas_negocio)
-- ============================================================

DROP DATABASE IF EXISTS sulqui_db;
CREATE DATABASE sulqui_db CHARACTER SET utf8mb4;
USE sulqui_db;

-- Tabla: cliente
CREATE TABLE cliente (
    id_cliente     INT AUTO_INCREMENT PRIMARY KEY,
    nombre         VARCHAR(100) NOT NULL,
    telefono       VARCHAR(20)  NOT NULL,
    email          VARCHAR(100) NOT NULL UNIQUE
);

-- Tabla: administrador
CREATE TABLE administrador (
    id_admin       INT AUTO_INCREMENT PRIMARY KEY,
    usuario        VARCHAR(50)  NOT NULL UNIQUE,
    contrasenia    VARCHAR(255) NOT NULL   -- hash SHA-256 en hexadecimal
);

-- Tabla: menu_paquete
CREATE TABLE menu_paquete (
    id_menu           INT AUTO_INCREMENT PRIMARY KEY,
    nombre            VARCHAR(100)   NOT NULL,
    precio_persona    DECIMAL(10,2)  NOT NULL,
    tipo_preparacion  VARCHAR(50)    NOT NULL,
    -- AP4: defensa en la base para el precio (tambien lo valida MenuService)
    CONSTRAINT chk_precio CHECK (precio_persona > 0)
);

-- Tabla: reserva
CREATE TABLE reserva (
    id_reserva       INT AUTO_INCREMENT PRIMARY KEY,
    id_cliente       INT NOT NULL,
    id_admin         INT NULL,
    id_menu          INT NOT NULL,
    fecha            DATE NOT NULL,
    cant_comensales  INT NOT NULL,
    estado           VARCHAR(20) NOT NULL DEFAULT 'Pendiente',
    fecha_solicitud  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reserva_cliente FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente),
    CONSTRAINT fk_reserva_admin   FOREIGN KEY (id_admin)   REFERENCES administrador(id_admin),
    CONSTRAINT fk_reserva_menu    FOREIGN KEY (id_menu)    REFERENCES menu_paquete(id_menu),
    CONSTRAINT chk_estado CHECK (estado IN ('Pendiente','Confirmada','Rechazada','Cancelada','Finalizada')),
    -- RN-02: misma regla que valida ReservaService (particion de equivalencia [10..150])
    CONSTRAINT chk_comensales CHECK (cant_comensales BETWEEN 10 AND 150)
    -- Regla de negocio "no permitir dos reservas confirmadas para la misma fecha" se valida
    -- en la capa de servicio (ReservaService), antes de confirmar, ya que SQL estandar no
    -- permite expresar una unicidad condicional (solo para estado = 'Confirmada') de forma directa.
);

-- Tabla: evento
CREATE TABLE evento (
    id_evento    INT AUTO_INCREMENT PRIMARY KEY,
    id_reserva   INT NOT NULL UNIQUE,
    fecha        DATE NOT NULL,
    direccion    VARCHAR(150) NOT NULL,
    horario      TIME NOT NULL,
    CONSTRAINT fk_evento_reserva FOREIGN KEY (id_reserva) REFERENCES reserva(id_reserva)
);

-- Tabla: senia_pago
CREATE TABLE senia_pago (
    id_senia        INT AUTO_INCREMENT PRIMARY KEY,
    id_reserva      INT NOT NULL,
    monto           DECIMAL(10,2) NOT NULL,
    fecha           DATE NOT NULL,
    medio_de_pago   VARCHAR(30) NOT NULL,
    estado          VARCHAR(20) NOT NULL DEFAULT 'Registrada',
    CONSTRAINT fk_senia_reserva FOREIGN KEY (id_reserva) REFERENCES reserva(id_reserva)
);

CREATE INDEX idx_reserva_fecha ON reserva(fecha);
CREATE INDEX idx_reserva_cliente ON reserva(id_cliente);

-- Usuario de aplicacion con permisos minimos (la app de escritorio no usa root)
-- CREATE USER 'sulqui_app'@'localhost' IDENTIFIED BY 'cambiar_esta_clave';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON sulqui_db.* TO 'sulqui_app'@'localhost';
