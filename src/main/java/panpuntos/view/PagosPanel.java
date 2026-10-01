package panpuntos.view;

import panpuntos.model.PedidoResumen;
import panpuntos.model.EstadoPedido;
import panpuntos.service.PedidoService;
import panpuntos.service.ResultadoCobro;
import panpuntos.model.TipoPago;
import panpuntos.service.PagoService;
import panpuntos.util.Comprobante;
import panpuntos.util.Formato;
import panpuntos.util.Validaciones;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Date;

public class PagosPanel extends JPanel {

    private static final String[] COLUMNAS = {
            "Pedido", "Fecha", "DPI", "Cliente", "Total", "Puntos", "Estado", "Pago"};

    private final PagoService servicio = new PagoService();
    private final PedidoService pedidoService = new PedidoService();

    private final JTable tabla = Componentes.tabla(COLUMNAS);
    private final JComboBox<String> cmbEstado = new JComboBox<>();
    private final JLabel lblDetalle = Componentes.valor("-");
    private final JRadioButton rbEfectivo = new JRadioButton("Efectivo", true);
    private final JRadioButton rbTarjeta = new JRadioButton("Tarjeta");
    private final JTextField txtEfectivo = new JTextField(8);
    private final JTextField txtReferencia = new JTextField(12);
    private int pedidoSeleccionado;

    public PagosPanel() {
        setLayout(new BorderLayout());
        construir();
        recargar();
    }

    private void construir() {
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionarPedido();
            }
        });

        JPanel panelCobro = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        ButtonGroup grupo = new ButtonGroup();
        grupo.add(rbEfectivo);
        grupo.add(rbTarjeta);
        panelCobro.add(Componentes.etiqueta("Metodo:"));
        panelCobro.add(rbEfectivo);
        panelCobro.add(txtEfectivo);
        panelCobro.add(Componentes.etiqueta("Efectivo recibido"));
        panelCobro.add(rbTarjeta);
        panelCobro.add(txtReferencia);
        panelCobro.add(Componentes.etiqueta("Referencia"));
        panelCobro.setBorder(BorderFactory.createTitledBorder("Datos del cobro"));
        txtReferencia.setEnabled(false);
        rbEfectivo.addActionListener(e -> {
            txtEfectivo.setEnabled(true);
            txtReferencia.setEnabled(false);
        });
        rbTarjeta.addActionListener(e -> {
            txtEfectivo.setEnabled(false);
            txtReferencia.setEnabled(true);
        });

        JButton btnCobrar = Componentes.boton("Cobrar", Componentes.VERDE);
        JButton btnEntregado = Componentes.boton("Marcar entregado", Componentes.AZUL_CLARO);
        JButton btnAnular = Componentes.botonPeligro("Anular pedido");
        JButton btnComprobante = Componentes.boton("Comprobante", Componentes.AZUL);
        JButton btnRefrescar = Componentes.botonSecundario("Refrescar");
        btnCobrar.addActionListener(e -> cobrar());
        btnEntregado.addActionListener(e -> marcarEntregado());
        btnAnular.addActionListener(e -> anular());
        btnComprobante.addActionListener(e -> comprobante());
        btnRefrescar.addActionListener(e -> recargar());

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        acciones.add(btnCobrar);
        acciones.add(btnEntregado);
        acciones.add(btnAnular);
        acciones.add(btnComprobante);
        acciones.add(btnRefrescar);

        JPanel sur = new JPanel(new BorderLayout(0, 6));
        JPanel cabecera = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        cabecera.add(Componentes.etiqueta("Pedido seleccionado:"));
        cabecera.add(lblDetalle);
        sur.add(cabecera, BorderLayout.NORTH);
        sur.add(panelCobro, BorderLayout.CENTER);
        sur.add(acciones, BorderLayout.SOUTH);
        sur.setBorder(new EmptyBorder(0, 10, 10, 10));

        JPanel lista = new JPanel(new BorderLayout(0, 6));
        JPanel filtro = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        filtro.add(Componentes.etiqueta("Filtrar por estado:"));
        cmbEstado.addItem("Todos los estados");
        cmbEstado.addItem("Pendiente");
        cmbEstado.addItem("Pagado");
        cmbEstado.addItem("Entregado");
        cmbEstado.addItem("Anulado");
        cmbEstado.addActionListener(e -> recargar());
        filtro.add(cmbEstado);
        lista.add(Componentes.titulo("Pedidos registrados"), BorderLayout.NORTH);
        lista.add(filtro, BorderLayout.NORTH);
        lista.add(Componentes.panelDesplazable(tabla), BorderLayout.CENTER);
        lista.setBorder(new EmptyBorder(0, 10, 0, 10));

        JPanel contenido = new JPanel(new BorderLayout(0, 8));
        contenido.add(lista, BorderLayout.CENTER);
        contenido.add(sur, BorderLayout.SOUTH);

        setLayout(new BorderLayout());
        add(Componentes.titulo("Cobro de pedidos"), BorderLayout.NORTH);
        add(contenido, BorderLayout.CENTER);
    }

    private void seleccionarPedido() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            pedidoSeleccionado = 0;
            lblDetalle.setText("-");
            return;
        }
        pedidoSeleccionado = (int) tabla.getValueAt(fila, 0);
        lblDetalle.setText("#" + pedidoSeleccionado
                + "  " + tabla.getValueAt(fila, 3)
                + "  " + tabla.getValueAt(fila, 4)
                + "  [" + tabla.getValueAt(fila, 6) + "]");
        txtEfectivo.setText(String.valueOf(tabla.getValueAt(fila, 4)).replace("Q", ""));
    }

    private void cobrar() {
        if (pedidoSeleccionado == 0) {
            Dialogos.aviso(this, "Seleccione un pedido pendiente de la tabla.");
            return;
        }
        try {
            TipoPago tipo = rbEfectivo.isSelected() ? TipoPago.EFECTIVO : TipoPago.TARJETA;
            Double efectivo = tipo == TipoPago.EFECTIVO
                    ? Validaciones.decimalPositivo(txtEfectivo.getText(), "efectivo recibido") : null;
            String referencia = tipo == TipoPago.TARJETA
                    ? Validaciones.textoRequerido(txtReferencia.getText(), "referencia") : null;
            boolean aprobada = true;
            if (tipo == TipoPago.TARJETA) {
                int opcion = JOptionPane.showConfirmDialog(this,
                        "Simulacion de la transaccion con tarjeta.\nDesea confirmar que fue APROBADA?",
                        "Pago con tarjeta", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                aprobada = opcion == JOptionPane.YES_OPTION;
            }
            ResultadoCobro resultado = servicio.cobrar(pedidoSeleccionado, tipo, efectivo, referencia, aprobada);
            if (!resultado.aprobado()) {
                Dialogos.aviso(this, "Pago RECHAZADO. No se registro la venta ni se acreditaron puntos.");
            } else if (tipo == TipoPago.EFECTIVO) {
                Dialogos.exito(this, "Pago registrado. Cambio: " + Formato.moneda(resultado.pago().getCambio())
                        + "\nPuntos acreditados: +" + resultado.puntosAcreditados());
            } else {
                Dialogos.exito(this, "Pago con tarjeta aprobado. Puntos acreditados: +"
                        + resultado.puntosAcreditados());
            }
            pedidoSeleccionado = 0;
            recargar();
        } catch (RuntimeException e) {
            Dialogos.error(this, e);
        }
    }

    private void marcarEntregado() {
        if (pedidoSeleccionado == 0) {
            Dialogos.aviso(this, "Seleccione un pedido de la tabla.");
            return;
        }
        try {
            servicio.marcarEntregado(pedidoSeleccionado);
            Dialogos.exito(this, "El pedido fue marcado como ENTREGADO.");
            recargar();
        } catch (RuntimeException e) {
            Dialogos.error(this, e);
        }
    }

    private void anular() {
        if (pedidoSeleccionado == 0) {
            Dialogos.aviso(this, "Seleccione un pedido de la tabla.");
            return;
        }
        if (!Dialogos.confirmar(this, "Anular el pedido " + pedidoSeleccionado + "?\n"
                + "Un pedido pendiente puede anularse sin afectar inventario ni puntos.")) {
            return;
        }
        try {
            servicio.anularPedidoPendiente(pedidoSeleccionado);
            Dialogos.exito(this, "El pedido fue anulado.");
            pedidoSeleccionado = 0;
            recargar();
        } catch (RuntimeException e) {
            Dialogos.error(this, e);
        }
    }

    private void comprobante() {
        if (pedidoSeleccionado == 0) {
            Dialogos.aviso(this, "Seleccione un pedido de la tabla.");
            return;
        }
        try {
            Dialogos.mostrarTexto(this, "Comprobante de venta",
                    Comprobante.deVenta(servicio.buscarPedido(pedidoSeleccionado)));
        } catch (RuntimeException e) {
            Dialogos.error(this, e);
        }
    }

    private void recargar() {
        try {
            DefaultTableModel modelo = (DefaultTableModel) tabla.getModel();
            modelo.setRowCount(0);
            EstadoPedido filtro = estadoSeleccionado();
            for (PedidoResumen pedido : pedidoService.listar(filtro, null, null, null)) {
                modelo.addRow(new Object[]{
                        pedido.getIdPedido(),
                        Formato.fechaHora(pedido.getFechaPedido()),
                        pedido.getDpi(),
                        pedido.getNombreCliente(),
                        Formato.moneda(pedido.getTotal()),
                        pedido.getPuntosGenerados(),
                        pedido.getEstado().getEtiqueta(),
                        pedido.getTipoPago() == null ? "-" : pedido.getTipoPago().getEtiqueta()});
            }
            lblDetalle.setText("-");
            pedidoSeleccionado = 0;
        } catch (RuntimeException e) {
            Dialogos.error(this, e);
        }
    }

    private EstadoPedido estadoSeleccionado() {
        String texto = (String) cmbEstado.getSelectedItem();
        if (texto == null || texto.startsWith("Todos")) {
            return null;
        }
        return EstadoPedido.valueOf(texto.toUpperCase());
    }

    static Date hoy() {
        return new Date();
    }
}
