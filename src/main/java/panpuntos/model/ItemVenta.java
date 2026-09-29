package panpuntos.model;

import panpuntos.util.ExcepcionNegocio;
import panpuntos.util.Validaciones;

import java.util.Map;

public abstract class ItemVenta {

    private final int idItem;
    private final String codigo;
    private final String descripcion;
    private final double precioUnitario;
    private int cantidad;

    protected ItemVenta(int idItem, String codigo, String descripcion, int cantidad, double precioUnitario) {
        if (cantidad <= 0) {
            throw new ExcepcionNegocio("La cantidad de " + descripcion + " debe ser mayor que cero.");
        }
        if (precioUnitario < 0) {
            throw new ExcepcionNegocio("El precio de " + descripcion + " no puede ser negativo.");
        }
        this.idItem = idItem;
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    public abstract int calcularPuntos();

    public abstract Map<Integer, Integer> unidadesDeInventario();

    public abstract String getTipoItem();

    public abstract Integer getIdProducto();

    public abstract Integer getIdMenu();

    public abstract String getDetalleComposicion();

    public final double getSubtotal() {
        return Validaciones.redondear(cantidad * precioUnitario);
    }

    public int getIdItem() {
        return idItem;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        if (cantidad <= 0) {
            throw new ExcepcionNegocio("La cantidad de " + descripcion + " debe ser mayor que cero.");
        }
        this.cantidad = cantidad;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }
}
