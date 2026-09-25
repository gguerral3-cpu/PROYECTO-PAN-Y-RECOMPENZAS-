# Base de datos Oracle - Pan, Puntos y Premios

## Orden de ejecucion

| # | Script | Contenido |
|---|--------|-----------|
| 0 | `00_reset.sql` | Elimina objetos existentes (usar solo en desarrollo) |
| 1 | `01_secuencias.sql` | 10 secuencias `SEQ_*` |
| 2 | `02_tablas.sql` | 11 tablas con PK y UNIQUE |
| 3 | `03_claves_foraneas.sql` | 13 claves foraneas |
| 4 | `04_restricciones.sql` | 30 restricciones CHECK |
| 5 | `05_indices.sql` | 9 indices |
| 6 | `06_pkg_inventario.sql` | Paquete `PK_INVENTARIO` |
| 7 | `07_pkg_puntos.sql` | Paquete `PK_PUNTOS` |
| 8 | `08_triggers.sql` | 4 triggers de integridad |
| 9 | `09_datos_prueba.sql` | Catalogo, clientes, historial de ventas |
| 10 | `10_vistas.sql` | 4 vistas para reportes |
| 11 | `11_reportes.sql` | Los 6 reportes obligatorios + historiales |
| 12 | `12_pruebas_validacion.sql` | 8 pruebas de reglas de negocio |

`instalar.sql` ejecuta los 13 scripts en orden (del 00 al 12) y termina mostrando los
resultados de las pruebas. Cada prueba imprime `ERROR ESPERADO` si la reglaactuo
correctamente, o `FALLO: ...` si la regla no se esta aplicando, de modo que un fallo de
integridad queda a la vista en lugar de pasar inadvertido.

Ejecucion completa en un solo paso:

```
sqlplus PANPUNTOS/123456@XEPDB1 @instalar.sql
```

Si `00_reset.sql` se ejecuta por separado, es seguro tanto si el esquema existe como si la
base esta vacia: ignora los objetos que no encuentra.

## Diagrama entidad-relacion

```
CLIENTE
  id_cliente (PK)
  dpi (UQ)
  nombre
  telefono
  email
  saldo_puntos
  estado              ACTIVO | INACTIVO
  fecha_registro
     |
     | 1
     |
     | N
PEDIDO
  id_pedido (PK)
  id_cliente (FK)
  fecha_pedido
  subtotal
  total
  puntos_generados
  estado              PENDIENTE | PAGADO | ENTREGADO | ANULADO
     |
     | 1
     |
     | N
DETALLE_PEDIDO
  id_detalle (PK)
  id_pedido (FK)
  tipo_item            PRODUCTO | MENU
  id_producto (FK, nullable)
  id_menu     (FK, nullable)
  cantidad
  precio_unitario
  subtotal
  puntos
     |
     +---- PAGO
     |       id_pago (PK)
     |       id_pedido (FK, UQ logica)
     |       tipo_pago         EFECTIVO | TARJETA
     |       monto
     |       efectivo_recibido
     |       cambio
     |       referencia
     |       estado            APROBADO | RECHAZADO
     |       fecha_pago
     |
     +---- MOVIMIENTO_PUNTOS
     |       id_movimiento (PK)
     |       id_cliente (FK)
     |       tipo_movimiento   ACUMULACION | CANJE | AJUSTE
     |       puntos            con signo
     |       saldo_resultante
     |       referencia
     |       fecha_movimiento
     |
     +---- CANJE
             id_canje (PK)
             id_cliente (FK)
             id_recompensa (FK)
             puntos_usados
             fecha_canje

PRODUCTO
  id_producto (PK)
  codigo (UQ)
  nombre
  categoria           SANDWICH | BEBIDA | ACOMPANAMIENTO
  precio_unitario
  existencia
  estado              ACTIVO | INACTIVO
     |
     | N
     |
     | N
DETALLE_MENU ----> MENU
  id_menu (PK/FK)       id_menu (PK)
  id_producto (PK/FK)   codigo (UQ)
  cantidad              nombre
                        precio_menu
                        estado

RECOMPENSA
  id_recompensa (PK)
  codigo (UQ)
  nombre
  tipo_item             PRODUCTO | MENU
  id_producto (FK, nullable)
  id_menu     (FK, nullable)
  puntos_necesarios     10 | 15 | 30 | 50
  estado

MOVIMIENTO_INVENTARIO
  id_movimiento (PK)
  id_producto (FK)
  tipo_movimiento       ABASTECIMIENTO | VENTA | CANJE | AJUSTE
  cantidad              con signo
  existencia_anterior
  existencia_nueva
  referencia
  fecha_movimiento
```

## Reglas de integridad implementadas en la base de datos

| Regla | Mecanismo |
|-------|-----------|
| Un pedido PENDIENTE no descuenta inventario | El descuento se ejecuta en `TRG_PEDIDO_PAGO` al pasar a PAGADO |
| Un menu descuenta 1 unidad de cada uno de sus 3 componentes | `PK_INVENTARIO.DESCONTAR_MENU` |
| No se venden productos desactivados | `PK_INVENTARIO.DESCONTAR` valida `ESTADO` |
| No se venden sin existencias | `PK_INVENTARIO.DESCONTAR` valida y usa `SELECT ... FOR UPDATE` |
| El saldo de puntos nunca queda negativo | `PK_PUNTOS.DESCONTAR` valida con el saldo bloqueado |
| El saldo siempre coincide con los movimientos | `TRG_MOV_PUNTOS_SALDO` actualiza `CLIENTE.SALDO_PUNTOS` |
| Un cliente con compras no se elimina | `TRG_CLIENTE_PROTEGIDO` |
| Transiciones de pedido | `TRG_PEDIDO_ESTADO`: PENDIENTE -> PAGADO -> ENTREGADO, PENDIENTE -> ANULADO |
| Un pedido pagado no se anula | `TRG_PEDIDO_ESTADO` |
| Todo el historial queda registrado | `MOVIMIENTO_INVENTARIO` y `MOVIMIENTO_PUNTOS` |

## Codigos de error que usa la aplicacion

| Codigo | Significado |
|--------|-------------|
| -20001 | El producto indicado no existe |
| -20002 | El producto esta desactivado |
| -20003 | Existencias insuficientes |
| -20004 | Cantidad de abastecimiento no positiva |
| -20005 | El menu indicado no existe |
| -20006 | El menu esta desactivado |
| -20007 | El menu no tiene componentes configurados |
| -20010 | Puntos a acreditar no positivos |
| -20011 | El cliente indicado no existe |
| -20012 | Puntos a descontar no positivos |
| -20013 | Puntos insuficientes |
| -20020 | No se puede eliminar un cliente con movimientos |
| -20021 | Transicion de estado no permitida |
