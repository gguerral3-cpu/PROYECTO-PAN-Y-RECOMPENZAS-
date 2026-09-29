package panpuntos.model;

import panpuntos.util.ExcepcionNegocio;

public class ComponenteMenu {

    private final int idProducto;
    private final String nombreProducto;
    private int cantidad;
    private double costoUnitario;

    public ComponenteMenu(int idProducto, String nombreProducto, int cantidad) {
        this(idProducto, nombreProducto, cantidad, 0);
    }

    public ComponenteMenu(int idProducto, String nombreProducto, int cantidad, double costoUnitario) {
        if (cantidad <= 0) {
            throw new ExcepcionNegocio("La cantidad del componente " + nombreProducto + " debe ser mayor que cero.");
        }
        this.idProducto = idProducto;
        this.nombreProducto = nombreProducto;
        this.cantidad = cantidad;
        this.costoUnitario = costoUnitario;
    }

    public double getCostoTotal() {
        return cantidad * costoUnitario;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        if (cantidad <= 0) {
            throw new ExcepcionNegocio("La cantidad del componente " + nombreProducto + " debe ser mayor que cero.");
        }
        this.cantidad = cantidad;
    }

    public double getCostoUnitario() {
        return costoUnitario;
    }

    public void setCostoUnitario(double costoUnitario) {
        this.costoUnitario = costoUnitario;
    }
}
