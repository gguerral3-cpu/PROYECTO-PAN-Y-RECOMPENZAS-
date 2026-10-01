package panpuntos.view;

import panpuntos.util.ExcepcionBaseDatos;
import panpuntos.util.ExcepcionNegocio;

import javax.swing.JOptionPane;
import java.awt.Component;

public final class Dialogos {

    private Dialogos() {
    }

    public static void error(Component padre, Exception e) {
        JOptionPane.showMessageDialog(padre, mensaje(e), "No se pudo completar la operacion", JOptionPane.ERROR_MESSAGE);
    }

    public static void error(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "No se pudo completar la operacion", JOptionPane.ERROR_MESSAGE);
    }

    public static void info(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Proceso generado", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void exito(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Operacion realizada", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void aviso(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Advertencia", JOptionPane.WARNING_MESSAGE);
    }

    public static boolean confirmar(Component padre, String mensaje) {
        return JOptionPane.showConfirmDialog(padre, mensaje, "Confirmacion",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    public static void mostrarTexto(Component padre, String titulo, String texto) {
        JOptionPane.showMessageDialog(padre,
                "<html><pre>" + texto.replace("&", "&amp;").replace("<", "&lt;") + "</pre></html>",
                titulo, JOptionPane.PLAIN_MESSAGE);
    }

    public static String pedirTexto(Component padre, String titulo, String mensaje, String valorInicial) {
        return (String) JOptionPane.showInputDialog(padre, mensaje, titulo,
                JOptionPane.QUESTION_MESSAGE, null, null, valorInicial);
    }

    private static String mensaje(Exception e) {
        if (e instanceof ExcepcionNegocio || e instanceof ExcepcionBaseDatos) {
            return e.getMessage();
        }
        if (e.getMessage() == null || e.getMessage().isBlank()) {
            return e.getClass().getSimpleName();
        }
        return e.getMessage();
    }
}
