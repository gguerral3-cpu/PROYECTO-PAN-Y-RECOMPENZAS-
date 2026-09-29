package panpuntos.model;

public enum Categoria {

    SANDWICH("Sandwich", 2),
    BEBIDA("Bebida", 0),
    ACOMPANAMIENTO("Acompanamiento", 0);

    private final String etiqueta;
    private final int puntosPorUnidad;

    Categoria(String etiqueta, int puntosPorUnidad) {
        this.etiqueta = etiqueta;
        this.puntosPorUnidad = puntosPorUnidad;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public int getPuntosPorUnidad() {
        return puntosPorUnidad;
    }

    public static Categoria desdeTexto(String texto) {
        for (Categoria categoria : values()) {
            if (categoria.name().equalsIgnoreCase(texto) || categoria.etiqueta.equalsIgnoreCase(texto)) {
                return categoria;
            }
        }
        throw new IllegalArgumentException("Categoria desconocida: " + texto);
    }
}
