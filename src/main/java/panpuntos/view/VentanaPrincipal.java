package panpuntos.view;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class VentanaPrincipal extends JFrame implements Navegador {

    public static final String INICIO = "INICIO";
    public static final String CLIENTES = "CLIENTES";
    public static final String PRODUCTOS = "PRODUCTOS";
    public static final String INVENTARIO = "INVENTARIO";
    public static final String PEDIDOS = "PEDIDOS";
    public static final String PAGOS = "PAGOS";
    public static final String FIDELIZACION = "FIDELIZACION";
    public static final String REPORTES = "REPORTES";

    private final CardLayout tarjetas = new CardLayout();
    private final JPanel area = new JPanel(tarjetas);
    private final Map<String, String> titulos = new LinkedHashMap<>();
    private final Map<String, Supplier<Component>> fabricas = new LinkedHashMap<>();
    private final Map<String, Component> instancias = new LinkedHashMap<>();

    private JLabel lblRuta;

    public VentanaPrincipal() {
        super("Pan, Puntos y Premios - Sistema de ventas y fidelizacion");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(1120, 700);
        setMinimumSize(new Dimension(1000, 620));
        setLocationRelativeTo(null);
        registrarModulos();
        construir();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evento) {
                salir();
            }
        });
        mostrarInicio();
    }

    private void registrarModulos() {
        titulos.put(CLIENTES, "Modulo 01 - Administracion de clientes");
        fabricas.put(CLIENTES, ClientesPanel::new);
        titulos.put(PRODUCTOS, "Modulo 02 - Administracion de productos y menus");
        fabricas.put(PRODUCTOS, ProductosPanel::new);
        titulos.put(INVENTARIO, "Modulo 03 - Inventario y abastecimiento");
        fabricas.put(INVENTARIO, InventarioPanel::new);
        titulos.put(PEDIDOS, "Modulos 04 y 05 - Pedidos y cobro");
        fabricas.put(PEDIDOS, PedidoPanel::new);
        titulos.put(PAGOS, "Modulo 05 - Cobro de pedidos");
        fabricas.put(PAGOS, PagosPanel::new);
        titulos.put(FIDELIZACION, "Modulo 06 - Fidelizacion: puntos y canjes");
        fabricas.put(FIDELIZACION, FidelizacionPanel::new);
        titulos.put(REPORTES, "Modulo 07 - Reportes");
        fabricas.put(REPORTES, ReportesPanel::new);
    }

    private void construir() {
        JButton btnInicio = Componentes.boton("Inicio", Componentes.AZUL);
        btnInicio.addActionListener(e -> irAlInicio());

        JButton btnSalir = Componentes.botonPeligro("Salir del sistema");
        btnSalir.addActionListener(e -> salir());

        lblRuta = new JLabel(" ", SwingConstants.CENTER);
        lblRuta.setFont(lblRuta.getFont().deriveFont(13f).deriveFont(java.awt.Font.BOLD));
        lblRuta.setForeground(Componentes.AZUL);

        JPanel barra = new JPanel(new BorderLayout(10, 0));
        JPanel izquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        izquierda.add(btnInicio);
        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        derecha.add(btnSalir);
        barra.add(izquierda, BorderLayout.WEST);
        barra.add(lblRuta, BorderLayout.CENTER);
        barra.add(derecha, BorderLayout.EAST);
        barra.setBackground(Componentes.GRIS_FONDO);
        barra.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Componentes.GRIS_BORDE));

        area.setBackground(Componentes.GRIS_FONDO);

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.add(barra, BorderLayout.NORTH);
        raiz.add(area, BorderLayout.CENTER);
        setLayout(new BorderLayout());
        add(raiz, BorderLayout.CENTER);
        setIconImage(icono());
    }

    @Override
    public void irA(String destino) {
        if (destino == null || INICIO.equals(destino)) {
            irAlInicio();
            return;
        }
        mostrar(destino, true);
    }

    public void irAlInicio() {
        mostrar(INICIO, false);
    }

    private void mostrarInicio() {
        if (!instancias.containsKey(INICIO)) {
            instancias.put(INICIO, new MenuPrincipalPanel(this));
            area.add(instancias.get(INICIO), INICIO);
        }
        mostrar(INICIO, false);
    }

    private void mostrar(String clave, boolean refrescar) {
        if (!instancias.containsKey(clave)) {
            Component panel = fabricas.get(clave).get();
            instancias.put(clave, panel);
            area.add(panel, clave);
        } else if (refrescar) {
            Component panel = instancias.get(clave);
            if (panel instanceof ModuloRefrescable) {
                ((ModuloRefrescable) panel).refrescar();
            }
        }
        tarjetas.show(area, clave);
        actualizarBarra();
    }

    private String actual() {
        for (Component c : area.getComponents()) {
            if (c.isVisible()) {
                for (Map.Entry<String, Component> e : instancias.entrySet()) {
                    if (e.getValue() == c) {
                        return e.getKey();
                    }
                }
            }
        }
        return INICIO;
    }

    private void actualizarBarra() {
        String clave = actual();
        lblRuta.setText(INICIO.equals(clave)
                ? "Menu principal"
                : "Inicio  /  " + titulos.getOrDefault(clave, clave));
    }

    private void salir() {
        if (Dialogos.confirmar(this, "Desea salir del sistema?")) {
            dispose();
            System.exit(0);
        }
    }

    private Image icono() {
        java.net.URL recurso = getClass().getResource("/icono.png");
        return recurso == null ? null : Toolkit.getDefaultToolkit().getImage(recurso);
    }
}
