package panpuntos.view;

import panpuntos.model.Reporte;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

public final class Componentes {

    public static final Color AZUL = new Color(0x1F4E79);
    public static final Color AZUL_CLARO = new Color(0x2E75B6);
    public static final Color VERDE = new Color(0x2E7D32);
    public static final Color ROJO = new Color(0xC62828);
    public static final Color GRIS_FONDO = new Color(0xF4F6F8);
    public static final Color GRIS_BORDE = new Color(0xD0D5DA);

    private Componentes() {
    }

    public static JButton boton(String texto) {
        return boton(texto, AZUL);
    }

    public static JButton boton(String texto, Color color) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        boton.setBackground(color);
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.setBorder(new EmptyBorder(8, 14, 8, 14));
        return boton;
    }

    public static JButton botonSecundario(String texto) {
        JButton boton = boton(texto, new Color(0x5A6673));
        return boton;
    }

    public static JButton botonPeligro(String texto) {
        return boton(texto, ROJO);
    }

    public static JLabel etiqueta(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return etiqueta;
    }

    public static JLabel titulo(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(new Font("Segoe UI", Font.BOLD, 18));
        etiqueta.setForeground(AZUL);
        etiqueta.setBorder(new EmptyBorder(10, 14, 10, 14));
        return etiqueta;
    }

    public static JLabel valor(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(new Font("Segoe UI", Font.BOLD, 14));
        etiqueta.setForeground(AZUL);
        return etiqueta;
    }

    public static JPanel panelBotones(JButton... botones) {
        JPanel panel = new JPanel(new GridLayout(1, botones.length, 8, 0));
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));
        for (JButton boton : botones) {
            panel.add(boton);
        }
        return panel;
    }

    public static JPanel panelCampos(int filas) {
        JPanel panel = new JPanel(new java.awt.GridLayout(filas, 2, 8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        return panel;
    }

    public static JTable tabla(Reporte reporte) {
        DefaultTableModel modelo = new DefaultTableModel(reporte.getColumnas(), 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        List<Object[]> filas = reporte.getFilas();
        for (Object[] fila : filas) {
            modelo.addRow(fila);
        }
        JTable tabla = new JTable(modelo);
        configurarTabla(tabla);
        return tabla;
    }

    public static JTable tabla(String[] columnas) {
        JTable tabla = new JTable(new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        });
        configurarTabla(tabla);
        return tabla;
    }

    public static void configurarTabla(JTable tabla) {
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabla.setRowHeight(24);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setGridColor(GRIS_BORDE);
        tabla.setShowVerticalLines(false);
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabla.getTableHeader().setBackground(new Color(0xE8EDF2));
        tabla.getTableHeader().setReorderingAllowed(false);
    }

    public static JScrollPane panelDesplazable(JTable tabla) {
        JScrollPane panel = new JScrollPane(tabla);
        panel.setBorder(BorderFactory.createLineBorder(GRIS_BORDE));
        return panel;
    }
}
