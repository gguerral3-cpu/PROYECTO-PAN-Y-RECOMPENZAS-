package panpuntos.util;

import panpuntos.model.ItemVenta;
import panpuntos.model.Pago;
import panpuntos.model.Pedido;

public final class Comprobante {

    private static final String LINEA = "--------------------------------------------------";

    private Comprobante() {
    }

    public static String deVenta(Pedido pedido) {
        StringBuilder texto = new StringBuilder();
        texto.append(LINEA).append('\n');
        texto.append("        PAN, PUNTOS Y PREMIOS\n");
        texto.append("           Comprobante de venta\n");
        texto.append(LINEA).append('\n');
        texto.append("Pedido      : ").append(pedido.getIdPedido()).append('\n');
        texto.append("Fecha       : ").append(Formato.fechaHora(pedido.getFechaPedido())).append('\n');
        texto.append("Cliente     : ").append(pedido.getCliente().getNombre()).append('\n');
        texto.append("DPI         : ").append(pedido.getCliente().getDpi()).append('\n');
        texto.append(LINEA).append('\n');
        for (ItemVenta item : pedido.getItems()) {
            texto.append(String.format("%-24s %3d x %-8s %10s%n",
                    item.getDescripcion(),
                    item.getCantidad(),
                    Formato.moneda(item.getPrecioUnitario()),
                    Formato.moneda(item.getSubtotal())));
            texto.append(String.format("    %s  Puntos: %+d%n", item.getDetalleComposicion(), item.calcularPuntos()));
        }
        texto.append(LINEA).append('\n');
        texto.append("TOTAL       : ").append(Formato.moneda(pedido.getTotal())).append('\n');
        Pago pago = pedido.getPago();
        if (pago != null) {
            texto.append("Metodo      : ").append(pago.getTipoPago().getEtiqueta()).append('\n');
            if (pago.getEfectivoRecibido() != null) {
                texto.append("Recibido    : ").append(Formato.moneda(pago.getEfectivoRecibido())).append('\n');
                texto.append("Cambio      : ").append(Formato.moneda(pago.getCambio())).append('\n');
            }
            if (pago.getReferencia() != null) {
                texto.append("Referencia  : ").append(pago.getReferencia()).append('\n');
            }
        }
        texto.append("Puntos      : +").append(pedido.getPuntosGenerados()).append('\n');
        texto.append("Saldo actual: ").append(Formato.puntos(pedido.getCliente().getSaldoPuntos())).append('\n');
        texto.append(LINEA).append('\n');
        texto.append("       Gracias por su compra\n");
        texto.append(LINEA);
        return texto.toString();
    }

    public static String deCanje(String nombreCliente, String nombreRecompensa, int puntosUsados, int saldoRestante) {
        return LINEA + "\n"
                + "        PAN, PUNTOS Y PREMIOS\n"
                + "         Comprobante de canje\n"
                + LINEA + "\n"
                + "Cliente     : " + nombreCliente + "\n"
                + "Recompensa  : " + nombreRecompensa + "\n"
                + "Puntos canje: " + puntosUsados + "\n"
                + "Saldo actual: " + saldoRestante + " pts\n"
                + LINEA;
    }
}
