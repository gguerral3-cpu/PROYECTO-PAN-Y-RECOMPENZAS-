package panpuntos.model;

public enum EstadoPedido {

    PENDIENTE("Pendiente"),
    PAGADO("Pagado"),
    ENTREGADO("Entregado"),
    ANULADO("Anulado");

    private final String etiqueta;

    EstadoPedido(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public boolean esPendiente() {
        return this == PENDIENTE;
    }

    public boolean esVendido() {
        return this == PAGADO || this == ENTREGADO;
    }
}
