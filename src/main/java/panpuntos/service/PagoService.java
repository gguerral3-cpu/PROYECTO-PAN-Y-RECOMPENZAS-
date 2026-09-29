package panpuntos.service;

import panpuntos.dao.PagoDAO;
import panpuntos.dao.PedidoDAO;
import panpuntos.model.EstadoPedido;
import panpuntos.model.Pago;
import panpuntos.model.Pedido;
import panpuntos.model.TipoPago;
import panpuntos.util.ExcepcionNegocio;
import panpuntos.util.Transaccion;

public class PagoService {

    private final PedidoService pedidoService = new PedidoService();

    public ResultadoCobro cobrar(int idPedido, TipoPago tipoPago, Double efectivoRecibido,
                                String referencia, boolean operacionAprobada) {
        return Transaccion.ejecutar(conexion -> {
            PedidoDAO pedidoDAO = new PedidoDAO(conexion);
            Pedido pedido = pedidoDAO.buscarPorId(idPedido);
            if (!pedido.getEstado().esPendiente()) {
                throw new ExcepcionNegocio("El pedido " + idPedido + " ya fue procesado y no admite nuevos pagos.");
            }
            double total = pedido.getTotal();

            if (tipoPago == TipoPago.TARJETA && !operacionAprobada) {
                Pago intento = Pago.rechazadoTarjeta(total, referencia);
                new PagoDAO(conexion).registrar(intento, idPedido);
                return ResultadoCobro.rechazado(intento, pedido);
            }

            Pago pago = tipoPago == TipoPago.EFECTIVO
                    ? Pago.enEfectivo(total, efectivoRecibido == null ? 0 : efectivoRecibido)
                    : Pago.conTarjeta(total, referencia);

            pedidoService.validarExistencias(conexion, pedido);

            PagoDAO pagos = new PagoDAO(conexion);
            pagos.registrar(pago, idPedido);
            pedidoDAO.cambiarEstado(idPedido, EstadoPedido.PAGADO);
            pedido.registrarPago(pago);
            return ResultadoCobro.exitoso(pago, pedido);
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

    public void anularPedidoPendiente(int idPedido) {
        Transaccion.ejecutarVoid(conexion -> {
            PedidoDAO dao = new PedidoDAO(conexion);
            Pedido pedido = dao.buscarPorId(idPedido);
            if (!pedido.getEstado().esPendiente()) {
                throw new ExcepcionNegocio("Solo un pedido pendiente puede anularse.");
            }
            dao.cambiarEstado(idPedido, EstadoPedido.ANULADO);
        });
    }

    public Pedido buscarPedido(int idPedido) {
        return Transaccion.ejecutar(conexion -> new PedidoDAO(conexion).buscarPorId(idPedido));
    }
}
