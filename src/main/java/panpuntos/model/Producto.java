package panpuntos.model;

import panpuntos.util.ExcepcionNegocio;
import panpuntos.util.Validaciones;

public class Producto {

    private int idProducto;
    private String codigo;
    private String nombre;
    private Categoria categoria;
    private double precioUnitario;
    private int existencia;
    private EstadoRegistro estado;

    public Producto(int idProducto, String codigo, String nombre, Categoria categoria,
                    double precioUnitario, int existencia, EstadoRegistro estado) {
        this.idProducto = idProducto;
        this.codigo = Validaciones.textoRequerido(codigo, "codigo");
        this.nombre = Validaciones.textoRequerido(nombre, "nombre");
        this.categoria = categoria;
        this.precioUnitario = Validaciones.redondear(precioUnitario);
        this.existencia = existencia;
        this.estado = estado == null ? EstadoRegistro.ACTIVO : estado;
        if (existencia < 0) {
            throw new ExcepcionNegocio("La existencia de " + nombre + " no puede ser negativa.");
        }
    }

    public ProductoIndividual crearItem(int cantidad) {
        if (!estado.estaActivo()) {
            throw new ExcepcionNegocio("El producto " + nombre + " esta desactivado.");
        }
        if (cantidad > existencia) {
            throw new ExcepcionNegocio(
                    "Existencias insuficientes de " + nombre + ". Disponible: " + existencia + ".");
        }
        return new ProductoIndividual(this, cantidad);
    }

    public boolean tieneExistencias(int requeridas) {
        return estado.estaActivo() && existencia >= requeridas;
    }

    public ProductoIndividual reconstruirItem(int cantidad, double precioUnitario) {
        return new ProductoIndividual(this, cantidad, precioUnitario);
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
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

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        if (precioUnitario < 0) {
            throw new ExcepcionNegocio("El precio unitario no puede ser negativo.");
        }
        this.precioUnitario = Validaciones.redondear(precioUnitario);
    }

    public int getExistencia() {
        return existencia;
    }

    public void setExistencia(int existencia) {
        if (existencia < 0) {
            throw new ExcepcionNegocio("La existencia no puede ser negativa.");
        }
        this.existencia = existencia;
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
