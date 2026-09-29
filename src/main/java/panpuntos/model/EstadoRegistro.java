package panpuntos.model;

public enum EstadoRegistro {

    ACTIVO("Activo"),
    INACTIVO("Inactivo");

    private final String etiqueta;

    EstadoRegistro(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public boolean estaActivo() {
        return this == ACTIVO;
    }
}
