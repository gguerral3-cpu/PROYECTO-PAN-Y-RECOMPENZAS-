package panpuntos.view;

import panpuntos.model.Canje;
import panpuntos.model.Cliente;
import panpuntos.model.MovimientoPuntos;
import panpuntos.model.Recompensa;
import panpuntos.service.ClienteService;
import panpuntos.service.FidelizacionService;
import panpuntos.util.Comprobante;
import panpuntos.util.ExcepcionNegocio;
import panpuntos.util.Formato;

import javax.swing.BorderFactory;
import javax.swing.JButton;
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
import java.util.List;

public class FidelizacionPanel extends JPanel {

    private static final String[] COLUMNAS_RECOMPENSAS =
            {"Codigo", "Recompensa", "Puntos necesarios", "Estado"};
    private static final String[] COLUMNAS_MOVIMIENTOS =
            {"Fecha", "Movimiento", "Puntos", "Saldo", "Referencia"};

    private final ClienteService clienteService = new ClienteService();
    private final FidelizacionService servicio = new FidelizacionService();

    private final JTextField txtDpi = new JTextField(12);
    private final JLabel lblNombre = Componentes.valor("-");
    private final JLabel lblSaldo = Componentes.valor("-");
    private final JTable tablaRecompensas = Componentes.tabla(COLUMNAS_RECOMPENSAS);
    private final JTable tablaMovimientos = Componentes.tabla(COLUMNAS_MOVIMIENTOS);
    private int idClienteSeleccionado;
    private int idRecompensaSeleccionada;

    public FidelizacionPanel() {
        setLayout(new BorderLayout());
        construir();
        recargarRecompensas();
    }

    private void construir() {
        JButton btnBuscar = Componentes.boton("Consultar", Componentes.AZUL_CLARO);
        btnBuscar.addActionListener(e -> consultarCliente());

        JPanel panelCliente = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        panelCliente.add(Componentes.etiqueta("DPI del cliente:"));
        panelCliente.add(txtDpi);
        panelCliente.add(btnBuscar);
        panelCliente.add(Componentes.etiqueta("Cliente:"));
        panelCliente.add(lblNombre);
        panelCliente.add(Componentes.etiqueta("Saldo de puntos:"));
        panelCliente.add(lblSaldo);
        panelCliente.setBorder(BorderFactory.createTitledBorder("1. Consulta de puntos acumulados"));
        txtDpi.addActionListener(e -> consultarCliente());

        tablaRecompensas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaRecompensas.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int fila = tablaRecompensas.getSelectedRow();
                idRecompensaSeleccionada = fila < 0 ? 0
                        : servicio.listarRecompensas(null).get(fila).getIdRecompensa();
            }
        });

        JButton btnCanjear = Componentes.boton("Canjear recompensa", Componentes.VERDE);
        JButton btnComprobante = Componentes.boton("Comprobante de canje", Componentes.AZUL);
        btnCanjear.addActionListener(e -> canjear());
        btnComprobante.addActionListener(e -> verComprobanteCanje());

        JPanel panelRecompensas = new JPanel(new BorderLayout(0, 6));
        panelRecompensas.add(Componentes.titulo("2. Catalogo de recompensas"), BorderLayout.NORTH);
        panelRecompensas.add(Componentes.panelDesplazable(tablaRecompensas), BorderLayout.CENTER);
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        acciones.add(btnCanjear);
        acciones.add(btnComprobante);
        panelRecompensas.add(acciones, BorderLayout.SOUTH);

        JPanel panelMovimientos = new JPanel(new BorderLayout());
        panelMovimientos.add(Componentes.titulo("3. Historial de movimientos de puntos"), BorderLayout.NORTH);
        panelMovimientos.add(Componentes.panelDesplazable(tablaMovimientos), BorderLayout.CENTER);

        JPanel centro = new JPanel(new java.awt.GridLayout(1, 2, 10, 0));
        centro.add(panelRecompensas);
        centro.add(panelMovimientos);

        JPanel contenido = new JPanel(new BorderLayout(0, 8));
        contenido.add(panelCliente, BorderLayout.NORTH);
        contenido.add(centro, BorderLayout.CENTER);
        contenido.setBorder(new EmptyBorder(10, 10, 10, 10));

        setLayout(new BorderLayout());
        add(Componentes.titulo("Sistema de fidelizacion"), BorderLayout.NORTH);
        add(contenido, BorderLayout.CENTER);
    }

    private void consultarCliente() {
        try {
            Cliente cliente = clienteService.buscarPorDpi(txtDpi.getText().trim());
            idClienteSeleccionado = cliente.getIdCliente();
            lblNombre.setText(cliente.getNombre());
            lblSaldo.setText(Formato.puntos(cliente.getSaldoPuntos()));
            if (!cliente.puedeOperar()) {
                Dialogos.aviso(this, "El cliente esta inactivo: no puede canjear recompensas.");
            }
            recargarMovimientos();
        } catch (RuntimeException e) {
            Dialogos.error(this, e);
        }
    }

    private void canjear() {
        if (idClienteSeleccionado == 0) {
            Dialogos.aviso(this, "Consulte primero al cliente.");
            return;
        }
        if (idRecompensaSeleccionada == 0) {
            Dialogos.aviso(this, "Seleccione una recompensa del catalogo.");
            return;
        }
        Recompensa recompensa = servicio.listarRecompensas(null).stream()
                .filter(r -> r.getIdRecompensa() == idRecompensaSeleccionada)
                .findFirst()
                .orElseThrow(() -> new ExcepcionNegocio("La recompensa ya no existe."));
        if (!Dialogos.confirmar(this, "Canjear \"" + recompensa.getNombre() + "\" por "
                + recompensa.getPuntosNecesarios() + " puntos?")) {
            return;
        }
        try {
            Canje canje = servicio.canjear(idClienteSeleccionado, idRecompensaSeleccionada);
            Cliente cliente = clienteService.buscarPorId(idClienteSeleccionado);
            Dialogos.exito(this, "Canje registrado. Nuevo saldo: " + Formato.puntos(cliente.getSaldoPuntos()));
            Dialogos.mostrarTexto(this, "Comprobante de canje", Comprobante.deCanje(
                    canje.getNombreCliente(), canje.getNombreRecompensa(),
                    canje.getPuntosUsados(), cliente.getSaldoPuntos()));
            consultarCliente();
        } catch (RuntimeException e) {
            Dialogos.error(this, e);
        }
    }

    private void verComprobanteCanje() {
        List<Canje> canjes = servicio.listarCanjes();
        if (canjes.isEmpty()) {
            Dialogos.aviso(this, "Todavia no hay canjes registrados.");
            return;
        }
        StringBuilder texto = new StringBuilder();
        for (Canje canje : canjes) {
            texto.append(String.format("%-12s %-22s %-28s -%3d%n",
                    Formato.fecha(canje.getFechaCanje()),
                    canje.getNombreCliente(),
                    canje.getNombreRecompensa(),
                    canje.getPuntosUsados()));
        }
        Dialogos.mostrarTexto(this, "Historial de canjes", texto.toString());
    }

    private void recargarRecompensas() {
        try {
            DefaultTableModel modelo = (DefaultTableModel) tablaRecompensas.getModel();
            modelo.setRowCount(0);
            for (Recompensa recompensa : servicio.listarRecompensas(null)) {
                modelo.addRow(new Object[]{
                        recompensa.getCodigo(),
                        recompensa.getNombre(),
                        recompensa.getPuntosNecesarios(),
                        recompensa.getEstado().getEtiqueta()});
            }
        } catch (RuntimeException e) {
            Dialogos.error(this, e);
        }
    }

    private void recargarMovimientos() {
        DefaultTableModel modelo = (DefaultTableModel) tablaMovimientos.getModel();
        modelo.setRowCount(0);
        if (idClienteSeleccionado == 0) {
            return;
        }
        try {
            for (MovimientoPuntos movimiento : clienteService.historialDePuntos(idClienteSeleccionado)) {
                modelo.addRow(new Object[]{
                        Formato.fechaHora(movimiento.getFechaMovimiento()),
                        movimiento.getTipo().getEtiqueta(),
                        Formato.puntosConSigno(movimiento.getPuntos()),
                        movimiento.getSaldoResultante(),
                        movimiento.getReferencia() == null ? "" : movimiento.getReferencia()});
            }
        } catch (RuntimeException e) {
            Dialogos.error(this, e);
        }
    }

    static JPanel separador() {
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(10, 10));
        return panel;
    }
}
