package panpuntos.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Validaciones {

    private Validaciones() {
    }

    public static String textoRequerido(String valor, String campo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new ExcepcionNegocio("El campo " + campo + " es obligatorio.");
        }
        return valor.trim();
    }

    public static String textoOpcional(String valor) {
        return valor == null || valor.trim().isEmpty() ? null : valor.trim();
    }

    public static int enteroRequerido(String valor, String campo) {
        textoRequerido(valor, campo);
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            throw new ExcepcionNegocio("El campo " + campo + " debe ser un numero entero.");
        }
    }

    public static int enteroPositivo(String valor, String campo) {
        int numero = enteroRequerido(valor, campo);
        if (numero <= 0) {
            throw new ExcepcionNegocio("El campo " + campo + " debe ser mayor que cero.");
        }
        return numero;
    }

    public static double decimalNoNegativo(String valor, String campo) {
        textoRequerido(valor, campo);
        double numero;
        try {
            numero = Double.parseDouble(valor.trim().replace(",", "."));
        } catch (NumberFormatException e) {
            throw new ExcepcionNegocio("El campo " + campo + " debe ser un valor numerico.");
        }
        if (numero < 0) {
            throw new ExcepcionNegocio("El campo " + campo + " no puede ser negativo.");
        }
        return redondear(numero);
    }

    public static double decimalPositivo(String valor, String campo) {
        double numero = decimalNoNegativo(valor, campo);
        if (numero <= 0) {
            throw new ExcepcionNegocio("El campo " + campo + " debe ser mayor que cero.");
        }
        return numero;
    }

    public static String dpiRequerido(String valor) {
        String dpi = textoRequerido(valor, "DPI");
        if (dpi.length() < 8 || dpi.length() > 20) {
            throw new ExcepcionNegocio("El DPI debe tener entre 8 y 20 caracteres.");
        }
        return dpi;
    }

    public static String correoValido(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }
        String correo = valor.trim();
        if (!correo.matches("^[\\w.+-]+@[\\w-]+\\.[\\w.-]+$")) {
            throw new ExcepcionNegocio("El correo electronico no tiene un formato valido.");
        }
        return correo;
    }

    public static double redondear(double valor) {
        return BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
