package panpuntos.model;

public enum TipoPago {

    EFECTIVO("Efectivo"),
    TARJETA("Tarjeta");

    private final String etiqueta;

    TipoPago(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
