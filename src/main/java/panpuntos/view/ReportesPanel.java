package panpuntos.view;

import panpuntos.model.Reporte;
import panpuntos.service.ReporteService;
import panpuntos.util.Formato;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Date;

public class ReportesPanel extends JPanel {

    private final ReporteService servicio = new ReporteService();

    private final JComboBox<String> cmbReporte = new JComboBox<>();
    private final JTextField txtDesde = new JTextField(10);
    private final JTextField txtHasta = new JTextField(10);
    private final JLabel lblResumen = Componentes.etiqueta(" ");
    private final JScrollPane panelTabla = new JScrollPane();
    private JTable tabla = Componentes.tabla(new String[]{"Sin resultados"});

    public ReportesPanel() {
        setLayout(new BorderLayout());
        construir();
        panelTabla.setViewportView(tabla);
        panelTabla.setBorder(BorderFactory.createLineBorder(Componentes.GRIS_BORDE));
        generar();
    }

    private void construir() {
        for (int i = 1; i <= ReporteService.cantidadDeReportes(); i++) {
            cmbReporte.addItem(i + ". " + ReporteService.nombreDe(i));
        }
        cmbReporte.setPreferredSize(new Dimension(330, 28));
        txtDesde.setText(Formato.fecha(hace(365)));
        txtHasta.setText(Formato.fecha(new Date()));

        JButton btnGenerar = Componentes.boton("Generar reporte", Componentes.VERDE);
        JButton btnExportar = Componentes.boton("Exportar a CSV", Componentes.AZUL_CLARO);
        btnGenerar.addActionListener(e -> generar());
        btnExportar.addActionListener(e -> exportar());

        JPanel controles = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        controles.add(Componentes.etiqueta("Reporte:"));
        controles.add(cmbReporte);
        controles.add(Componentes.etiqueta("Desde:"));
        controles.add(txtDesde);
        controles.add(Componentes.etiqueta("Hasta:"));
        controles.add(txtHasta);
        controles.add(btnGenerar);
        controles.add(btnExportar);
        controles.setBorder(BorderFactory.createTitledBorder("Seleccion del reporte (fechas en dd/MM/yyyy)"));

        JPanel norte = new JPanel(new BorderLayout(0, 6));
        norte.add(controles, BorderLayout.CENTER);
        norte.add(lblResumen, BorderLayout.SOUTH);
        norte.setBorder(new EmptyBorder(10, 10, 6, 10));

        JPanel centro = new JPanel(new BorderLayout());
        centro.add(panelTabla, BorderLayout.CENTER);
        centro.setBorder(new EmptyBorder(0, 10, 10, 10));

        JPanel contenido = new JPanel(new BorderLayout());
        contenido.add(norte, BorderLayout.NORTH);
        contenido.add(centro, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(Componentes.titulo("Reportes de ventas, inventario y puntos"), BorderLayout.NORTH);
        add(contenido, BorderLayout.CENTER);
    }

    private void generar() {
        try {
            int tipo = cmbReporte.getSelectedIndex() + 1;
            Date desde = Formato.aFecha(txtDesde.getText());
            Date hasta = Formato.aFecha(txtHasta.getText());
            Reporte reporte = servicio.generar(tipo, desde, hasta);
            tabla = Componentes.tabla(reporte);
            panelTabla.setViewportView(tabla);
            lblResumen.setText("Reporte: " + reporte.getTitulo()
                    + "   |   Registros encontrados: " + reporte.getCantidadFilas());
        } catch (RuntimeException e) {
            Dialogos.error(this, e);
        }
    }

    private void exportar() {
        DefaultTableModel modelo = (DefaultTableModel) tabla.getModel();
        if (modelo.getRowCount() == 0) {
            Dialogos.aviso(this, "No hay datos para exportar.");
            return;
        }
        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Guardar reporte");
        selector.setFileFilter(new FileNameExtensionFilter("Archivo CSV (*.csv)", "csv"));
        selector.setSelectedFile(new File(reporteActual() + ".csv"));
        if (selector.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File archivo = selector.getSelectedFile();
        if (!archivo.getName().toLowerCase().endsWith(".csv")) {
            archivo = new File(archivo.getParentFile(), archivo.getName() + ".csv");
        }
        try {
            StringBuilder csv = new StringBuilder();
            for (int columna = 0; columna < modelo.getColumnCount(); columna++) {
                csv.append(separar(modelo.getColumnName(columna)))
                        .append(columna < modelo.getColumnCount() - 1 ? ";" : "\n");
            }
            for (int fila = 0; fila < modelo.getRowCount(); fila++) {
                for (int columna = 0; columna < modelo.getColumnCount(); columna++) {
                    csv.append(separar(String.valueOf(modelo.getValueAt(fila, columna))))
                            .append(columna < modelo.getColumnCount() - 1 ? ";" : "\n");
                }
            }
            Files.writeString(archivo.toPath(), csv.toString(), StandardCharsets.UTF_8);
            Dialogos.exito(this, "Reporte exportado en " + archivo.getName());
        } catch (Exception e) {
            Dialogos.error(this, e);
        }
    }

    private String separar(String valor) {
        return valor.contains(";") ? "\"" + valor + "\"" : valor;
    }

    private String reporteActual() {
        return cmbReporte.getItemAt(cmbReporte.getSelectedIndex())
                .replaceAll("[^A-Za-z0-9]+", "_");
    }

    private Date hace(int dias) {
        return new Date(System.currentTimeMillis() - dias * 24L * 60L * 60L * 1000L);
    }
}
