package panpuntos.service;

import panpuntos.dao.MenuDAO;
import panpuntos.dao.ProductoDAO;
import panpuntos.model.Categoria;
import panpuntos.model.ComponenteMenu;
import panpuntos.model.EstadoRegistro;
import panpuntos.model.Menu;
import panpuntos.model.Producto;
import panpuntos.util.ExcepcionNegocio;
import panpuntos.util.Transaccion;

import java.util.List;

public class CatalogoService {

    public Producto registrarProducto(String codigo, String nombre, Categoria categoria, double precio, int existencia) {
        Producto producto = new Producto(0, codigo, nombre, categoria, precio, existencia, EstadoRegistro.ACTIVO);
        return Transaccion.ejecutar(conexion -> {
            ProductoDAO dao = new ProductoDAO(conexion);
            if (dao.buscarPorCodigo(codigo) != null) {
                throw new ExcepcionNegocio("Ya existe un producto con el codigo " + codigo + ".");
            }
            if (existencia > 0) {
                dao.insertarConStock(producto, existencia);
            } else {
                dao.insertar(producto);
            }
            return producto;
        });
    }

    public Producto actualizarProducto(Producto producto) {
        return Transaccion.ejecutar(conexion -> {
            ProductoDAO dao = new ProductoDAO(conexion);
            Producto actual = dao.buscarPorId(producto.getIdProducto());
            if (actual == null) {
                throw new ExcepcionNegocio("El producto seleccionado ya no existe.");
            }
            dao.actualizar(producto);
            return producto;
        });
    }

    public Producto buscarProductoPorId(int idProducto) {
        return Transaccion.ejecutar(conexion -> new ProductoDAO(conexion).buscarPorId(idProducto));
    }

    public List<Producto> listarProductos(String categoria, String filtro) {
        return Transaccion.ejecutar(conexion -> new ProductoDAO(conexion).listar(categoria, filtro));
    }

    public void cambiarEstadoProducto(int idProducto, EstadoRegistro estado) {
        Transaccion.ejecutarVoid(conexion -> new ProductoDAO(conexion).cambiarEstado(idProducto, estado));
    }

    public Menu registrarMenu(String codigo, String nombre, double precio, List<ComponenteMenu> componentes) {
        Menu menu = new Menu(0, codigo, nombre, precio, EstadoRegistro.ACTIVO);
        componentes.forEach(menu::agregarComponente);
        validarComposicion(menu);
        return Transaccion.ejecutar(conexion -> {
            MenuDAO dao = new MenuDAO(conexion);
            if (dao.buscarPorCodigo(codigo) != null) {
                throw new ExcepcionNegocio("Ya existe un menu con el codigo " + codigo + ".");
            }
            ProductoDAO productos = new ProductoDAO(conexion);
            for (ComponenteMenu componente : menu.getComponentes()) {
                Producto producto = productos.buscarPorId(componente.getIdProducto());
                if (producto == null) {
                    throw new ExcepcionNegocio("Uno de los componentes del menu no existe en el catalogo.");
                }
                componente.setCostoUnitario(producto.getPrecioUnitario());
            }
            dao.insertar(menu);
            return menu;
        });
    }

    public Menu actualizarMenu(Menu menu) {
        validarComposicion(menu);
        return Transaccion.ejecutar(conexion -> {
            MenuDAO dao = new MenuDAO(conexion);
            if (dao.buscarPorId(menu.getIdMenu()) == null) {
                throw new ExcepcionNegocio("El menu seleccionado ya no existe.");
            }
            ProductoDAO productos = new ProductoDAO(conexion);
            for (ComponenteMenu componente : menu.getComponentes()) {
                Producto producto = productos.buscarPorId(componente.getIdProducto());
                if (producto == null) {
                    throw new ExcepcionNegocio("Uno de los componentes del menu no existe en el catalogo.");
                }
                componente.setCostoUnitario(producto.getPrecioUnitario());
            }
            dao.actualizar(menu);
            return menu;
        });
    }

    public Menu buscarMenuPorId(int idMenu) {
        return Transaccion.ejecutar(conexion -> new MenuDAO(conexion).buscarPorId(idMenu));
    }

    public List<Menu> listarMenus(String filtro) {
        return Transaccion.ejecutar(conexion -> new MenuDAO(conexion).listar(filtro));
    }

    public void cambiarEstadoMenu(int idMenu, EstadoRegistro estado) {
        Transaccion.ejecutarVoid(conexion -> new MenuDAO(conexion).cambiarEstado(idMenu, estado));
    }

    private void validarComposicion(Menu menu) {
        if (menu.getComponentes().size() < 2) {
            throw new ExcepcionNegocio("Un menu completo debe tener al menos un sándwich y un acompanamiento.");
        }
        boolean tieneSandwich = menu.getComponentes().stream()
                .anyMatch(c -> esCategoria(c.getIdProducto(), Categoria.SANDWICH));
        boolean tieneBebida = menu.getComponentes().stream()
                .anyMatch(c -> esCategoria(c.getIdProducto(), Categoria.BEBIDA));
        boolean tieneAcompanamiento = menu.getComponentes().stream()
                .anyMatch(c -> esCategoria(c.getIdProducto(), Categoria.ACOMPANAMIENTO));
        if (!tieneSandwich || !tieneBebida || !tieneAcompanamiento) {
            throw new ExcepcionNegocio(
                    "Un menu completo debe incluir un sándwich, una bebida y un acompanamiento.");
        }
    }

    private boolean esCategoria(int idProducto, Categoria categoria) {
        return Transaccion.ejecutar(conexion -> {
            Producto producto = new ProductoDAO(conexion).buscarPorId(idProducto);
            return producto != null && producto.getCategoria() == categoria;
        });
    }
}
