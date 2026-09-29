package panpuntos.dao;

import panpuntos.model.Cliente;
import panpuntos.model.EstadoPedido;
import panpuntos.model.EstadoPago;
import panpuntos.model.ItemVenta;
import panpuntos.model.Pago;
import panpuntos.model.Pedido;
import panpuntos.model.PedidoResumen;
import panpuntos.model.TipoPago;
import panpuntos.util.ErroresOracle;
import panpuntos.util.ExcepcionNegocio;
import panpuntos.util.Vincular;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class PedidoDAO {

    private static final String SQL_INSERTAR = """
            INSERT INTO PEDIDO (ID_PEDIDO, ID_CLIENTE, FECHA_PEDIDO, SUBTOTAL, TOTAL, PUNTOS_GENERADOS, ESTADO)
            VALUES (SEQ_PEDIDO.NEXTVAL, ?, SYSDATE, ?, ?, ?, 'PENDIENTE')
            """;

    private static final String SQL_INSERTAR_DETALLE = """
            INSERT INTO DETALLE_PEDIDO
                (ID_DETALLE, ID_PEDIDO, TIPO_ITEM, ID_PRODUCTO, ID_MENU, CANTIDAD, PRECIO_UNITARIO, SUBTOTAL, PUNTOS)
            VALUES (SEQ_DETALLE_PEDIDO.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String SQL_ACTUALIZAR_ESTADO = "UPDATE PEDIDO SET ESTADO = ? WHERE ID_PEDIDO = ?";

    private static final String SQL_PEDIDO = """
            SELECT PE.ID_PEDIDO, PE.ID_CLIENTE, PE.FECHA_PEDIDO, PE.SUBTOTAL, PE.TOTAL,
                   PE.PUNTOS_GENERADOS, PE.ESTADO,
                   PA.ID_PAGO, PA.TIPO_PAGO, PA.MONTO, PA.EFECTIVO_RECIBIDO, PA.CAMBIO,
                   PA.REFERENCIA, PA.ESTADO AS ESTADO_PAGO
            FROM PEDIDO PE LEFT JOIN PAGO PA ON PA.ID_PEDIDO = PE.ID_PEDIDO
            WHERE PE.ID_PEDIDO = ?
            """;

    private static final String SQL_DETALLES = """
            SELECT TIPO_ITEM, ID_PRODUCTO, ID_MENU, CANTIDAD, PRECIO_UNITARIO, SUBTOTAL, PUNTOS
            FROM DETALLE_PEDIDO WHERE ID_PEDIDO = ? ORDER BY ID_DETALLE
            """;

    private static final String SQL_LISTAR = """
            SELECT PE.ID_PEDIDO, PE.FECHA_PEDIDO, C.DPI, C.NOMBRE, PE.TOTAL, PE.PUNTOS_GENERADOS,
                   PE.ESTADO, PA.TIPO_PAGO
            FROM PEDIDO PE
            JOIN CLIENTE C ON C.ID_CLIENTE = PE.ID_CLIENTE
            LEFT JOIN PAGO PA ON PA.ID_PEDIDO = PE.ID_PEDIDO
            WHERE (? IS NULL OR PE.ESTADO = ?)
              AND (? IS NULL OR UPPER(C.DPI) = UPPER(?))
              AND (? IS NULL OR PE.FECHA_PEDIDO >= ?)
              AND (? IS NULL OR PE.FECHA_PEDIDO < ?)
            ORDER BY PE.ID_PEDIDO DESC
            """;

    private final Connection conexion;
    private final ProductoDAO productoDAO;
    private final MenuDAO menuDAO;

    public PedidoDAO(Connection conexion) {
        this.conexion = conexion;
        this.productoDAO = new ProductoDAO(conexion);
        this.menuDAO = new MenuDAO(conexion);
    }

    public int insertar(Pedido pedido) {
        pedido.confirmar();
        try (PreparedStatement statement = conexion.prepareStatement(SQL_INSERTAR, new String[]{"ID_PEDIDO"})) {
            double total = pedido.getTotal();
            Vincular.registrar(statement, pedido.getCliente().getIdCliente(), total, total,
                    pedido.getPuntosGenerados());
            statement.executeUpdate();
            int idPedido;
            try (ResultSet claves = statement.getGeneratedKeys()) {
                claves.next();
                idPedido = claves.getInt(1);
            }
            pedido.setIdPedido(idPedido);
            guardarDetalles(pedido);
            return idPedido;
        } catch (SQLException e) {
            throw ErroresOracle.traducir(e);
        }
    }

    public void cambiarEstado(int idPedido, EstadoPedido estado) {
        Conexion.ejecutar(conexion, SQL_ACTUALIZAR_ESTADO, estado.name(), idPedido);
    }

    public Pedido buscarPorId(int idPedido) {
        ClienteDAO clienteDAO = new ClienteDAO(conexion);
        try (PreparedStatement statement = conexion.prepareStatement(SQL_PEDIDO)) {
            Vincular.registrar(statement, idPedido);
            try (ResultSet resultado = statement.executeQuery()) {
                if (!resultado.next()) {
                    throw new ExcepcionNegocio("El pedido " + idPedido + " no existe.");
                }
                Cliente cliente = clienteDAO.buscarPorId(resultado.getInt("ID_CLIENTE"));
                Pedido pedido = new Pedido(cliente);
                pedido.setIdPedido(idPedido);
                Timestamp fecha = resultado.getTimestamp("FECHA_PEDIDO");
                pedido.setFechaPedido(fecha == null ? new java.util.Date() : new java.util.Date(fecha.getTime()));
                pedido.setEstado(EstadoPedido.valueOf(resultado.getString("ESTADO")));
                pedido.setPago(mapearPago(resultado, idPedido));
                cargarItems(pedido);
                return pedido;
            }
        } catch (SQLException e) {
            throw ErroresOracle.traducir(e);
        }
    }

    public List<PedidoResumen> listar(EstadoPedido estado, String dpi, Timestamp desde, Timestamp hasta) {
        List<PedidoResumen> pedidos = new ArrayList<>();
        try (PreparedStatement statement = conexion.prepareStatement(SQL_LISTAR)) {
            Vincular.registrar(statement,
                    estado == null ? null : estado.name(), estado == null ? null : estado.name(),
                    dpi, dpi, desde, desde, hasta, hasta);
            try (ResultSet resultado = statement.executeQuery()) {
                while (resultado.next()) {
                    Timestamp fecha = resultado.getTimestamp("FECHA_PEDIDO");
                    String tipoPago = resultado.getString("TIPO_PAGO");
                    pedidos.add(new PedidoResumen(
                            resultado.getInt("ID_PEDIDO"),
                            fecha == null ? null : new java.util.Date(fecha.getTime()),
                            resultado.getString("DPI"),
                            resultado.getString("NOMBRE"),
                            resultado.getDouble("TOTAL"),
                            resultado.getInt("PUNTOS_GENERADOS"),
                            EstadoPedido.valueOf(resultado.getString("ESTADO")),
                            tipoPago == null ? null : TipoPago.valueOf(tipoPago)));
                }
            }
            return pedidos;
        } catch (SQLException e) {
            throw ErroresOracle.traducir(e);
        }
    }

    private void guardarDetalles(Pedido pedido) {
        try (PreparedStatement statement = conexion.prepareStatement(SQL_INSERTAR_DETALLE)) {
            for (ItemVenta item : pedido.getItems()) {
                Vincular.registrar(statement,
                        pedido.getIdPedido(),
                        item.getTipoItem(),
                        item.getIdProducto(),
                        item.getIdMenu(),
                        item.getCantidad(),
                        item.getPrecioUnitario(),
                        item.getSubtotal(),
                        item.calcularPuntos());
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            throw ErroresOracle.traducir(e);
        }
    }

    private void cargarItems(Pedido pedido) {
        try (PreparedStatement statement = conexion.prepareStatement(SQL_DETALLES)) {
            Vincular.registrar(statement, pedido.getIdPedido());
            try (ResultSet resultado = statement.executeQuery()) {
                while (resultado.next()) {
                    int cantidad = resultado.getInt("CANTIDAD");
                    double precio = resultado.getDouble("PRECIO_UNITARIO");
                    if ("MENU".equals(resultado.getString("TIPO_ITEM"))) {
                        pedido.getItems().add(menuDAO.buscarPorId(resultado.getInt("ID_MENU"))
                                .reconstruirItem(cantidad, precio));
                    } else {
                        pedido.getItems().add(productoDAO.buscarPorId(resultado.getInt("ID_PRODUCTO"))
                                .reconstruirItem(cantidad, precio));
                    }
                }
            }
        } catch (SQLException e) {
            throw ErroresOracle.traducir(e);
        }
    }

    private Pago mapearPago(ResultSet resultado, int idPedido) throws SQLException {
        if (resultado.getString("TIPO_PAGO") == null) {
            return null;
        }
        TipoPago tipo = TipoPago.valueOf(resultado.getString("TIPO_PAGO"));
        double monto = resultado.getDouble("MONTO");
        Pago pago = tipo == TipoPago.EFECTIVO
                ? Pago.enEfectivo(monto, resultado.getDouble("EFECTIVO_RECIBIDO"))
                : Pago.conTarjeta(monto, resultado.getString("REFERENCIA"));
        pago.setIdPago(resultado.getInt("ID_PAGO"));
        pago.setIdPedido(idPedido);
        pago.setEstado(EstadoPago.valueOf(resultado.getString("ESTADO_PAGO")));
        return pago;
    }
}
