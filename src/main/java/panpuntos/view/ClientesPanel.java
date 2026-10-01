package panpuntos.view;

import panpuntos.model.Cliente;
import panpuntos.model.EstadoCliente;
import panpuntos.model.MovimientoPuntos;
import panpuntos.service.ClienteService;
import panpuntos.util.ExcepcionNegocio;
import panpuntos.util.Formato;
import panpuntos.util.Validaciones;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;

public class ClientesPanel extends JPanel {

    private static final String[] COLUMNAS = {"ID", "DPI", "Nombre", "Telefono", "Correo", "Puntos", "Estado"};

    private final ClienteService servicio = new ClienteService();

    private final JTextField txtDpi = new JTextField(18);
    private final JTextField txtNombre = new JTextField(18);
    private final JTextField txtTelefono = new JTextField(18);
    private final JTextField txtCorreo = new JTextField(18);
    private final JTextField txtFiltro = new JTextField(18);
    private final JTable tabla = Componentes.tabla(COLUMNAS);
    private int idSeleccionado = 0;

    public ClientesPanel() {
        setLayout(new BorderLayout());
        construir();
        recargar();
    }

    private void construir() {
        JPanel formulario = Componentes.panelCampos(4);
        formulario.add(Componentes.etiqueta("DPI"));
        formulario.add(txtDpi);
        formulario.add(Componentes.etiqueta("Nombre completo"));
        formulario.add(txtNombre);
        formulario.add(Componentes.etiqueta("Telefono"));
        formulario.add(txtTelefono);
        formulario.add(Componentes.etiqueta("Correo electronico"));
        formulario.add(txtCorreo);
        formulario.setBorder(BorderFactory.createTitledBorder("Datos del cliente"));

        JButton btnNuevo = Componentes.boton("Nuevo");
        JButton btnGuardar = Componentes.boton("Guardar", Componentes.VERDE);
        JButton btnActualizar = Componentes.boton("Actualizar", Componentes.AZUL_CLARO);
        JButton btnLimpiar = Componentes.botonSecundario("Limpiar");
        JButton btnEstado = Componentes.boton("Cambiar estado", Componentes.ROJO);
        JButton btnHistorial = Componentes.boton("Historial de puntos", Componentes.AZUL_CLARO);

        btnNuevo.addActionListener(e -> limpiarFormulario());
        btnGuardar.addActionListener(e -> guardar());
        btnActualizar.addActionListener(e -> actualizar());
        btnLimpiar.addActionListener(e -> limpiarFormulario());
        btnEstado.addActionListener(e -> cambiarEstado());
        btnHistorial.addActionListener(e -> verHistorial());

        JPanel acciones = new JPanel(new GridLayout(2, 3, 8, 8));
        acciones.add(btnNuevo);
        acciones.add(btnGuardar);
        acciones.add(btnActualizar);
        acciones.add(btnLimpiar);
        acciones.add(btnEstado);
        acciones.add(btnHistorial);
        acciones.setBorder(new EmptyBorder(0, 12, 12, 12));

        JPanel izq = new JPanel(new BorderLayout(0, 8));
        izq.setBorder(new EmptyBorder(12, 12, 12, 6));
        izq.add(formulario, BorderLayout.NORTH);
        izq.add(acciones, BorderLayout.CENTER);

        JPanel busqueda = new JPanel(new BorderLayout(8, 0));
        busqueda.add(Componentes.etiqueta("Buscar"), BorderLayout.WEST);
        busqueda.add(txtFiltro, BorderLayout.CENTER);
        JButton btnBuscar = Componentes.boton("Buscar", Componentes.AZUL_CLARO);
        btnBuscar.addActionListener(e -> recargar());
        txtFiltro.addActionListener(e -> recargar());
        busqueda.add(btnBuscar, BorderLayout.EAST);
        busqueda.setBorder(new EmptyBorder(0, 0, 8, 0));

        JPanel derecha = new JPanel(new BorderLayout(0, 8));
        derecha.setBorder(new EmptyBorder(12, 6, 12, 12));
        derecha.add(Componentes.titulo("Clientes registrados"), BorderLayout.NORTH);
        derecha.add(busqueda, BorderLayout.NORTH);
        derecha.add(Componentes.panelDesplazable(tabla), BorderLayout.CENTER);

        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarSeleccion();
            }
        });

        JPanel contenido = new JPanel(new java.awt.GridLayout(1, 2, 0, 0));
        contenido.add(izq);
        contenido.add(derecha);

        setLayout(new BorderLayout());
        add(contenido, BorderLayout.CENTER);
        setMinimumSize(new Dimension(900, 560));
    }

    private void guardar() {
        try {
            Cliente cliente = servicio.registrar(
                    Validaciones.dpiRequerido(txtDpi.getText()),
                    Validaciones.textoRequerido(txtNombre.getText(), "nombre"),
                    Validaciones.textoOpcional(txtTelefono.getText()),
                    Validaciones.correoValido(txtCorreo.getText()));
            Dialogos.exito(this, "Cliente registrado. Saldo inicial de puntos: 0");
            limpiarFormulario();
            recargar();
            seleccionarPorId(cliente.getIdCliente());
        } catch (RuntimeException e) {
            Dialogos.error(this, e);
        }
    }

    private void actualizar() {
        if (idSeleccionado == 0) {
            Dialogos.aviso(this, "Seleccione un cliente de la tabla antes de actualizar.");
            return;
        }
        try {
            Cliente cliente = servicio.buscarPorId(idSeleccionado);
            cliente.setDpi(Validaciones.dpiRequerido(txtDpi.getText()));
            cliente.setNombre(Validaciones.textoRequerido(txtNombre.getText(), "nombre"));
            cliente.setTelefono(Validaciones.textoOpcional(txtTelefono.getText()));
            cliente.setEmail(Validaciones.correoValido(txtCorreo.getText()));
            servicio.actualizar(cliente);
            Dialogos.exito(this, "Los datos del cliente fueron actualizados.");
            recargar();
        } catch (RuntimeException e) {
            Dialogos.error(this, e);
        }
    }

    private void cambiarEstado() {
        if (idSeleccionado == 0) {
            Dialogos.aviso(this, "Seleccione un cliente de la tabla.");
            return;
        }
        Cliente cliente = servicio.buscarPorId(idSeleccionado);
        if (cliente == null) {
            Dialogos.aviso(this, "El cliente seleccionado ya no existe.");
            return;
        }
        EstadoCliente[] opciones = EstadoCliente.values();
        String[] etiquetas = new String[opciones.length];
        int actual = 0;
        for (int i = 0; i < opciones.length; i++) {
            etiquetas[i] = opciones[i] == cliente.getEstado()
                    ? opciones[i].getEtiqueta() + "   <-- estado actual"
                    : opciones[i].getEtiqueta();
            if (opciones[i] == cliente.getEstado()) {
                actual = i;
            }
        }
        javax.swing.JComboBox<String> combo = new javax.swing.JComboBox<>(etiquetas);
        combo.setSelectedIndex(actual);
        int opcion = javax.swing.JOptionPane.showConfirmDialog(this, combo,
                "Cambiar estado de " + cliente.getNombre(), javax.swing.JOptionPane.OK_CANCEL_OPTION,
                javax.swing.JOptionPane.QUESTION_MESSAGE);
        if (opcion != javax.swing.JOptionPane.OK_OPTION) {
            return;
        }
        int elegido = combo.getSelectedIndex();
        if (elegido < 0 || opciones[elegido] == cliente.getEstado()) {
            Dialogos.aviso(this, "El cliente ya tiene ese estado.");
            return;
        }
        if (!Dialogos.confirmar(this, "Cambiar el estado de " + cliente.getNombre()
                + " a " + opciones[elegido].getEtiqueta() + "?")) {
            return;
        }
        try {
            servicio.cambiarEstado(idSeleccionado, opciones[elegido]);
            Dialogos.exito(this, "Estado del cliente actualizado a " + opciones[elegido].getEtiqueta() + ".");
            recargar();
        } catch (RuntimeException e) {
            Dialogos.error(this, e);
        }
    }

    private void verHistorial() {
        if (idSeleccionado == 0) {
            Dialogos.aviso(this, "Seleccione un cliente de la tabla.");
            return;
        }
        try {
            List<MovimientoPuntos> movimientos = servicio.historialDePuntos(idSeleccionado);
            Cliente cliente = servicio.buscarPorId(idSeleccionado);
            StringBuilder texto = new StringBuilder();
            texto.append("Cliente : ").append(cliente.getNombre()).append('\n');
            texto.append("DPI     : ").append(cliente.getDpi()).append('\n');
            texto.append("Saldo   : ").append(Formato.puntos(cliente.getSaldoPuntos())).append('\n');
            texto.append("--------------------------------------------------\n");
            if (movimientos.isEmpty()) {
                texto.append("Sin movimientos registrados.\n");
            }
            for (MovimientoPuntos movimiento : movimientos) {
                texto.append(String.format("%-12s %-12s %+5d  saldo %5d  %s%n",
                        Formato.fecha(movimiento.getFechaMovimiento()),
                        movimiento.getTipo().getEtiqueta(),
                        movimiento.getPuntos(),
                        movimiento.getSaldoResultante(),
                        movimiento.getReferencia() == null ? "" : movimiento.getReferencia()));
            }
            Dialogos.mostrarTexto(this, "Historial de puntos", texto.toString());
        } catch (RuntimeException e) {
            Dialogos.error(this, e);
        }
    }

    private void cargarSeleccion() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return;
        }
        idSeleccionado = (int) tabla.getValueAt(fila, 0);
        txtDpi.setText((String) tabla.getValueAt(fila, 1));
        txtNombre.setText((String) tabla.getValueAt(fila, 2));
        txtTelefono.setText((String) tabla.getValueAt(fila, 3));
        String correo = (String) tabla.getValueAt(fila, 4);
        txtCorreo.setText(correo == null ? "" : correo);
    }

    private void seleccionarPorId(int idCliente) {
        for (int fila = 0; fila < tabla.getRowCount(); fila++) {
            if ((int) tabla.getValueAt(fila, 0) == idCliente) {
                tabla.setRowSelectionInterval(fila, fila);
                tabla.scrollRectToVisible(tabla.getCellRect(fila, 0, true));
                return;
            }
        }
    }

    private void limpiarFormulario() {
        idSeleccionado = 0;
        txtDpi.setText("");
        txtNombre.setText("");
        txtTelefono.setText("");
        txtCorreo.setText("");
        tabla.clearSelection();
    }

    private void recargar() {
        try {
            List<Cliente> clientes = servicio.listar(txtFiltro.getText());
            DefaultTableModel modelo = (DefaultTableModel) tabla.getModel();
            modelo.setRowCount(0);
            for (Cliente cliente : clientes) {
                modelo.addRow(new Object[]{
                        cliente.getIdCliente(),
                        cliente.getDpi(),
                        cliente.getNombre(),
                        cliente.getTelefono() == null ? "" : cliente.getTelefono(),
                        cliente.getEmail() == null ? "" : cliente.getEmail(),
                        cliente.getSaldoPuntos(),
                        cliente.getEstado().getEtiqueta()});
            }
        } catch (RuntimeException e) {
            Dialogos.error(this, e);
        }
    }

    public void refrescar() {
        recargar();
    }

    public static JLabel centered(String texto) {
        JLabel etiqueta = new JLabel(texto, SwingConstants.CENTER);
        return etiqueta;
    }

    static ExcepcionNegocio sinImplementar() {
        return new ExcepcionNegocio("Funcion no disponible");
    }
}
