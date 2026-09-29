package panpuntos.service;

import panpuntos.dao.PedidoDAO;
import panpuntos.dao.ProductoDAO;
import panpuntos.model.EstadoPedido;
import panpuntos.model.ItemVenta;
import panpuntos.model.Pedido;
import panpuntos.model.PedidoResumen;
import panpuntos.model.Producto;
import panpuntos.util.ExcepcionNegocio;
import panpuntos.util.Transaccion;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PedidoService {

    public Pedido crearPedido(ClienteSnapshot cliente, List<ItemVenta> items) {
        return Transaccion.ejecutar(conexion -> {
            Pedido pedido = new Pedido(cliente.aCliente());
            items.forEach(pedido::agregarItem);
            validarExistencias(conexion, pedido);
            new PedidoDAO(conexion).insertar(pedido);
            return pedido;
        });
    }

    public Pedido buscarPedido(int idPedido) {
        return Transaccion.ejecutar(conexion -> new PedidoDAO(conexion).buscarPorId(idPedido));
    }

    public List<PedidoResumen> listar(EstadoPedido estado, String dpi, Timestamp desde, Timestamp hasta) {
        return Transaccion.ejecutar(conexion -> new PedidoDAO(conexion).listar(estado, dpi, desde, hasta));
    }

    public List<PedidoResumen> listarPendientes() {
        return listar(EstadoPedido.PENDIENTE, null, null, null);
    }

    public void anular(int idPedido) {
        Transaccion.ejecutarVoid(conexion -> {
            PedidoDAO dao = new PedidoDAO(conexion);
            Pedido pedido = dao.buscarPorId(idPedido);
            if (!pedido.getEstado().esPendiente()) {
                throw new ExcepcionNegocio("Solo un pedido pendiente puede anularse.");
            }
            dao.cambiarEstado(idPedido, EstadoPedido.ANULADO);
        });
    }

    public void marcarEntregado(int idPedido) {
        Transaccion.ejecutarVoid(conexion -> {
            PedidoDAO dao = new PedidoDAO(conexion);
            Pedido pedido = dao.buscarPorId(idPedido);
            if (pedido.getEstado() != EstadoPedido.PAGADO) {
                throw new ExcepcionNegocio("Solo un pedido pagado puede marcarse como entregado.");
            }
            dao.cambiarEstado(idPedido, EstadoPedido.ENTREGADO);
        });
    }

    public void validarExistencias(Connection conexion, Pedido pedido) {
        Map<Integer, Integer> requeridas = new LinkedHashMap<>();
        for (ItemVenta item : pedido.getItems()) {
            item.unidadesDeInventario().forEach((idProducto, unidades) ->
                    requeridas.merge(idProducto, unidades, Integer::sum));
        }
        ProductoDAO productos = new ProductoDAO(conexion);
        List<String> problemas = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entrada : requeridas.entrySet()) {
            Producto producto = productos.buscarPorId(entrada.getKey());
            if (producto == null) {
                problemas.add("Un componente del pedido no existe en el catalogo.");
            } else if (!producto.getEstado().estaActivo()) {
                problemas.add(producto.getNombre() + " esta desactivado.");
            } else if (producto.getExistencia() < entrada.getValue()) {
                problemas.add(producto.getNombre() + ": requiere " + entrada.getValue()
                        + " y hay " + producto.getExistencia() + " disponibles.");
            }
        }
        if (!problemas.isEmpty()) {
            throw new ExcepcionNegocio("No se puede confirmar el pedido. " + String.join(" ", problemas));
        }
    }
}
