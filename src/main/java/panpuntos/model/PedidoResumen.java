package panpuntos.model;

import java.util.Date;

public class PedidoResumen {

    private int idPedido;
    private Date fechaPedido;
    private String dpi;
    private String nombreCliente;
    private double total;
    private int puntosGenerados;
    private EstadoPedido estado;
    private TipoPago tipoPago;

    public PedidoResumen(int idPedido, Date fechaPedido, String dpi, String nombreCliente,
                         double total, int puntosGenerados, EstadoPedido estado, TipoPago tipoPago) {
        this.idPedido = idPedido;
        this.fechaPedido = fechaPedido;
        this.dpi = dpi;
        this.nombreCliente = nombreCliente;
        this.total = total;
        this.puntosGenerados = puntosGenerados;
        this.estado = estado;
        this.tipoPago = tipoPago;
    }

    public int getIdPedido() {
        return idPedido;
    }

    public Date getFechaPedido() {
        return fechaPedido;
    }

    public String getDpi() {
        return dpi;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public double getTotal() {
        return total;
    }

    public int getPuntosGenerados() {
        return puntosGenerados;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public TipoPago getTipoPago() {
        return tipoPago;
    }
}
