# Manual de archivos del proyecto

Guía de referencia de los 96 archivos del proyecto, qué hace cada uno y con qué programa se abre.

---

## Con qué programa abrir cada tipo

| Extensión | Archivos | Ábrelo con | No hagas esto |
|---|---|---|---|
| `.java` | 66 | **NetBeans** o VS Code | — |
| `.sql` | 14 | **SQL Developer** | — |
| `.puml` | 3 | **NetBeans** (con PlantUML) o VS Code | — |
| `.md` | 5 | VS Code o Bloc de notas | — |
| `.ps1` | 3 | **PowerShell ISE** | Doble clic abre Bloc de notas |
| `.properties` | 1 | NetBeans o Bloc de notas | — |
| `.xml` | 1 | NetBeans o Bloc de notas | — |
| `.bat` | 2 | Bloc de notas (solo para leer) | Doble clic ejecuta |
| `.jar` | 1 | **No se abre** | Es una librería |
| `.gitignore` | 1 | Bloc de notas | — |

### Excepciones importantes

- **`.ps1`**: el doble clic abre el Bloc de notas porque Windows no los asocia con PowerShell. Usa `INICIAR-APP.bat` e `INICIAR-API.bat` para ejecutar sin problema.
- **`.jar`**: es una librería binaria, no tiene nada que ver ni que abrir a mano.
- **`.bat`**: sí se ejecuta con doble clic, pero para leerlo usa el Bloc de notas.
- **`.puml`**: son diagramas en texto. NetBeans los dibuja solo si tiene el plugin PlantUML instalado.

---

## Carpeta raíz (8 archivos)

| Archivo | Qué hace | Abrir con |
|---|---|---|
| `README.md` | Documentación general del proyecto | VS Code |
| `pom.xml` | Configuración de **Maven**: dependencias y versión de Java. No se usa aún porque no hay Maven instalado | NetBeans o Bloc de notas |
| `.gitignore` | Lista de lo que Git debe ignorar: `target/`, `*.class`, `*.jar`, carpetas de IDE | Bloc de notas |
| `compilar.ps1` | Compila los 66 `.java` a `target\classes` usando Java 21 | PowerShell ISE |
| `ejecutar.ps1` | Arranca la aplicación o la API según el parámetro `-Api` | PowerShell ISE |
| `instalar-base-datos.ps1` | Crea el usuario `PANPUNTOS`, da privilegios y ejecuta los 14 scripts SQL. Pide la contraseña de SYSTEM de forma oculta | PowerShell ISE |
| `INICIAR-APP.bat` | Doble clic y abre la aplicación de escritorio | Doble clic |
| `INICIAR-API.bat` | Doble clic y abre la API REST en el puerto 8080 | Doble clic |

---

## `database/` (14 archivos)

Todos se abren y ejecutan con **SQL Developer**. Se ejecutan en orden numérico.

| Archivo | Qué hace |
|---|---|
| `instalar.sql` | Maestro. Llama a los 13 siguientes en el orden correcto |
| `00_reset.sql` | Borra el esquema completo para poder reinstalar desde cero |
| `01_secuencias.sql` | Crea los 10 contadores automáticos de identificadores |
| `02_tablas.sql` | Crea las 11 tablas |
| `03_claves_foraneas.sql` | Define las relaciones entre tablas |
| `04_restricciones.sql` | Restricciones `CHECK`, `UNIQUE` y `NOT NULL` |
| `05_indices.sql` | Crea los 24 índices |
| `06_pkg_inventario.sql` | Paquete `PK_INVENTARIO`: descuenta stock y avisa de existencias bajas |
| `07_pkg_puntos.sql` | Paquete `PK_PUNTOS`: acredita y descuenta puntos |
| `08_triggers.sql` | Los 4 triggers: movimiento de stock, bitácora y validaciones |
| `09_datos_prueba.sql` | Inserta los datos iniciales: 5 clientes, 8 productos, pedidos y pagos |
| `10_vistas.sql` | Las 4 vistas de consulta |
| `11_reportes.sql` | Los 6 reportes y la consulta del historial de puntos |
| `12_pruebas_validacion.sql` | Las 10 pruebas que verifican que las reglas se respetan |
| `README.md` | Mapa de la base de datos y guía de uso |

### Restricciones de estado

| Tabla | Estados permitidos |
|---|---|
| `CLIENTE` | ACTIVO, INACTIVO, SUSPENDIDO |
| `PRODUCTO` | ACTIVO, INACTIVO |
| `MENU` | ACTIVO, INACTIVO |
| `RECOMPENSA` | ACTIVO, INACTIVO |
| `PEDIDO` | PENDIENTE, PAGADO, ENTREGADO, ANULADO |
| `PAGO` | APROBADO, RECHAZADO |

---

## `src/main/java/panpuntos/` (66 clases)

### `Main.java`

| Archivo | Qué hace |
|---|---|
| `Main.java` | Punto de entrada. Verifica la conexión a la base y abre `VentanaPrincipal` |

### `model/` (22 archivos) — Los datos

Cada archivo es una entidad de negocio. Esta capa **no** habla con la base de datos.

| Archivo | Qué representa |
|---|---|
| `Cliente.java` | Cliente del negocio, con su saldo de puntos |
| `EstadoCliente.java` | Catálogo de estados del cliente: ACTIVO, INACTIVO, SUSPENDIDO |
| `EstadoRegistro.java` | Catálogo de estados simple: ACTIVO, INACTIVO. Lo comparten Producto, Menú y Recompensa |
| `EstadoPedido.java` | PENDIENTE, PAGADO, ENTREGADO, ANULADO |
| `EstadoPago.java` | APROBADO, RECHAZADO |
| `Producto.java` | Producto con precio y existencia |
| `Menu.java` | Menú con su precio |
| `Recompensa.java` | Recompensa que se canjea con puntos |
| `Pedido.java` | **Contiene la regla principal**: un pedido exige 2 puntos por cada sándwich |
| `Pago.java` | Registro de un cobro |
| `Canje.java` | Canje de puntos por una recompensa |
| `MovimientoPuntos.java` | Asiento de puntos ganados o gastados |
| `MovimientoInventario.java` | Asento de movimiento de stock |
| `ItemVenta.java` | Línea de un pedido |
| `ComponenteMenu.java` | Ingrediente que forma parte de un menú |
| `Categoria.java` | Categoría a la que pertenece un producto |
| `TipoPago.java` | Efectivo, tarjeta, boleto y demás formas de pago |
| `TipoItem.java` | Indica si una línea de venta es producto o menú |
| `MenuCompleto.java` | Menú con su lista de componentes ya cargada |
| `ProductoIndividual.java` | Producto suelto, sincombination de menú |
| `PedidoResumen.java` | Versión ligera de un pedido junto con su cliente |
| `Reporte.java` | Estructura genérica para mostrar un reporte con su tabla de filas |

### `dao/` (10 archivos) — Acceso a la base de datos

Cada DAO se ocupa de **una sola tabla**. Todas las consultas usan `PreparedStatement`, nunca concatenan texto.

| Archivo | Qué hace |
|---|---|
| `Conexion.java` | Clase auxiliar de conexión, se usa sin instanciarla |
| `ClienteDAO.java` | Alta, consulta, edición y cambio de estado de clientes |
| `ProductoDAO.java` | Alta, consulta y edición de productos |
| `MenuDAO.java` | Alta, consulta y edición de menús |
| `PedidoDAO.java` | Pedidos y sus líneas de detalle |
| `PagoDAO.java` | Registro y consulta de pagos |
| `CanjeDAO.java` | Canjes de recompensas con puntos |
| `InventarioDAO.java` | Control de existencias |
| `MovimientoPuntosDAO.java` | Historial de movimientos de puntos |
| `ReporteDAO.java` | Los 6 reportes |

### `service/` (9 archivos) — Reglas de negocio

Aquí viven las validaciones. Esta capa **decide** y luego llama al DAO.

| Archivo | Qué hace |
|---|---|
| `ClienteService.java` | Alta, edición y cambio entre los tres estados del cliente |
| `CatalogoService.java` | Productos y menús |
| `PedidoService.java` | Crear pedidos, cobra 2 puntos por cada sándwich |
| `PagoService.java` | Procesar cobros y rechazar pagos inválidos |
| `InventarioService.java` | Abastecer y descontar existencias |
| `FidelizacionService.java` | Canjear puntos por recompensas |
| `ReporteService.java` | Genera los reportes |
| `ClienteSnapshot.java` | Copia ligera e inmutable de un cliente |
| `ResultadoCobro.java` | Resultado de un cobro: aprobado o rechazado, con sus datos |

### `view/` (13 archivos) — Interfaz gráfica

| Archivo | Qué hace |
|---|---|
| `VentanaPrincipal.java` | **La única ventana del programa.** Contiene la barra superior con Inicio, la ruta actual y Salir |
| `MenuPrincipalPanel.java` | La pantalla de inicio con los 7 módulos |
| `ClientesPanel.java` | Módulo 01, Administración de clientes. Incluye el botón "Cambiar estado" |
| `ProductosPanel.java` | Módulo 02, Administración de productos y menús |
| `InventarioPanel.java` | Módulo 03, Inventario y abastecimiento |
| `PedidoPanel.java` | Módulo 04, Gestión de pedidos |
| `PagosPanel.java` | Módulo 05, Cobro de pedidos |
| `FidelizacionPanel.java` | Módulo 06, Fidelización y canjes |
| `ReportesPanel.java` | Módulo 07, Reportes |
| `Componentes.java` | Botones, tablas y etiquetas con el mismo estilo visual |
| `Dialogos.java` | Mensajes de error, éxito, aviso y confirmación |
| `Navegador.java` | Interfaz que permite pedir un cambio de módulo |
| `ModuloRefrescable.java` | Permite que un módulo recargue sus datos al volver a él |

### `util/` (10 archivos) — Utilidades compartidas

| Archivo | Qué hace |
|---|---|
| `ConexionOracle.java` | Abre y cierra la conexión con Oracle |
| `Transaccion.java` | Controla `commit` y `rollback` de las operaciones |
| `Vincular.java` | Asocia los valores a los huecos de un `PreparedStatement` |
| `Validaciones.java` | Valida DPI, correo y campos obligatorios |
| `Formato.java` | Presenta moneda, puntos y fechas |
| `Json.java` | Convierte objetos a JSON para la API |
| `ErroresOracle.java` | Traduce los errores de Oracle a mensajes claros |
| `Comprobante.java` | Genera el texto del comprobante de venta o de canje |
| `ExcepcionNegocio.java` | Excepción para reglas de negocio |
| `ExcepcionBaseDatos.java` | Excepción para problemas de conexión |

### `api/` (1 archivo)

| Archivo | Qué hace |
|---|---|
| `ApiPuntos.java` | API REST en el puerto 8080. Es la que consume la aplicación Android |

---

## `src/main/resources/` (1 archivo)

| Archivo | Qué hace |
|---|---|
| `database.properties` | Datos de conexión: servidor, puerto, base de datos, usuario, contraseña y puerto de la API |

---

## `lib/` (2 archivos)

| Archivo | Qué hace |
|---|---|
| `ojdbc11.jar` | Driver oficial de Oracle para Java. Ocupa 7 MB y no se abre |
| `README.md` | Explicación del driver y de por qué se incluye |

---

## `docs/` (4 archivos)

| Archivo | Qué muestra | Abrir con |
|---|---|---|
| `arquitectura.md` | Las 10 capas del proyecto y cómo se relacionan | VS Code |
| `diagrama-clases.puml` | Diagrama de clases en formato PlantUML | NetBeans o PlantUML |
| `modelo-entidad-relacion.puml` | Diagrama de la base de datos | NetBeans o PlantUML |
| `casos-uso.puml` | Diagrama de casos de uso | NetBeans o PlantUML |

---

## Archivos que no debes modificar

| Archivo | Por qué |
|---|---|
| `lib/ojdbc11.jar` | Es el driver de Oracle, no código tuyo |
| `target/` | Código ya compilado, se regenera con `compilar.ps1` |
| `android/` | Proyecto separado, no es parte del backend |

---

## Comandos útiles

| Quiero… | Ejecuto |
|---|---|
| Compilar el proyecto | `.\compilar.ps1` |
| Abrir la aplicación | Doble clic en `INICIAR-APP.bat` |
| Abrir la API | Doble clic en `INICIAR-API.bat` |
| Reinstalar la base de datos | `.\instalar-base-datos.ps1` |
| Abrir solo un módulo SQL | SQL Developer, luego `base de datos > guiones > abrir` y elegir el `.sql` |
