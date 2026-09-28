package panpuntos.util;

public class ExcepcionNegocio extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ExcepcionNegocio(String mensaje) {
        super(mensaje);
    }
}
