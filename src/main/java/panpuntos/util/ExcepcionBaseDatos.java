package panpuntos.util;

import java.sql.SQLException;

public class ExcepcionBaseDatos extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final int codigo;

    public ExcepcionBaseDatos(String mensaje, Throwable causa) {
        super(mensaje, causa);
        this.codigo = extraerCodigo(causa);
    }

    public int getCodigo() {
        return codigo;
    }

    public String getMensajeOracle() {
        return getMessage();
    }

    private static int extraerCodigo(Throwable causa) {
        if (causa instanceof SQLException sql) {
            return sql.getErrorCode();
        }
        return 0;
    }
}
