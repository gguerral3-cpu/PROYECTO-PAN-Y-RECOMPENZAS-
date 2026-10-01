package panpuntos.view;

import panpuntos.model.Cliente;
import panpuntos.model.ItemVenta;
import panpuntos.model.Menu;
import panpuntos.model.Pedido;
import panpuntos.model.Producto;
import panpuntos.service.ResultadoCobro;
import panpuntos.model.TipoPago;
import panpuntos.service.CatalogoService;
import panpuntos.service.ClienteService;
import panpuntos.service.ClienteSnapshot;
import panpuntos.service.PagoService;
import panpuntos.service.PedidoService;
import panpuntos.util.Comprobante;
import panpuntos.util.Formato;
import panpuntos.util.Validaciones;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

public class PedidoPanel extends JPanel {

    private static final String[] COLUMNAS_DETALLE =
            {"Producto", "Detalle", "Cant.", "Precio unit.", "Subtotal", "Puntos"};

    private final ClienteService clienteService = new ClienteService();
    private final CatalogoService catalogoService = new CatalogoService();
    private final PagoService pagoService = new PagoService();

    private final JTextField txtDpi = new JTextField(12);
    private final JButton btnBuscarCliente = Componentes.boton("Buscar cliente", Componentes.AZUL_CLARO);
    private final JLabel lblNombreCliente = new JLabel("Seleccione un cliente");
    private final JLabel lblSaldoPuntos = new JLabel("-");

    private final JComboBox<String> cmbCatalogo = new JComboBox<>();
    private final JSpinner spnCantidad = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
    private final JButton btnAgregar = Componentes.boton("Agregar al pedido", Componentes.VERDE);

    private final JTable tablaDetalle = Componentes.tabla(COLUMNAS_DETALLE);

    private final JLabel lblSubtotal = Componentes.valor(Formato.moneda(0));
    private final JLabel lblTotal = Componentes.valor(Formato.moneda(0));
    private final JLabel lblPuntos = Componentes.valor("+0");

    private final JRadioButton rbEfectivo = new JRadioButton("Efectivo", true);
    private final JRadioButton rbTarjeta = new JRadioButton("Tarjeta");
    private final JTextField txtEfectivo = new JTextField(8);
    private final JTextField txtReferencia = new JTextField(12);

    private final JButton btnQuitar = Componentes.boton("Quitar producto", Componentes.ROJO);
    private final JButton btnLimpiar = Componentes.botonSecundario("Limpiar pedido");
    private final JButton btnConfirmar = Componentes.boton("Confirmar pedido", Componentes.AZUL);
    private final JButton btnCobrar = Componentes.boton("Cobrar pedido", Componentes.VERDE);
    private final JButton btnComprobante = Componentes.boton("Generar comprobante", Componentes.AZUL_CLARO);

    private final List<Object[]> catalogo = new ArrayList<>();
    private Pedido pedidoEnCurso;
    private Cliente clienteSeleccionado;

    public PedidoPanel() {
        setLayout(new BorderLayout());
        construir();
        cargarCatalogo();
        actualizarTotales();
    }

    private void construir() {
        JPanel panelCliente = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        panelCliente.add(Componentes.etiqueta("DPI del cliente:"));
        panelCliente.add(txtDpi);
        panelCliente.add(btnBuscarCliente);
        panelCliente.add(lblNombreCliente);
        panelCliente.add(Componentes.etiqueta("Saldo:"));
        panelCliente.add(lblSaldoPuntos);
        panelCliente.setBorder(BorderFactory.createTitledBorder("1. Cliente del pedido"));
        txtDpi.addActionListener(e -> buscarCliente());
        btnBuscarCliente.addActionListener(e -> buscarCliente());

        JPanel panelCatalogo = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        panelCatalogo.add(Componentes.etiqueta("Catalogo:"));
        cmbCatalogo.setPreferredSize(new Dimension(340, 28));
        panelCatalogo.add(cmbCatalogo);
        panelCatalogo.add(Componentes.etiqueta("Cantidad:"));
        spnCantidad.setPreferredSize(new Dimension(70, 28));
        panelCatalogo.add(spnCantidad);
        panelCatalogo.add(btnAgregar);
        panelCatalogo.setBorder(BorderFactory.createTitledBorder("2. Productos y menus disponibles"));
        btnAgregar.addActionListener(e -> agregarAlPedido());

        JPanel panelPago = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        ButtonGroup grupo = new ButtonGroup();
        grupo.add(rbEfectivo);
        grupo.add(rbTarjeta);
        panelPago.add(Componentes.etiqueta("5. Metodo de pago:"));
        panelPago.add(rbEfectivo);
        panelPago.add(txtEfectivo);
        panelPago.add(Componentes.etiqueta("Efectivo recibido"));
        panelPago.add(rbTarjeta);
        panelPago.add(txtReferencia);
        panelPago.add(Componentes.etiqueta("Referencia"));
        panelPago.setBorder(BorderFactory.createTitledBorder("3. Cobro"));
        rbEfectivo.addActionListener(e -> {
            txtEfectivo.setEnabled(true);
            txtReferencia.setEnabled(false);
        });
        rbTarjeta.addActionListener(e -> {
            txtEfectivo.setEnabled(false);
            txtReferencia.setEnabled(true);
        });
        txtReferencia.setEnabled(false);

        JPanel panelTotales = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 10, 4, 10);
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        panelTotales.add(Componentes.etiqueta("Subtotal:"), c);
        c.gridx = 1;
        panelTotales.add(lblSubtotal, c);
        c.gridx = 2;
        panelTotales.add(Componentes.etiqueta("TOTAL:"), c);
        c.gridx = 3;
        panelTotales.add(lblTotal, c);
        c.gridx = 4;
        panelTotales.add(Componentes.etiqueta("4. Puntos que generara:"), c);
        c.gridx = 5;
        panelTotales.add(lblPuntos, c);
        panelTotales.setBorder(BorderFactory.createTitledBorder("Totales del pedido"));

        JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        panelAcciones.add(btnQuitar);
        panelAcciones.add(btnLimpiar);
        panelAcciones.add(btnConfirmar);
        panelAcciones.add(btnCobrar);
        panelAcciones.add(btnComprobante);

        JPanel norte = new JPanel(new BorderLayout(0, 4));
        norte.add(panelCliente, BorderLayout.NORTH);
        norte.add(panelCatalogo, BorderLayout.SOUTH);
        norte.setBorder(new EmptyBorder(10, 10, 0, 10));

        JPanel centro = new JPanel(new BorderLayout(0, 8));
        centro.add(Componentes.panelDesplazable(tablaDetalle), BorderLayout.CENTER);
        centro.setBorder(new EmptyBorder(8, 10, 0, 10));

        JPanel sur = new JPanel(new BorderLayout(0, 6));
        JPanel surOeste = new JPanel(new BorderLayout());
        surOeste.add(panelTotales, BorderLayout.NORTH);
        surOeste.add(panelPago, BorderLayout.CENTER);
        sur.add(surOeste, BorderLayout.CENTER);
        sur.add(panelAcciones, BorderLayout.SOUTH);
        sur.setBorder(new EmptyBorder(0, 10, 10, 10));

        btnQuitar.addActionListener(e -> quitarProducto());
        btnLimpiar.addActionListener(e -> limpiarPedido());
        btnConfirmar.addActionListener(e -> confirmarPedido());
        btnCobrar.addActionListener(e -> cobrar());
        btnComprobante.addActionListener(e -> generarComprobante());

        JPanel contenedor = new JPanel(new BorderLayout(0, 6));
        contenedor.add(Componentes.titulo("Registrar pedido"), BorderLayout.NORTH);
        contenedor.add(norte, BorderLayout.CENTER);
        contenedor.add(centro, BorderLayout.CENTER);
        contenedor.add(sur, BorderLayout.SOUTH);
        add(contenedor, BorderLayout.CENTER);

        btnCobrar.setEnabled(false);
        btnComprobante.setEnabled(false);
    }

    private void cargarCatalogo() {
        DefaultComboBoxModel<String> modelo = new DefaultComboBoxModel<>();
        modelo.addElement("Seleccione un producto o menu...");
        catalogo.clear();
        for (Producto producto : catalogoService.listarProductos(null, null)) {
            if (producto.getEstado().estaActivo()) {
                String etiqueta = producto.getCodigo() + " - " + producto.getNombre()
                        + " (" + producto.getCategoria().getEtiqueta() + ") " + Formato.moneda(producto.getPrecioUnitario())
                        + " [" + producto.getExistencia() + " disp.]";
                modelo.addElement(etiqueta);
                catalogo.add(new Object[]{etiqueta, "PRODUCTO", producto});
            }
        }
        for (Menu menu : catalogoService.listarMenus(null)) {
            if (menu.getEstado().estaActivo()) {
                String etiqueta = menu.getCodigo() + " - " + menu.getNombre()
                        + " (Menu completo) " + Formato.moneda(menu.getPrecioMenu())
                        + " [" + menu.getComponentes().size() + " componentes]";
                modelo.addElement(etiqueta);
                catalogo.add(new Object[]{etiqueta, "MENU", menu});
            }
        }
        cmbCatalogo.setModel(modelo);
    }

    private void buscarCliente() {
        try {
            clienteSeleccionado = clienteService.buscarPorDpi(txtDpi.getText().trim());
            lblNombreCliente.setText(clienteSeleccionado.getNombre());
            lblSaldoPuntos.setText(Formato.puntos(clienteSeleccionado.getSaldoPuntos()));
            if (!clienteSeleccionado.puedeOperar()) {
                Dialogos.aviso(this, "El cliente esta inactivo: no puede registrar pedidos ni canjear recompensas.");
            }
        } catch (RuntimeException e) {
            Dialogos.error(this, e);
        }
    }

    private void agregarAlPedido() {
        int indice = cmbCatalogo.getSelectedIndex() - 1;
        if (indice < 0 || indice >= catalogo.size()) {
            Dialogos.aviso(this, "Seleccione un producto o menu del catalogo.");
            return;
        }
        if (clienteSeleccionado == null) {
            Dialogos.aviso(this, "Primero debe buscar al cliente del pedido.");
            return;
        }
        int cantidad = (Integer) spnCantidad.getValue();
        try {
            ItemVenta item;
            Object[] entrada = catalogo.get(indice);
            if ("PRODUCTO".equals(entrada[1])) {
                Producto producto = (Producto) entrada[2];
                producto = catalogoService.buscarProductoPorId(producto.getIdProducto());
                item = producto.crearItem(cantidad);
            } else {
                Menu menu = catalogoService.buscarMenuPorId(((Menu) entrada[2]).getIdMenu());
                item = menu.crearItem(cantidad);
            }
            if (pedidoEnCurso == null) {
                pedidoEnCurso = new Pedido(clienteSeleccionado);
            }
            pedidoEnCurso.agregarItem(item);
            actualizarDetalle();
        } catch (RuntimeException e) {
            Dialogos.error(this, e);
        }
    }

    private void quitarProducto() {
        int fila = tablaDetalle.getSelectedRow();
        if (fila < 0) {
            Dialogos.aviso(this, "Seleccione la fila del producto que desea quitar.");
            return;
        }
        if (pedidoEnCurso == null) {
            return;
        }
        try {
            pedidoEnCurso.quitarItem(fila);
            actualizarDetalle();
        } catch (RuntimeException e) {
            Dialogos.error(this, e);
        }
    }

    private void limpiarPedido() {
        if (pedidoEnCurso != null && pedidoEnCurso.getIdPedido() > 0) {
            Dialogos.aviso(this, "El pedido ya fue confirmado en la base de datos y no puede modificarse.");
            return;
        }
        pedidoEnCurso = null;
        ((DefaultTableModel) tablaDetalle.getModel()).setRowCount(0);
        actualizarTotales();
    }

    private void confirmarPedido() {
        if (clienteSeleccionado == null) {
            Dialogos.aviso(this, "Debe seleccionar el cliente del pedido.");
            return;
        }
        if (pedidoEnCurso == null || pedidoEnCurso.estaVacio()) {
            Dialogos.aviso(this, "Agregue al menos un producto al pedido.");
            return;
        }
        try {
            PedidoService pedidoService = new PedidoService();
            Pedido guardado = pedidoService.crearPedido(
                    ClienteSnapshot.de(clienteSeleccionado), pedidoEnCurso.getItems());
            pedidoEnCurso = guardado;
            Dialogos.exito(this, "Pedido " + guardado.getIdPedido() + " registrado con estado PENDIENTE.");
            btnCobrar.setEnabled(true);
            buscarCliente();
        } catch (RuntimeException e) {
            Dialogos.error(this, e);
        }
    }

    private void cobrar() {
        if (pedidoEnCurso == null || pedidoEnCurso.getIdPedido() == 0) {
            Dialogos.aviso(this, "Primero debe confirmar el pedido.");
            return;
        }
        try {
            TipoPago tipo = rbEfectivo.isSelected() ? TipoPago.EFECTIVO : TipoPago.TARJETA;
            Double efectivo = null;
            if (tipo == TipoPago.EFECTIVO) {
                efectivo = Validaciones.decimalPositivo(txtEfectivo.getText(), "efectivo recibido");
            }
            String referencia = tipo == TipoPago.TARJETA
                    ? Validaciones.textoRequerido(txtReferencia.getText(), "referencia") : null;
            boolean aprobada = true;
            if (tipo == TipoPago.TARJETA) {
                int opcion = JOptionPane.showConfirmDialog(this,
                        "Simulacion de la transaccion con tarjeta\n\n"
                                + "Desea confirmar que la operacion fue APROBADA?",
                        "Pago con tarjeta", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                aprobada = opcion == JOptionPane.YES_OPTION;
            }
            ResultadoCobro resultado = pagoService.cobrar(pedidoEnCurso.getIdPedido(), tipo,
                    efectivo, referencia, aprobada);
            if (!resultado.aprobado()) {
                Dialogos.aviso(this, "El pago con tarjeta fue RECHAZADO.\n"
                        + "No se registro la venta ni se acreditaron puntos.");
                return;
            }
            pedidoEnCurso = resultado.pedido();
            actualizarDetalle();
            buscarCliente();
            btnCobrar.setEnabled(false);
            btnComprobante.setEnabled(true);
            if (resultado.pago().getTipoPago() == TipoPago.EFECTIVO) {
                Dialogos.exito(this, "Pago registrado.\nCambio a entregar: "
                        + Formato.moneda(resultado.pago().getCambio())
                        + "\nPuntos acreditados: +" + resultado.puntosAcreditados());
            } else {
                Dialogos.exito(this, "Pago con tarjeta aprobado.\nPuntos acreditados: +"
                        + resultado.puntosAcreditados());
            }
        } catch (RuntimeException e) {
            Dialogos.error(this, e);
        }
    }

    private void generarComprobante() {
        if (pedidoEnCurso == null || pedidoEnCurso.getPago() == null) {
            Dialogos.aviso(this, "El comprobante se genera despues de registrar el pago.");
            return;
        }
        try {
            Cliente cliente = clienteService.buscarPorId(pedidoEnCurso.getCliente().getIdCliente());
            pedidoEnCurso.setCliente(cliente);
            Dialogos.mostrarTexto(this, "Comprobante de venta", Comprobante.deVenta(pedidoEnCurso));
        } catch (RuntimeException e) {
            Dialogos.error(this, e);
        }
    }

    private void actualizarDetalle() {
        DefaultTableModel modelo = (DefaultTableModel) tablaDetalle.getModel();
        modelo.setRowCount(0);
        if (pedidoEnCurso == null) {
            actualizarTotales();
            return;
        }
        for (ItemVenta item : pedidoEnCurso.getItems()) {
            modelo.addRow(new Object[]{
                    item.getCodigo() + " - " + item.getDescripcion(),
                    item.getDetalleComposicion(),
                    item.getCantidad(),
                    Formato.moneda(item.getPrecioUnitario()),
                    Formato.moneda(item.getSubtotal()),
                    Formato.puntosConSigno(item.calcularPuntos())});
        }
        actualizarTotales();
    }

    private void actualizarTotales() {
        if (pedidoEnCurso == null) {
            lblSubtotal.setText(Formato.moneda(0));
            lblTotal.setText(Formato.moneda(0));
            lblPuntos.setText("+0");
            return;
        }
        double total = pedidoEnCurso.getTotal();
        lblSubtotal.setText(Formato.moneda(total));
        lblTotal.setText(Formato.moneda(total));
        lblPuntos.setText(Formato.puntosConSigno(pedidoEnCurso.getPuntosGenerados()));
    }
}
