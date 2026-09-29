package panpuntos.model;

import panpuntos.util.ExcepcionNegocio;
import panpuntos.util.Validaciones;

import java.util.ArrayList;
import java.util.List;

public class Menu {

    public static final int PUNTOS_POR_MENU = 8;

    private int idMenu;
    private String codigo;
    private String nombre;
    private double precioMenu;
    private EstadoRegistro estado;
    private final List<ComponenteMenu> componentes = new ArrayList<>();

    public Menu(int idMenu, String codigo, String nombre, double precioMenu, EstadoRegistro estado) {
        this.idMenu = idMenu;
        this.codigo = Validaciones.textoRequerido(codigo, "codigo");
        this.nombre = Validaciones.textoRequerido(nombre, "nombre");
        this.precioMenu = Validaciones.redondear(precioMenu);
        this.estado = estado == null ? EstadoRegistro.ACTIVO : estado;
    }

    public MenuCompleto crearItem(int cantidad) {
        if (!estado.estaActivo()) {
            throw new ExcepcionNegocio("El menu " + nombre + " esta desactivado.");
        }
        if (componentes.isEmpty()) {
            throw new ExcepcionNegocio("El menu " + nombre + " no tiene componentes configurados.");
        }
        return new MenuCompleto(this, cantidad);
    }

    public MenuCompleto reconstruirItem(int cantidad, double precioUnitario) {
        return new MenuCompleto(this, cantidad, precioUnitario);
    }

    public void agregarComponente(ComponenteMenu componente) {
        for (ComponenteMenu existente : componentes) {
            if (existente.getIdProducto() == componente.getIdProducto()) {
                existente.setCantidad(existente.getCantidad() + componente.getCantidad());
                return;
            }
        }
        componentes.add(componente);
    }

    public List<ComponenteMenu> getComponentes() {
        return componentes;
    }

    public String getDescripcionComponentes() {
        StringBuilder texto = new StringBuilder();
        for (ComponenteMenu componente : componentes) {
            if (texto.length() > 0) {
                texto.append(" + ");
            }
            texto.append(componente.getCantidad()).append(" ").append(componente.getNombreProducto());
        }
        return texto.toString();
    }

    public double getCostoComponentes() {
        return componentes.stream()
                .mapToDouble(c -> c.getCantidad() * c.getCostoUnitario())
                .sum();
    }

    public int getIdMenu() {
        return idMenu;
    }

    public void setIdMenu(int idMenu) {
        this.idMenu = idMenu;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = Validaciones.textoRequerido(codigo, "codigo");
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = Validaciones.textoRequerido(nombre, "nombre");
    }

    public double getPrecioMenu() {
        return precioMenu;
    }

    public void setPrecioMenu(double precioMenu) {
        if (precioMenu < 0) {
            throw new ExcepcionNegocio("El precio del menu no puede ser negativo.");
        }
        this.precioMenu = Validaciones.redondear(precioMenu);
    }

    public EstadoRegistro getEstado() {
        return estado;
    }

    public void setEstado(EstadoRegistro estado) {
        this.estado = estado;
    }

    public void activar() {
        this.estado = EstadoRegistro.ACTIVO;
    }

    public void desactivar() {
        this.estado = EstadoRegistro.INACTIVO;
    }
}
