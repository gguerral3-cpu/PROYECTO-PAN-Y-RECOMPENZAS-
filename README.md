# Pan, Puntos y Premios

Sistema de ventas, inventario y fidelizacion para una panaderia, desarrollado en Java con
interfaz Swing y base de datos Oracle. Incluye un programa de puntos por compra, canje de
recompensas, seis reportes de `JTable` y, como punto extra, una API REST y una app Android
para consultar el saldo de puntos de un cliente por DPI.

## Modulos

| # | Modulo | Que hace |
|---|--------|----------|
| 01 | Administracion de clientes | Alta, edicion, busqueda por nombre o DPI, activacion y baja logica |
| 02 | Administracion de productos | Sandwiches, bebidas y acompanamientos, y menus completos con sus componentes |
| 03 | Inventario y abastecimiento | Abastecimiento con validacion, movimientos e indicador de existencias criticas |
| 04 | Gestion de pedidos | Carrito de venta, calculo de subtotal, puntos a generar y confirmacion |
| 05 | Cobro de pedidos | Cobro en efectivo (con cambio) o tarjeta (aprobado o rechazado) |
| 06 | Fidelizacion y canjes | Saldo de puntos, catalogo de recompensas e historial de canjes |
| 07 | Reportes | Seis reportes filtrables por rango de fechas y exportacion a CSV |

## Reglas de negocio

- Cada sandwich genera 2 puntos y cada menu genera 8 puntos.
- Un menu descuenta una unidad de cada uno de sus componentes, no una cuarta unidad del menu.
- Un pedido pasa por `PENDIENTE -> PAGADO -> ENTREGADO` o `PENDIENTE -> ANULADO`.
- Un pedido `PENDIENTE` no descuenta inventario; el descuento ocurre al cobrar.
- Un pedido pagado no puede anularse ni editarse.
- Un cliente con compras o movimientos no se elimina, solo se inactiva.
- El saldo de puntos nunca puede quedar negativo.
- Las recompensas cuestan 10, 15, 30 o 50 puntos.
- Si el pago con tarjeta es rechazado, el pedido vuelve a `PENDIENTE` y no se genera ninguna venta.

## Estructura del proyecto

```
PanPuntosPremios/
  database/                 scripts de Oracle
  docs/                     diagramas UML y entidad-relacion
  lib/                      driver JDBC de Oracle (ojdbc11.jar)
  src/main/java/panpuntos/
    Main.java               punto de entrada de la aplicacion Swing
    model/                  clases de dominio
    dao/                    acceso a datos con PreparedStatement
    service/                reglas de negocio y transacciones
    view/                   ventanas Swing
    api/                    API REST de consulta de puntos
    util/                   conexion, formatos, validaciones, comprobantes
  src/main/resources/       database.properties
  android/                  app Android de consulta de puntos
  compilar.ps1              compila el proyecto
  ejecutar.ps1              ejecuta la aplicacion o la API
  pom.xml                   proyecto Maven
```

## Requisitos

- JDK 21 o superior.
- Oracle Database 19c, 21c o XE.
- Driver JDBC `ojdbc11.jar` en la carpeta `lib/`.
- Maven es opcional: el proyecto se compila tambien con `compilar.ps1`.
- Android Studio, solo si se quiere compilar la app Android.

### Como obtener el driver de Oracle

El archivo `ojdbc11.jar` **no esta incluido en este repositorio** porque pesa
7 MB y es un binario de terceros. Descarguelo una sola vez y coloquelo en la
carpeta `lib/` antes de compilar:

1. Baje **Oracle JDBC Driver for JDK 21** desde el sitio oficial de Oracle:
   <https://www.oracle.com/database/technologies/appdev/jdbc-downloads.html>
2. Copie el archivo `ojdbc11.jar` dentro de la carpeta `lib/` del proyecto.
3. Verifique que quede en `lib/ojdbc11.jar`.

Si prefiere dejarlo en otra ruta, los scripts la aceptan como parametro:

```powershell
.\compilar.ps1 -RutaDriver "C:\ruta\ojdbc11.jar"
.\ejecutar.ps1 -RutaDriver "C:\ruta\ojdbc11.jar"
```

## Configuracion de la base de datos

El usuario `PANPUNTOS` debe existir antes de instalar el esquema. Conectese como
administrador y ejecute:

```sql
CREATE USER PANPUNTOS IDENTIFIED BY 123456;
GRANT CONNECT, RESOURCE TO PANPUNTOS;
GRANT CREATE SESSION, CREATE TABLE, CREATE SEQUENCE, CREATE VIEW,
      CREATE TRIGGER, CREATE PROCEDURE TO PANPUNTOS;
ALTER USER PANPUNTOS QUOTA UNLIMITED ON USERS;
```

Luego instale el esquema completo:

```
sqlplus PANPUNTOS/123456@XEPDB1 @database/instalar.sql
```

Ajuste despues `src/main/resources/database.properties` si su instalacion es distinta:

```properties
db.url=jdbc:oracle:thin:@localhost:1521/XEPDB1
db.usuario=PANPUNTOS
db.password=123456
db.driver=oracle.jdbc.OracleDriver
api.puerto=8080
```

## Compilacion y ejecucion

Con Maven:

```
mvn clean package
mvn exec:java -Dexec.mainClass=panpuntos.Main
```

Con los scripts de PowerShell:

```
.\compilar.ps1
.\ejecutar.ps1
```

Si el driver esta en otra ruta:

```
.\compilar.ps1 -RutaDriver "C:\ruta\ojdbc11.jar"
.\ejecutar.ps1 -RutaDriver "C:\ruta\ojdbc11.jar"
```

## API de consulta de puntos

Arranque la API antes de abrir la app Android:

```
.\ejecutar.ps1 -Api
```

| Metodo | Ruta | Descripcion |
|--------|------|-------------|
| GET | `/api/salud` | Estado del servicio y de la conexion |
| GET | `/api/recompensas` | Catalogo de recompensas disponibles |
| GET | `/api/clientes/{dpi}` | Datos y saldo del cliente |
| GET | `/api/clientes/{dpi}/puntos` | Saldo y recompensas que el cliente puede canjear |
| GET | `/api/clientes/{dpi}/historial` | Historial de movimientos de puntos |

Ejemplo de respuesta:

```json
{
  "dpi": "1001",
  "nombre": "Ana Torres",
  "puntos": 28,
  "estado": "ACTIVO",
  "recompensas": [
    { "codigo": "R-GALLETA", "nombre": "Galleta de avena", "puntos": 10, "estado": "ACTIVO" }
  ]
}
```

## App Android

Abra la carpeta `android/` en Android Studio y ejecute el modulo `app`. La app busca en
`http://10.0.2.2:8080`, que es como un emulador alcanza al equipo. En un telefono fisico
cambie la constante `SERVIDOR` en `ConsultaActivity.java` por la IP local del equipo,
por ejemplo `http://192.168.1.10:8080`, y mantenga la API encendida en el puerto 8080.

## Reportes

| # | Reporte |
|---|---------|
| 1 | Ventas por periodo |
| 2 | Productos mas vendidos |
| 3 | Productos agotados o con bajo inventario |
| 4 | Puntos acumulados por cliente |
| 5 | Historial de canjes |
| 6 | Ventas de menus completos |

Todos se consultan con `JTable`, aceptan rango de fechas y se exportan a CSV.

## Donde vive la logica

Oracle es la fuente de verdad de la integridad. Los triggers y los paquetes implementados
impiden que se rompan las reglas aunque alguien escriba directamente en las tablas:

- `PK_INVENTARIO.ABASTECER` y `PK_INVENTARIO.DESCONTAR` controlan las existencias.
- `PK_INVENTARIO.DESCONTAR_MENU` descuenta los componentes de un menu.
- `PK_PUNTOS.ACREDITAR` y `PK_PUNTOS.DESCONTAR` controlan el saldo de puntos.
- `TRG_PEDIDO_PAGO` descuenta inventario y acredita puntos al pasar a `PAGADO`.
- `TRG_PEDIDO_ESTADO` valida las transiciones de estado.
- `TRG_MOV_PUNTOS_SALDO` mantiene `CLIENTE.SALDO_PUNTOS` sincronizado.
- `TRG_CLIENTE_PROTEGIDO` impide borrar clientes con historial.

Java coordina las transacciones con `Transaccion.ejecutar`, que hace `commit` en exito y
`rollback` ante cualquier excepcion. El detalle esta en `docs/arquitectura.md`.

## Documentacion

- `docs/arquitectura.md`: capas, flujo de cobro y de canje, y decisiones de diseno.
- `docs/modelo-entidad-relacion.puml`: diagrama entidad-relacion.
- `docs/diagrama-clases.puml`: diagrama de clases.
- `docs/casos-uso.puml`: diagrama de casos de uso.
- `database/README.md`: orden de ejecucion de los scripts, diagrama ER en texto y
  codigos de error de Oracle.
