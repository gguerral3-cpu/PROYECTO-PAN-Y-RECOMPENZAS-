package panpuntos.util;

import java.sql.SQLException;

public final class ErroresOracle {

    private ErroresOracle() {
    }

    public static RuntimeException traducir(SQLException causa) {
        int codigo = causa.getErrorCode();
        if (codigo >= 20000 && codigo <= 20999) {
            return new ExcepcionNegocio(limpiar(causa.getMessage()));
        }
        if (codigo == 1) {
            return new ExcepcionNegocio("El registro ya existe: no se permiten identificadores, "
                    + "codigos ni correos repetidos.");
        }
        if (codigo == 2291 || codigo == 2292) {
            return new ExcepcionNegocio("No se puede completar la operacion: hay registros relacionados "
                    + "que impiden realizarla.");
        }
        if (codigo == 1400) {
            return new ExcepcionNegocio("Falta un dato obligatorio en el formulario.");
        }
        return new ExcepcionBaseDatos("Error de base de datos: " + causa.getMessage(), causa);
    }

    private static String limpiar(String mensaje) {
        if (mensaje == null) {
            return "Ocurrio un error inesperado.";
        }
        return mensaje.replaceAll("ORA-\\d{5}: ", "").trim();
    }
}
