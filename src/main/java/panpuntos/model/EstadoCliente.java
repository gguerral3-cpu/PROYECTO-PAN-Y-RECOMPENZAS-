package panpuntos.model;

public enum EstadoCliente {

    ACTIVO("Activo"),
    INACTIVO("Inactivo"),
    SUSPENDIDO("Suspendido");

    private final String etiqueta;

    EstadoCliente(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public boolean estaActivo() {
        return this == ACTIVO;
    }

    public boolean estaSuspendido() {
        return this == SUSPENDIDO;
    }

    public boolean puedeOperar() {
        return this == ACTIVO;
    }

    public static EstadoCliente desdeTexto(String texto) {
        if (texto == null) {
            return ACTIVO;
        }
        for (EstadoCliente estado : values()) {
            if (estado.name().equalsIgnoreCase(texto.trim())) {
                return estado;
            }
        }
        return ACTIVO;
    }
}
