package panpuntos.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public final class Formato {

    private static final String FECHA_CORTA = "dd/MM/yyyy";
    private static final String FECHA_HORA = "dd/MM/yyyy HH:mm";

    private Formato() {
    }

    public static String moneda(double valor) {
        return String.format("Q%.2f", valor);
    }

    public static String puntos(int valor) {
        return valor + " pts";
    }

    public static String puntosConSigno(int valor) {
        return valor > 0 ? "+" + valor : String.valueOf(valor);
    }

    public static String fecha(Date valor) {
        return valor == null ? "" : new SimpleDateFormat(FECHA_CORTA).format(valor);
    }

    public static String fechaHora(Date valor) {
        return valor == null ? "" : new SimpleDateFormat(FECHA_HORA).format(valor);
    }

    public static Date aFecha(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        try {
            return new SimpleDateFormat(FECHA_CORTA).parse(texto.trim());
        } catch (java.text.ParseException e) {
            throw new ExcepcionNegocio("La fecha debe tener el formato dd/MM/yyyy.");
        }
    }
}
