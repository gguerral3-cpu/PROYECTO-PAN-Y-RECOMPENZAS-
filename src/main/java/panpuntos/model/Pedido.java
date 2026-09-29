package panpuntos.model;

import panpuntos.util.ExcepcionNegocio;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Pedido {

    private int idPedido;
    private Cliente cliente;
    private final List<ItemVenta> items = new ArrayList<>();
    private Date fechaPedido;
    private EstadoPedido estado;
    private Pago pago;

    public Pedido(Cliente cliente) {
        this.cliente = cliente;
        this.fechaPedido = new Date();
        this.estado = EstadoPedido.PENDIENTE;
    }

    public void agregarItem(ItemVenta item) {
        for (ItemVenta existente : items) {
            if (existente.getTipoItem().equals(item.getTipoItem())
                    && existente.getIdItem() == item.getIdItem()) {
                existente.setCantidad(existente.getCantidad() + item.getCantidad());
                return;
            }
        }
        items.add(item);
    }

    public void quitarItem(int indice) {
        if (indice < 0 || indice >= items.size()) {
            throw new ExcepcionNegocio("La posicion seleccionada no corresponde a un producto del pedido.");
        }
        items.remove(indice);
    }

    public void limpiarItems() {
        items.clear();
    }

    public void confirmar() {
        if (cliente == null) {
            throw new ExcepcionNegocio("Debe seleccionar un cliente para el pedido.");
        }
        if (!cliente.puedeOperar()) {
            throw new ExcepcionNegocio("El cliente " + cliente.getNombre() + " esta inactivo.");
        }
        if (items.isEmpty()) {
            throw new ExcepcionNegocio("El pedido debe contener al menos un producto.");
        }
        this.estado = EstadoPedido.PENDIENTE;
    }

    public void registrarPago(Pago nuevoPago) {
        if (!estado.esPendiente()) {
            throw new ExcepcionNegocio("Solo un pedido pendiente puede recibir un pago.");
        }
        if (nuevoPago.getMonto() < getTotal()) {
            throw new ExcepcionNegocio("El monto del pago es menor al total del pedido.");
        }
        this.pago = nuevoPago;
        this.estado = EstadoPedido.PAGADO;
    }

    public void marcarEntregado() {
        if (estado != EstadoPedido.PAGADO) {
            throw new ExcepcionNegocio("Solo un pedido pagado puede marcarse como entregado.");
        }
        this.estado = EstadoPedido.ENTREGADO;
    }

    public void anular() {
        if (!estado.esPendiente()) {
            throw new ExcepcionNegocio("Solo un pedido pendiente puede anularse.");
        }
        this.estado = EstadoPedido.ANULADO;
    }

    public double getTotal() {
        return items.stream().mapToDouble(ItemVenta::getSubtotal).sum();
    }

    public int getPuntosGenerados() {
        return items.stream().mapToInt(ItemVenta::calcularPuntos).sum();
    }

    public int getCantidadDeItems() {
        return items.size();
    }

    public boolean estaVacio() {
        return items.isEmpty();
    }

    public List<ItemVenta> getItems() {
        return items;
    }

    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Date getFechaPedido() {
        return fechaPedido;
    }

    public void setFechaPedido(Date fechaPedido) {
        this.fechaPedido = fechaPedido;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public void setEstado(EstadoPedido estado) {
        this.estado = estado;
    }

    public Pago getPago() {
        return pago;
    }

    public void setPago(Pago pago) {
        this.pago = pago;
    }
}
