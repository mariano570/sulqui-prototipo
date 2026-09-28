# Sulqui – Prototipo (TP2, Seminario de Práctica)

Prototipo Java + MySQL correspondiente a la Etapa 2 del proyecto Sulqui
(Sistema de Gestión de Reservas y Eventos Gastronómicos a Fuego).

## Contenido
- `sql/sulqui_schema.sql`: creación de la base de datos y las tablas (MySQL).
- `sql/sulqui_consultas.sql`: inserciones, consultas, borrados y consultas SQL de ejemplo.
- `src/main/java/com/sulqui/modelo`: clases de dominio (Cliente, Reserva, Evento, MenuPaquete, SeniaPago, Administrador).
- `src/main/java/com/sulqui/dao`: clases de acceso a datos (JDBC) con operaciones CRUD.
- `src/main/java/com/sulqui/servicio`: reglas de negocio (validación de disponibilidad, notificaciones).
- `src/main/java/com/sulqui/app`: clase Main de demostración del prototipo.

## Requisitos
- JDK 17+
- MySQL 8+
- Conector JDBC de MySQL (mysql-connector-j) en el classpath.

## Configuración
Editar `src/main/java/com/sulqui/dao/Conexion.java` con la URL, usuario y contraseña
de la base de datos local.

## Ejecución
1. Ejecutar `sql/sulqui_schema.sql` en MySQL para crear la base de datos y las tablas.
2. Ejecutar `sql/sulqui_consultas.sql` (opcional) para cargar datos de ejemplo.
3. Compilar y ejecutar `com.sulqui.app.Main`.
