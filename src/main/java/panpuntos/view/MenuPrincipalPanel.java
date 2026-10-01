package panpuntos.view;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.LinkedHashMap;
import java.util.Map;

public class MenuPrincipalPanel extends JPanel implements ModuloRefrescable {

    private final Navegador navegador;

    public MenuPrincipalPanel(Navegador navegador) {
        this.navegador = navegador;
        construir();
    }

    private void construir() {
        Map<String, String> modulos = new LinkedHashMap<>();
        modulos.put(VentanaPrincipal.CLIENTES, "01  Administracion de clientes");
        modulos.put(VentanaPrincipal.PRODUCTOS, "02  Administracion de productos");
        modulos.put(VentanaPrincipal.INVENTARIO, "03  Inventario y abastecimiento");
        modulos.put(VentanaPrincipal.PEDIDOS, "04  Gestion de pedidos");
        modulos.put(VentanaPrincipal.PAGOS, "05  Cobro de pedidos");
        modulos.put(VentanaPrincipal.FIDELIZACION, "06  Fidelizacion y canjes");
        modulos.put(VentanaPrincipal.REPORTES, "07  Reportes");

        JPanel panel = new JPanel(new GridLayout(4, 2, 14, 14));
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));
        java.awt.Color[] colores = {
                Componentes.AZUL, Componentes.AZUL_CLARO, Componentes.VERDE,
                Componentes.AZUL, Componentes.AZUL_CLARO, Componentes.VERDE, Componentes.AZUL};
        int indice = 0;
        for (Map.Entry<String, String> modulo : modulos.entrySet()) {
            final String destino = modulo.getKey();
            JButton boton = Componentes.boton(modulo.getValue(), colores[indice % colores.length]);
            boton.setFont(boton.getFont().deriveFont(14f));
            boton.setPreferredSize(new Dimension(360, 70));
            boton.addActionListener(e -> navegador.irA(destino));
            panel.add(boton);
            indice++;
        }

        JPanel cabecera = new JPanel(new BorderLayout());
        JLabel titulo = new JLabel("PAN, PUNTOS Y PREMIOS", SwingConstants.CENTER);
        titulo.setFont(titulo.getFont().deriveFont(26f).deriveFont(java.awt.Font.BOLD));
        titulo.setForeground(Componentes.AZUL);
        cabecera.add(titulo, BorderLayout.CENTER);
        JLabel subtitulo = new JLabel("Seleccione el modulo que desea utilizar", SwingConstants.CENTER);
        subtitulo.setForeground(Componentes.GRIS_BORDE.darker());
        cabecera.add(subtitulo, BorderLayout.SOUTH);
        cabecera.setBorder(javax.swing.BorderFactory.createEmptyBorder(24, 10, 10, 10));

        JPanel pie = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        JLabel aviso = new JLabel("Use el boton Inicio de la barra superior para volver al menu");
        aviso.setForeground(Componentes.GRIS_BORDE.darker());
        pie.add(aviso);
        pie.setBorder(new EmptyBorder(0, 0, 18, 0));

        JPanel contenido = new JPanel(new BorderLayout());
        contenido.add(cabecera, BorderLayout.NORTH);
        contenido.add(panel, BorderLayout.CENTER);
        contenido.add(pie, BorderLayout.SOUTH);

        setLayout(new BorderLayout());
        setBackground(Componentes.GRIS_FONDO);
        add(contenido, BorderLayout.CENTER);
    }
}
