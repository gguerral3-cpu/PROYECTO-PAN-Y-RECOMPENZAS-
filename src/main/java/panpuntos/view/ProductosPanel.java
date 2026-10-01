package panpuntos.view;

import panpuntos.model.Categoria;
import panpuntos.model.ComponenteMenu;
import panpuntos.model.EstadoRegistro;
import panpuntos.model.Menu;
import panpuntos.model.Producto;
import panpuntos.service.CatalogoService;
import panpuntos.util.ExcepcionNegocio;
import panpuntos.util.Formato;
import panpuntos.util.Validaciones;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
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

public class ProductosPanel extends JPanel {

    private static final String[] COLUMNAS_PRODUCTO =
            {"Codigo", "Nombre", "Categoria", "Precio", "Existencia", "Estado"};
    private static final String[] COLUMNAS_MENU =
            {"Codigo", "Nombre", "Precio", "Componentes", "Costo componentes", "Estado"};

    private final CatalogoService servicio = new CatalogoService();

    private final JTextField txtCodigo = new JTextField(14);
    private final JTextField txtNombreProducto = new JTextField(14);
    private final JComboBox<String> cmbCategoria = new JComboBox<>();
    private final JTextField txtPrecio = new JTextField(8);
    private final JTextField txtExistencia = new JTextField(8);
    private final JTable tablaProductos = Componentes.tabla(COLUMNAS_PRODUCTO);

    private final JTextField txtCodigoMenu = new JTextField(14);
    private final JTextField txtNombreMenu = new JTextField(14);
    private final JTextField txtPrecioMenu = new JTextField(8);
    private final JComboBox<String> cmbComponente = new JComboBox<>();
    private final JTextField txtCantidadComponente = new JTextField(5);
    private final JTable tablaComponentes = Componentes.tabla(new String[]{"Producto", "Cantidad", "Precio"});
    private final JTable tablaMenus = Componentes.tabla(COLUMNAS_MENU);

    private final List<ComponenteMenu> componentes = new ArrayList<>();
    private final List<Object[]> opcionesComponente = new ArrayList<>();
    private Menu menuEnEdicion;

    public ProductosPanel() {
        setLayout(new BorderLayout());
        construir();
        recargarProductos();
        recargarMenus();
        recargarComponentesDisponibles();
    }

    private void construir() {
        for (Categoria categoria : Categoria.values()) {
            cmbCategoria.addItem(categoria.name() + " - " + categoria.getEtiqueta());
        }

        JPanel formProducto = Componentes.panelCampos(5);
        formProducto.add(Componentes.etiqueta("Codigo"));
        formProducto.add(txtCodigo);
        formProducto.add(Componentes.etiqueta("Nombre"));
        formProducto.add(txtNombreProducto);
        formProducto.add(Componentes.etiqueta("Categoria"));
        formProducto.add(cmbCategoria);
        formProducto.add(Componentes.etiqueta("Precio unitario"));
        formProducto.add(txtPrecio);
        formProducto.add(Componentes.etiqueta("Existencia inicial"));
        formProducto.add(txtExistencia);
        formProducto.setBorder(BorderFactory.createTitledBorder("Registro de producto"));

        JButton btnGuardarProducto = Componentes.boton("Guardar producto", Componentes.VERDE);
        JButton btnActualizarProducto = Componentes.boton("Actualizar", Componentes.AZUL_CLARO);
        JButton btnNuevoProducto = Componentes.botonSecundario("Nuevo");
        JButton btnEstadoProducto = Componentes.botonPeligro("Activar / Desactivar");
        btnGuardarProducto.addActionListener(e -> guardarProducto());
        btnActualizarProducto.addActionListener(e -> actualizarProducto());
        btnNuevoProducto.addActionListener(e -> limpiarProducto());
        btnEstadoProducto.addActionListener(e -> cambiarEstadoProducto());

        JPanel botonesProducto = new JPanel(new GridLayout(1, 4, 8, 0));
        botonesProducto.add(btnGuardarProducto);
        botonesProducto.add(btnActualizarProducto);
        botonesProducto.add(btnNuevoProducto);
        botonesProducto.add(btnEstadoProducto);
        botonesProducto.setBorder(new EmptyBorder(0, 12, 10, 12));

        JPanel izquierda = new JPanel(new BorderLayout(0, 8));
        izquierda.add(formProducto, BorderLayout.NORTH);
        izquierda.add(botonesProducto, BorderLayout.CENTER);
        izquierda.setBorder(new EmptyBorder(10, 10, 10, 5));

        tablaProductos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaProductos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarProducto();
            }
        });

        JPanel derecha = new JPanel(new BorderLayout());
        derecha.add(Componentes.titulo("Catalogo de productos"), BorderLayout.NORTH);
        derecha.add(Componentes.panelDesplazable(tablaProductos), BorderLayout.CENTER);
        derecha.setBorder(new EmptyBorder(10, 5, 10, 10));

        JPanel panelProductos = new JPanel(new java.awt.GridLayout(1, 2));
        panelProductos.add(izquierda);
        panelProductos.add(derecha);

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Productos", panelProductos);
        pestanas.addTab("Menus completos", construirPanelMenus());

        setLayout(new BorderLayout());
        add(Componentes.titulo("Administracion de productos"), BorderLayout.NORTH);
        add(pestanas, BorderLayout.CENTER);
        setMinimumSize(new Dimension(960, 600));
    }

    private JPanel construirPanelMenus() {
        JPanel form = Componentes.panelCampos(3);
        form.add(Componentes.etiqueta("Codigo del menu"));
        form.add(txtCodigoMenu);
        form.add(Componentes.etiqueta("Nombre del menu"));
        form.add(txtNombreMenu);
        form.add(Componentes.etiqueta("Precio del menu"));
        form.add(txtPrecioMenu);
        form.setBorder(BorderFactory.createTitledBorder("Datos del menu"));

        JPanel panelComponente = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        panelComponente.add(Componentes.etiqueta("Componente:"));
        cmbComponente.setPreferredSize(new Dimension(260, 26));
        panelComponente.add(cmbComponente);
        panelComponente.add(Componentes.etiqueta("Cantidad:"));
        panelComponente.add(txtCantidadComponente);
        JButton btnAgregarComponente = Componentes.boton("Agregar", Componentes.AZUL_CLARO);
        JButton btnQuitarComponente = Componentes.boton("Quitar", Componentes.ROJO);
        btnAgregarComponente.addActionListener(e -> agregarComponente());
        btnQuitarComponente.addActionListener(e -> quitarComponente());
        panelComponente.add(btnAgregarComponente);
        panelComponente.add(btnQuitarComponente);
        panelComponente.setBorder(BorderFactory.createTitledBorder("Composicion del menu"));

        JButton btnGuardarMenu = Componentes.boton("Guardar menu", Componentes.VERDE);
        JButton btnNuevoMenu = Componentes.botonSecundario("Nuevo menu");
        JButton btnEstadoMenu = Componentes.botonPeligro("Activar / Desactivar");
        btnGuardarMenu.addActionListener(e -> guardarMenu());
        btnNuevoMenu.addActionListener(e -> limpiarMenu());
        btnEstadoMenu.addActionListener(e -> cambiarEstadoMenu());
        JPanel botones = new JPanel(new GridLayout(1, 3, 8, 0));
        botones.add(btnGuardarMenu);
        botones.add(btnNuevoMenu);
        botones.add(btnEstadoMenu);
        botones.setBorder(new EmptyBorder(8, 10, 10, 10));

        JPanel izquierda = new JPanel(new BorderLayout(0, 6));
        izquierda.add(form, BorderLayout.NORTH);
        izquierda.add(panelComponente, BorderLayout.CENTER);
        izquierda.add(Componentes.panelDesplazable(tablaComponentes), BorderLayout.CENTER);
        izquierda.add(botones, BorderLayout.SOUTH);
        izquierda.setBorder(new EmptyBorder(10, 10, 10, 5));

        tablaMenus.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaMenus.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarMenu();
            }
        });

        JPanel derecha = new JPanel(new BorderLayout());
        derecha.add(Componentes.titulo("Catalogos de menus"), BorderLayout.NORTH);
        derecha.add(Componentes.panelDesplazable(tablaMenus), BorderLayout.CENTER);
        derecha.setBorder(new EmptyBorder(10, 5, 10, 10));

        JPanel panel = new JPanel(new java.awt.GridLayout(1, 2));
        panel.add(izquierda);
        panel.add(derecha);
        return panel;
    }

    private void guardarProducto() {
        try {
            Categoria categoria = categoriaSeleccionada();
            Producto producto = servicio.registrarProducto(
                    Validaciones.textoRequerido(txtCodigo.getText(), "codigo"),
                    Validaciones.textoRequerido(txtNombreProducto.getText(), "nombre"),
                    categoria,
                    Validaciones.decimalNoNegativo(txtPrecio.getText(), "precio"),
                    (int) Validaciones.redondear(Validaciones.decimalNoNegativo(
                            txtExistencia.getText() == null || txtExistencia.getText().isBlank()
                                    ? "0" : txtExistencia.getText(), "existencia")));
            Dialogos.exito(this, "Producto registrado con el codigo " + producto.getCodigo() + ".");
            limpiarProducto();
            recargarProductos();
            recargarComponentesDisponibles();
        } catch (RuntimeException e) {
            Dialogos.error(this, e);
        }
    }

    private void actualizarProducto() {
        int fila = tablaProductos.getSelectedRow();
        if (fila < 0) {
            Dialogos.aviso(this, "Seleccione un producto de la tabla.");
            return;
        }
        try {
            Producto producto = servicio.listarProductos(null, null).stream()
                    .filter(p -> p.getCodigo().equals(tablaProductos.getValueAt(fila, 0)))
                    .findFirst()
                    .orElseThrow(() -> new ExcepcionNegocio("El producto ya no existe."));
            producto.setNombre(Validaciones.textoRequerido(txtNombreProducto.getText(), "nombre"));
            producto.setCategoria(categoriaSeleccionada());
            producto.setPrecioUnitario(Validaciones.decimalNoNegativo(txtPrecio.getText(), "precio"));
            servicio.actualizarProducto(producto);
            Dialogos.exito(this, "Producto actualizado. Las existencias se modifican desde el modulo de inventario.");
            recargarProductos();
        } catch (RuntimeException e) {
            Dialogos.error(this, e);
        }
    }

    private void cambiarEstadoProducto() {
        int fila = tablaProductos.getSelectedRow();
        if (fila < 0) {
            Dialogos.aviso(this, "Seleccione un producto de la tabla.");
            return;
        }
        try {
            String codigo = (String) tablaProductos.getValueAt(fila, 0);
            Producto producto = servicio.listarProductos(null, null).stream()
                    .filter(p -> p.getCodigo().equals(codigo))
                    .findFirst()
                    .orElseThrow(() -> new ExcepcionNegocio("El producto ya no existe."));
            EstadoRegistro nuevo = producto.getEstado().estaActivo()
                    ? EstadoRegistro.INACTIVO : EstadoRegistro.ACTIVO;
            servicio.cambiarEstadoProducto(producto.getIdProducto(), nuevo);
            Dialogos.exito(this, "Producto " + nuevo.getEtiqueta().toLowerCase() + ".");
            recargarProductos();
        } catch (RuntimeException e) {
            Dialogos.error(this, e);
        }
    }

    private void cargarProducto() {
        int fila = tablaProductos.getSelectedRow();
        if (fila < 0) {
            return;
        }
        txtCodigo.setText((String) tablaProductos.getValueAt(fila, 0));
        txtNombreProducto.setText((String) tablaProductos.getValueAt(fila, 1));
        String categoria = (String) tablaProductos.getValueAt(fila, 2);
        for (int i = 0; i < cmbCategoria.getItemCount(); i++) {
            if (cmbCategoria.getItemAt(i).startsWith(categoria)) {
                cmbCategoria.setSelectedIndex(i);
                break;
            }
        }
        txtPrecio.setText(String.valueOf(tablaProductos.getValueAt(fila, 3)));
        txtExistencia.setText("0");
    }

    private void limpiarProducto() {
        txtCodigo.setText("");
        txtNombreProducto.setText("");
        txtPrecio.setText("");
        txtExistencia.setText("");
        cmbCategoria.setSelectedIndex(0);
        tablaProductos.clearSelection();
    }

    private void agregarComponente() {
        int indice = cmbComponente.getSelectedIndex();
        Object[] entrada = componentesDisponibles(indice);
        if (entrada == null) {
            Dialogos.aviso(this, "Seleccione un producto para el menu.");
            return;
        }
        try {
            Producto producto = (Producto) entrada[1];
            int cantidad = Validaciones.enteroPositivo(txtCantidadComponente.getText(), "cantidad");
            Producto vigente = servicio.buscarProductoPorId(producto.getIdProducto());
            menuEnEdicion = menuEnEdicion == null
                    ? new Menu(0, "", "", 0, EstadoRegistro.ACTIVO) : menuEnEdicion;
            menuEnEdicion.agregarComponente(new ComponenteMenu(
                    vigente.getIdProducto(), vigente.getNombre(), cantidad, vigente.getPrecioUnitario()));
            actualizarTablaComponentes(menuEnEdicion);
            txtCantidadComponente.setText("1");
        } catch (RuntimeException e) {
            Dialogos.error(this, e);
        }
    }

    private void quitarComponente() {
        int fila = tablaComponentes.getSelectedRow();
        if (fila < 0 || menuEnEdicion == null) {
            Dialogos.aviso(this, "Seleccione el componente que desea quitar.");
            return;
        }
        String nombre = (String) tablaComponentes.getValueAt(fila, 0);
        menuEnEdicion.getComponentes().removeIf(c -> c.getNombreProducto().equals(nombre));
        actualizarTablaComponentes(menuEnEdicion);
    }

    private void guardarMenu() {
        try {
            Menu menu = menuEnEdicion == null
                    ? new Menu(0, txtCodigoMenu.getText(), txtNombreMenu.getText(),
                    Validaciones.decimalNoNegativo(txtPrecioMenu.getText(), "precio"), EstadoRegistro.ACTIVO)
                    : menuEnEdicion;
            menu.setCodigo(Validaciones.textoRequerido(txtCodigoMenu.getText(), "codigo"));
            menu.setNombre(Validaciones.textoRequerido(txtNombreMenu.getText(), "nombre"));
            menu.setPrecioMenu(Validaciones.decimalNoNegativo(txtPrecioMenu.getText(), "precio"));
            servicio.registrarMenu(menu.getCodigo(), menu.getNombre(), menu.getPrecioMenu(),
                    new ArrayList<>(menu.getComponentes()));
            Dialogos.exito(this, "Menu registrado correctamente.");
            limpiarMenu();
            recargarMenus();
            recargarComponentesDisponibles();
        } catch (RuntimeException e) {
            Dialogos.error(this, e);
        }
    }

    private void cambiarEstadoMenu() {
        int fila = tablaMenus.getSelectedRow();
        if (fila < 0) {
            Dialogos.aviso(this, "Seleccione un menu de la tabla.");
            return;
        }
        try {
            String codigo = (String) tablaMenus.getValueAt(fila, 0);
            Menu menu = servicio.listarMenus(null).stream()
                    .filter(m -> m.getCodigo().equals(codigo))
                    .findFirst()
                    .orElseThrow(() -> new ExcepcionNegocio("El menu ya no existe."));
            EstadoRegistro nuevo = menu.getEstado().estaActivo()
                    ? EstadoRegistro.INACTIVO : EstadoRegistro.ACTIVO;
            servicio.cambiarEstadoMenu(menu.getIdMenu(), nuevo);
            Dialogos.exito(this, "Menu " + nuevo.getEtiqueta().toLowerCase() + ".");
            recargarMenus();
        } catch (RuntimeException e) {
            Dialogos.error(this, e);
        }
    }

    private void cargarMenu() {
        int fila = tablaMenus.getSelectedRow();
        if (fila < 0) {
            return;
        }
        String codigo = (String) tablaMenus.getValueAt(fila, 0);
        try {
            Menu menu = servicio.listarMenus(null).stream()
                    .filter(m -> m.getCodigo().equals(codigo))
                    .findFirst()
                    .orElseThrow(() -> new ExcepcionNegocio("El menu ya no existe."));
            menuEnEdicion = menu;
            txtCodigoMenu.setText(menu.getCodigo());
            txtNombreMenu.setText(menu.getNombre());
            txtPrecioMenu.setText(String.valueOf(menu.getPrecioMenu()));
            actualizarTablaComponentes(menu);
        } catch (RuntimeException e) {
            Dialogos.error(this, e);
        }
    }

    private void limpiarMenu() {
        menuEnEdicion = null;
        txtCodigoMenu.setText("");
        txtNombreMenu.setText("");
        txtPrecioMenu.setText("");
        ((DefaultTableModel) tablaComponentes.getModel()).setRowCount(0);
        tablaMenus.clearSelection();
    }

    private void actualizarTablaComponentes(Menu menu) {
        DefaultTableModel modelo = (DefaultTableModel) tablaComponentes.getModel();
        modelo.setRowCount(0);
        for (ComponenteMenu componente : menu.getComponentes()) {
            modelo.addRow(new Object[]{componente.getNombreProducto(), componente.getCantidad(),
                    Formato.moneda(componente.getCostoUnitario())});
        }
    }

    private void recargarProductos() {
        try {
            DefaultTableModel modelo = (DefaultTableModel) tablaProductos.getModel();
            modelo.setRowCount(0);
            for (Producto producto : servicio.listarProductos(null, null)) {
                modelo.addRow(new Object[]{
                        producto.getCodigo(),
                        producto.getNombre(),
                        producto.getCategoria().name(),
                        Formato.moneda(producto.getPrecioUnitario()),
                        producto.getExistencia(),
                        producto.getEstado().getEtiqueta()});
            }
        } catch (RuntimeException e) {
            Dialogos.error(this, e);
        }
    }

    private void recargarMenus() {
        try {
            DefaultTableModel modelo = (DefaultTableModel) tablaMenus.getModel();
            modelo.setRowCount(0);
            for (Menu menu : servicio.listarMenus(null)) {
                modelo.addRow(new Object[]{
                        menu.getCodigo(),
                        menu.getNombre(),
                        Formato.moneda(menu.getPrecioMenu()),
                        menu.getDescripcionComponentes(),
                        Formato.moneda(menu.getCostoComponentes()),
                        menu.getEstado().getEtiqueta()});
            }
        } catch (RuntimeException e) {
            Dialogos.error(this, e);
        }
    }

    private void recargarComponentesDisponibles() {
        DefaultComboBoxModel<String> modelo = new DefaultComboBoxModel<>();
        modelo.addElement("Seleccione un producto...");
        opcionesComponente.clear();
        for (Producto producto : servicio.listarProductos(null, null)) {
            if (producto.getEstado().estaActivo()) {
                modelo.addElement(producto.getCodigo() + " - " + producto.getNombre());
                opcionesComponente.add(new Object[]{producto.getCodigo() + " - " + producto.getNombre(), producto});
            }
        }
        cmbComponente.setModel(modelo);
    }

    private Object[] componentesDisponibles(int indice) {
        int real = indice - 1;
        return real < 0 || real >= opcionesComponente.size() ? null : opcionesComponente.get(real);
    }

    private Categoria categoriaSeleccionada() {
        String texto = (String) cmbCategoria.getSelectedItem();
        return Categoria.valueOf(texto.split(" - ")[0]);
    }

    static JLabel etiquetaInformativa(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setForeground(Componentes.AZUL);
        return etiqueta;
    }
}
