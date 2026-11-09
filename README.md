# Sulqui – Proyecto integrador (AP4, Seminario de Práctica)

Aplicación **de escritorio en Java (Swing) con MySQL** para el proyecto Sulqui
(Sistema de Gestión de Reservas y Eventos Gastronómicos a Fuego).

La misma aplicación se instala en dos equipos de la red local:

- **PC del administrador**: aloja la base de datos MySQL y se usa en modo *Administración*.
- **Terminal de clientes**: PC del local donde el cliente consulta fechas, pide su reserva y ve su historial.

## Novedades de la entrega 4 (proyecto integrador)

- **Patrón de diseño DAO + Singleton**: interfaz `Dao<T>`, clase abstracta `DaoJdbc<T>` (métodos plantilla
  reutilizables), un DAO por tabla (incluye el nuevo `EventoDAO`) y `Conexion` como Singleton.
- **Excepciones de MySQL**: `PersistenciaException` (subclase de `SQLException`) clasifica los errores
  (conexión, duplicado, referencia, restricción CHECK, dato inválido) y los muestra con un mensaje claro.
- **Transacciones**: confirmar una reserva registra el evento (dirección y horario) en la misma transacción;
  restaurar el catálogo desde un archivo es "todo o nada" (commit / rollback).
- **Arreglos y ArrayList**: los DAO devuelven `ArrayList`; `ReporteService` arma arreglos de 12 meses,
  un conteo por estado y la grilla del calendario (`int[][]`).
- **Archivos**: exportación de reservas a CSV, respaldo y restauración del catálogo de menús
  (`com.sulqui.archivo`).
- Interfaz: nueva pestaña *Reportes y archivos* (P-10) y diálogo de lugar y horario; menú de consola con
  las opciones 10 a 13.
- 43 pruebas automatizadas (P1 a P34).

## Estructura

```
sql/
  01_sulqui_schema.sql            creación de la base y las tablas
  02_sulqui_datos_ejemplo.sql     datos de ejemplo
  03_sulqui_operaciones_crud.sql  inserción, consulta, actualización y borrado
  04_sulqui_consultas_negocio.sql las cinco consultas de negocio
src/main/java/com/sulqui/
  modelo/    entidades (POJO sin lógica de negocio) + EstadoReserva
  dao/       patrón DAO: Dao<T>, DaoJdbc<T>, un DAO por tabla, Conexion (Singleton), PersistenciaException
  servicio/  lógica de negocio: ReservaService, ClienteService, PagoService,
             MenuService, AutenticacionService, NotificacionService
  ui/        interfaz Swing: VentanaInicio, VentanaCliente, DialogoLogin, VentanaAdministrador
  app/       MenuConsola: menú de selección por consola
  estructuras/  ListaEnlazada, Pila, Cola (implementación propia) y Algoritmos
  notificacion/ CanalNotificacion (abstracta), CanalEmail, CanalWhatsApp
  archivo/   ArchivoCsv<T> (abstracta), MenuCsv, ReservaCsv, ArchivoException
src/test/java/com/sulqui/
  CasosDePruebaTest.java          casos P1 a P8
  ParticionComensalesTest.java    P9: partición de equivalencia con valores frontera
  EstructurasYAlgoritmosTest.java P10 a P15: pila, cola, lista, merge sort, búsqueda binaria
  PooTest.java                    P16 a P19: herencia, polimorfismo, encapsulamiento, excepciones
  IntegracionAP3Test.java         P20 a P24: estructuras y algoritmos con la base de datos
  MenuConsolaTest.java            P25: sesión completa del menú de consola
  IntegracionAP4Test.java         P26 a P34: Singleton, Dao<T>, transacciones, excepciones de MySQL,
                                  arreglos y archivos
  ui/CapturaPantallas.java        genera las capturas de evidencia
evidencia/
  resultado_pruebas.txt           salida de la ejecución de las pruebas
  sesion_menu_consola.txt         sesión grabada del menú de consola
  pantallas/                      capturas de las pantallas reales
  archivos/                       ejemplos de los archivos CSV generados
docs/diagramas/                   fuentes PlantUML y PNG de todos los diagramas
```

## Requisitos

- JDK 17 o superior
- MySQL 8 (o MariaDB 10.6+)
- Maven 3.9 (opcional, para compilar y generar el .jar)

## Instalación

1. Ejecutar en MySQL `sql/01_sulqui_schema.sql` y luego `sql/02_sulqui_datos_ejemplo.sql`.
2. Copiar `config/sulqui.properties.example` como `config/sulqui.properties` y completar URL, usuario y contraseña.
3. Compilar: `mvn package` → genera `target/sulqui-app.jar`.
4. Ejecutar la interfaz gráfica: `java -jar target/sulqui-app.jar`.
5. Ejecutar el menú por consola: `java -jar target/sulqui-app.jar --consola`.

También puede abrirse como proyecto Maven en IntelliJ IDEA, NetBeans o Eclipse y ejecutar
`com.sulqui.ui.SulquiApp` (Swing) o `com.sulqui.app.MenuConsola` (consola).

Usuario de ejemplo del administrador: `mariano` / `sulqui2026`.

## Pruebas

Las pruebas crean su propia base `sulqui_test` (no tocan `sulqui_db`):

```
mvn test -Ddb.usuario=root -Ddb.contrasenia=TU_CLAVE
```

## Reglas de negocio implementadas

| Regla | Dónde se valida |
|---|---|
| RN-01 No hay dos reservas confirmadas en la misma fecha | `ReservaService.consultarDisponibilidad / confirmarReserva` |
| RN-02 Entre 10 y 150 comensales | `ReservaService.validarComensales` y `CHECK chk_comensales` |
| RN-03 La fecha del evento debe ser futura | `ReservaService.solicitarReserva` |
| RN-04 Solo se confirma o rechaza una reserva pendiente | `ReservaService.confirmarReserva / rechazarReserva` |
| RN-05 Solo se registran señas de reservas confirmadas | `PagoService.registrarPago` |

## Archivos que genera la aplicación

Se guardan en la carpeta `datos/` (junto al .jar):

- `datos/exportaciones/reservas_AAAA-MM-DD.csv`: listado de reservas para planilla de cálculo.
- `datos/respaldos/catalogo_menus.csv`: respaldo del catálogo; se restaura desde la opción 13 del menú
  o desde la pestaña *Reportes y archivos*.
