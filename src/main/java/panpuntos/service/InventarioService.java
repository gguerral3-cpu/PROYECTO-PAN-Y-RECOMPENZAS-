package panpuntos.service;

import panpuntos.dao.InventarioDAO;
import panpuntos.model.MovimientoInventario;
import panpuntos.model.Producto;
import panpuntos.util.Transaccion;

import java.util.List;

public class InventarioService {

    public void abastecer(int idProducto, int cantidad, String referencia) {
        if (cantidad <= 0) {
            throw new panpuntos.util.ExcepcionNegocio("La cantidad de abastecimiento debe ser mayor que cero.");
        }
        Transaccion.ejecutarVoid(conexion ->
                new InventarioDAO(conexion).abastecer(idProducto, cantidad, referencia));
    }

    public List<Producto> listarProductos(String categoria, String filtro) {
        return Transaccion.ejecutar(conexion ->
                new panpuntos.dao.ProductoDAO(conexion).listar(categoria, filtro));
    }

    public List<MovimientoInventario> listarMovimientos(Integer idProducto) {
        return Transaccion.ejecutar(conexion -> new InventarioDAO(conexion).listarMovimientos(idProducto));
    }

    public List<Producto> listarCriticos() {
        return Transaccion.ejecutar(conexion -> new InventarioDAO(conexion).listarCriticos());
    }
}
