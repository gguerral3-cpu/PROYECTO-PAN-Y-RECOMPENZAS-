package panpuntos.model;

public enum EstadoPago {

    APROBADO("Aprobado"),
    RECHAZADO("Rechazado");

    private final String etiqueta;

    EstadoPago(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
