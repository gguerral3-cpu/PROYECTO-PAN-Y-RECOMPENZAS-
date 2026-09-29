package panpuntos.model;

import java.util.Map;

public class ProductoIndividual extends ItemVenta {

    private final Producto producto;

    public ProductoIndividual(Producto producto, int cantidad) {
        super(producto.getIdProducto(), producto.getCodigo(), producto.getNombre(),
                cantidad, producto.getPrecioUnitario());
        this.producto = producto;
    }

    public ProductoIndividual(Producto producto, int cantidad, double precioUnitario) {
        super(producto.getIdProducto(), producto.getCodigo(), producto.getNombre(),
                cantidad, precioUnitario);
        this.producto = producto;
    }

    @Override
    public int calcularPuntos() {
        return getCantidad() * producto.getCategoria().getPuntosPorUnidad();
    }

    @Override
    public Map<Integer, Integer> unidadesDeInventario() {
        return Map.of(producto.getIdProducto(), getCantidad());
    }

    @Override
    public String getTipoItem() {
        return "PRODUCTO";
    }

    @Override
    public Integer getIdProducto() {
        return producto.getIdProducto();
    }

    @Override
    public Integer getIdMenu() {
        return null;
    }

    @Override
    public String getDetalleComposicion() {
        return producto.getCategoria().getEtiqueta();
    }

    public Producto getProducto() {
        return producto;
    }

    public Categoria getCategoria() {
        return producto.getCategoria();
    }

    public int getExistenciaDisponible() {
        return producto.getExistencia();
    }
}
