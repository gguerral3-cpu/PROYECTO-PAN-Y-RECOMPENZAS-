package panpuntos;

import panpuntos.util.ConexionOracle;
import panpuntos.util.ExcepcionBaseDatos;
import panpuntos.view.Dialogos;
import panpuntos.view.VentanaPrincipal;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.sql.Connection;

public class Main {

    public static void main(String[] args) {
        aplicarEstilo();
        SwingUtilities.invokeLater(() -> {
            if (!verificarConexion()) {
                System.exit(1);
            }
            new VentanaPrincipal().setVisible(true);
        });
    }

    private static void aplicarEstilo() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.put("Table.showGrid", Boolean.FALSE);
            UIManager.put("TextField.font", new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
            UIManager.put("OptionPane.messageFont", new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
        } catch (Exception e) {
            System.setProperty("awt.useSystemAAFontSettings", "on");
        }
    }

    private static boolean verificarConexion() {
        Connection conexion = null;
        try {
            conexion = ConexionOracle.conectar();
            return conexion.isValid(3);
        } catch (ExcepcionBaseDatos | java.sql.SQLException e) {
            JOptionPane.showMessageDialog(null,
                    e.getMessage() == null ? e.toString() : e.getMessage(),
                    "Sin conexion a la base de datos", JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            ConexionOracle.cerrar(conexion);
        }
    }
}
