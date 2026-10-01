package panpuntos.view;

import panpuntos.model.MovimientoInventario;
import panpuntos.model.Producto;
import panpuntos.service.InventarioService;
import panpuntos.util.ExcepcionNegocio;
import panpuntos.util.Formato;
import panpuntos.util.Validaciones;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

public class InventarioPanel extends JPanel {

    private static final String[] COLUMNAS_MOVIMIENTOS = {
            "Fecha", "Codigo", "Producto", "Movimiento", "Cantidad", "Existencia anterior",
            "Existencia nueva", "Referencia"};

    private final InventarioService servicio = new InventarioService();

    private final JComboBox<String> cmbProducto = new JComboBox<>();
    private final JLabel lblExistenciaActual = Componentes.valor("-");
    private final JTextField txtCantidad = new JTextField(8);
    private final JTextField txtReferencia = new JTextField(14);
    private final JTable tablaMovimientos = Componentes.tabla(COLUMNAS_MOVIMIENTOS);
    private final JTable tablaStock = Componentes.tabla(
            new String[]{"Codigo", "Producto", "Categoria", "Existencia"});
    private final JTable tablaCriticos = Componentes.tabla(
            new String[]{"Codigo", "Producto", "Categoria", "Existencia"});

    private final List<Object[]> productos = new ArrayList<>();

    public InventarioPanel() {
        setLayout(new BorderLayout());
        construir();
        recargar();
    }

    private void construir() {
        JPanel panelAbastecer = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        cmbProducto.setPreferredSize(new Dimension(330, 28));
        panelAbastecer.add(Componentes.etiqueta("Producto:"));
        panelAbastecer.add(cmbProducto);
        panelAbastecer.add(Componentes.etiqueta("Existencia actual:"));
        panelAbastecer.add(lblExistenciaActual);
        panelAbastecer.add(Componentes.etiqueta("Cantidad a abastecer:"));
        panelAbastecer.add(txtCantidad);
        panelAbastecer.add(Componentes.etiqueta("Referencia:"));
        panelAbastecer.add(txtReferencia);
        JButton btnAbastecer = Componentes.boton("Abastecer", Componentes.VERDE);
        btnAbastecer.addActionListener(e -> abastecer());
        panelAbastecer.add(btnAbastecer);
        panelAbastecer.setBorder(BorderFactory.createTitledBorder("1. Abastecimiento de inventario"));

        tablaStock.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaStock.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionarProductoDesdeTabla();
            }
        });

        JPanel panelStock = new JPanel(new BorderLayout(0, 6));
        panelStock.add(Componentes.titulo("Existencias actuales"), BorderLayout.NORTH);
        panelStock.add(Componentes.panelDesplazable(tablaStock), BorderLayout.CENTER);

        JPanel panelCriticos = new JPanel(new BorderLayout());
        panelCriticos.add(Componentes.titulo("Agotados o con bajo inventario (<= 5)"), BorderLayout.NORTH);
        panelCriticos.add(Componentes.panelDesplazable(tablaCriticos), BorderLayout.CENTER);

        JPanel centro = new JPanel(new java.awt.GridLayout(1, 2, 10, 0));
        centro.add(panelStock);
        centro.add(panelCriticos);

        JPanel sur = new JPanel(new BorderLayout());
        sur.add(Componentes.titulo("2. Historial de movimientos de inventario"), BorderLayout.NORTH);
        sur.add(Componentes.panelDesplazable(tablaMovimientos), BorderLayout.CENTER);

        JPanel contenido = new JPanel(new BorderLayout(0, 8));
        contenido.add(panelAbastecer, BorderLayout.NORTH);
        JPanel centroOeste = new JPanel(new BorderLayout());
        centroOeste.add(centro, BorderLayout.CENTER);
        centroOeste.add(sur, BorderLayout.SOUTH);
        contenido.add(centroOeste, BorderLayout.CENTER);
        contenido.setBorder(new EmptyBorder(10, 10, 10, 10));

        setLayout(new BorderLayout());
        add(Componentes.titulo("Control de inventario"), BorderLayout.NORTH);
        add(contenido, BorderLayout.CENTER);

        cmbProducto.addActionListener(e -> mostrarExistencia());
    }

    private void abastecer() {
        Object[] entrada = productoSeleccionado();
        if (entrada == null) {
            Dialogos.aviso(this, "Seleccione un producto del catalogo.");
            return;
        }
        try {
            int cantidad = Validaciones.enteroPositivo(txtCantidad.getText(), "cantidad");
            Producto producto = servicio.listarProductos(null, null).stream()
                    .filter(p -> p.getIdProducto() == (Integer) entrada[1])
                    .findFirst()
                    .orElseThrow(() -> new ExcepcionNegocio("El producto ya no existe."));
            String referencia = Validaciones.textoOpcional(txtReferencia.getText());
            servicio.abastecer(producto.getIdProducto(), cantidad,
                    referencia == null ? "ABAST-" + System.currentTimeMillis() : referencia);
            Dialogos.exito(this, "Abastecimiento registrado.\nNueva existencia de "
                    + producto.getNombre() + ": " + (producto.getExistencia() + cantidad));
            txtCantidad.setText("");
            txtReferencia.setText("");
            recargar();
        } catch (RuntimeException e) {
            Dialogos.error(this, e);
        }
    }

    private void seleccionarProductoDesdeTabla() {
        int fila = tablaStock.getSelectedRow();
        if (fila < 0) {
            return;
        }
        String codigo = (String) tablaStock.getValueAt(fila, 0);
        for (int i = 0; i < cmbProducto.getItemCount(); i++) {
            if (cmbProducto.getItemAt(i).startsWith(codigo)) {
                cmbProducto.setSelectedIndex(i);
                return;
            }
        }
    }

    private void mostrarExistencia() {
        Object[] entrada = productoSeleccionado();
        if (entrada == null) {
            lblExistenciaActual.setText("-");
            return;
        }
        Producto producto = (Producto) entrada[2];
        lblExistenciaActual.setText(producto.getExistencia() + " unidades");
    }

    private Object[] productoSeleccionado() {
        int indice = cmbProducto.getSelectedIndex() - 1;
        return indice < 0 || indice >= productos.size() ? null : productos.get(indice);
    }

    private void recargar() {
        try {
            DefaultComboBoxModel<String> modelo = new DefaultComboBoxModel<>();
            modelo.addElement("Seleccione un producto...");
            productos.clear();

            DefaultTableModel modeloStock = (DefaultTableModel) tablaStock.getModel();
            modeloStock.setRowCount(0);

            DefaultTableModel modeloMovimientos = (DefaultTableModel) tablaMovimientos.getModel();
            modeloMovimientos.setRowCount(0);

            for (Producto producto : servicio.listarProductos(null, null)) {
                String etiqueta = producto.getCodigo() + " - " + producto.getNombre()
                        + " (" + producto.getCategoria().getEtiqueta() + ")";
                modelo.addElement(etiqueta);
                productos.add(new Object[]{etiqueta, producto.getIdProducto(), producto});
                modeloStock.addRow(new Object[]{
                        producto.getCodigo(),
                        producto.getNombre(),
                        producto.getCategoria().getEtiqueta(),
                        producto.getExistencia()});
            }
            cmbProducto.setModel(modelo);

            for (MovimientoInventario movimiento : servicio.listarMovimientos(null)) {
                modeloMovimientos.addRow(new Object[]{
                        Formato.fechaHora(movimiento.getFechaMovimiento()),
                        movimiento.getCodigoProducto(),
                        movimiento.getNombreProducto(),
                        movimiento.getTipo().getEtiqueta(),
                        movimiento.getCantidad() > 0 ? "+" + movimiento.getCantidad() : movimiento.getCantidad(),
                        movimiento.getExistenciaAnterior(),
                        movimiento.getExistenciaNueva(),
                        movimiento.getReferencia() == null ? "" : movimiento.getReferencia()});
            }

            DefaultTableModel modeloCriticos = (DefaultTableModel) tablaCriticos.getModel();
            modeloCriticos.setRowCount(0);
            for (Producto producto : servicio.listarCriticos()) {
                modeloCriticos.addRow(new Object[]{
                        producto.getCodigo(),
                        producto.getNombre(),
                        producto.getCategoria().getEtiqueta(),
                        producto.getExistencia()});
            }
            mostrarExistencia();
        } catch (RuntimeException e) {
            Dialogos.error(this, e);
        }
    }

    static JPanel panelVacio(String texto) {
        JPanel panel = new JPanel(new GridLayout(1, 1));
        panel.add(new JLabel(texto));
        return panel;
    }
}
