# Arquitectura

## Capas

El proyecto sigue tres capas, con una cuarta transversal para la API.

```
Vista (Swing)  ->  Servicio  ->  DAO  ->  Oracle
                                  ^
API REST  ------>  Servicio  ------|
```

- `view`: ventanas Swing. No escriben SQL, solo llaman servicios y muestran el resultado.
  Cada ventana corresponde a un modulo del enunciado.
- `service`: contiene las reglas de negocio y abre una transaccion por operacion con
  `Transaccion.ejecutar`. Si algo falla, se hace `rollback` y se propaga la excepcion.
- `dao`: ejecuta SQL parametrizado con `PreparedStatement`. No abre ni cierra conexiones;
  recibe una `Connection` ya iniciada por el servicio.
- `util`: `ConexionOracle` (pool y propiedades), `Transaccion`, `Formato`, `Validaciones`,
  `Comprobante`, `ErroresOracle` y `Json`.
- `api`: expone los servicios por HTTP usando `com.sun.net.httpserver`, sin dependencias
  externas.

## Modelo POO

`ItemVenta` es la clase abstracta que representa cualquier renglon de un pedido. Encapsula
codigo, descripcion, cantidad y precio unitario, y define el comportamiento que cambia segun
el tipo de producto:

- `calcularPuntos()`: `ProductoIndividual` usa los puntos de la categoria, `MenuCompleto` usa
  la constante `Menu.PUNTOS_POR_MENU` (8).
- `unidadesDeInventario()`: `ProductoIndividual` devuelve su propio id, `MenuCompleto`
  devuelve el mapa de componentes multiplicado por la cantidad.

Ese unico metodo es lo que permite que `PK_INVENTARIO.DESCONTAR_MENU` trate un menu sin
conocer sus componentes: el DAO arma el mapa y el paquete lo descuenta.

Al leer un pedido guardado, los items se reconstruyen con `reconstruirItem`, que no vuelve a
validar existencia ni estado. Asi un pedido pagado sigue pudiéndose consultar aunque el
producto se haya desactivado o su existencia haya bajado.

## Flujo de cobro

`PagoService.cobrar` es el punto mas delicado del sistema:

1. Abre transaccion y carga el pedido.
2. Si el pago es con tarjeta, registra primero el intento en `PAGO` como `RECHAZADO`.
   Si el tarjeta falla, el pedido sigue `PENDIENTE`, no se toca inventario ni puntos, y la
   transaccion se confirma para conservar el intento.
3. Valida que el pedido sea `PENDIENTE` y que el monto coincida con el total.
4. En efectivo, valida que el efectivo recibido cubra el total.
5. Vuelve a validar existencias, porque pudieron cambiar entre que se creo el pedido y se
   cobro.
6. Cambia el estado del pedido a `PAGADO`.

A partir del paso 6 el trabajo pesado lo hace `TRG_PEDIDO_PAGO`, que descuenta inventario
(con `PK_INVENTARIO`, componente a componente si hay menus) y acredita los puntos con
`PK_PUNTOS`. Java no duplica esa logica: solo solicita el cambio de estado dentro de la misma
transaccion. Si el trigger falla por existencias o puntos, la excepcion sube, Java hace
`rollback` y el pedido queda `PENDIENTE` como estaba.

## Flujo de canje

`FidelizacionService.canjear` valida cliente activo, recompensa activa y saldo suficiente;
descuenta puntos con `PK_PUNTOS.DESCONTAR`, entrega el producto o menu segun el tipo de
recompensa con `PK_INVENTARIO`, y registra el `CANJE`. Las tres cosas en una sola
transaccion: si algo falla, el cliente conserva sus puntos.

## Donde esta la integridad

La decision de diseno es no duplicar reglas en dos lugares. Oracle es la fuente de verdad:

| Regla | Mecanismo |
|-------|-----------|
| No vender sin existencias | `PK_INVENTARIO.DESCONTAR` con `SELECT ... FOR UPDATE` |
| No vender desactivados | `PK_INVENTARIO.DESCONTAR` valida `ESTADO` |
| Un menu descuenta sus componentes | `PK_INVENTARIO.DESCONTAR_MENU` |
| El saldo nunca es negativo | `PK_PUNTOS.DESCONTAR` valida con el saldo bloqueado |
| El saldo siempre cuadra | `TRG_MOV_PUNTOS_SALDO` actualiza `CLIENTE.SALDO_PUNTOS` |
| Transiciones validas | `TRG_PEDIDO_ESTADO` |
| No borrar clientes con historial | `TRG_CLIENTE_PROTEGIDO` |

Java valida tambien antes de enviar, para dar mensajes claros al usuario, pero la garantia
real viene de la base: si alguien inserta con SQL a mano, la base lo rechaza.

## Transacciones

`Transaccion.ejecutar` hace `setAutoCommit(false)`, ejecuta la operacion y hace `commit`.
Ante cualquier excepcion hace `rollback`, traduce el error de Oracle con `ErroresOracle` y
cierra la conexion. `Transaccion.ejecutarVoid` es la variante para operaciones sin resultado.

## Puntos extra

- **API REST**: `panpuntos.api.ApiPuntos` expone saldo e historial por DPI. Solo lectura.
  La app Android nunca habla con Oracle, solo con esta API.
- **Exportacion CSV**: `ReportesFrame` serializa el `TableModel` que ya esta en pantalla, asi
  que el archivo exportado coincide exactamente con lo que ve el usuario.
- **Sin dependencias externas**: el JSON se genera con `panpuntos.util.Json`, asi el
  proyecto corre con el JDK y el driver de Oracle, sin Gson ni otro jar.
