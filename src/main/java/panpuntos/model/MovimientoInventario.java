package panpuntos.model;

import java.util.Date;

public class MovimientoInventario {

    public enum Tipo {
        ABASTECIMIENTO("Abastecimiento"),
        VENTA("Venta"),
        CANJE("Canje"),
        AJUSTE("Ajuste");

        private final String etiqueta;

        Tipo(String etiqueta) {
            this.etiqueta = etiqueta;
        }

        public String getEtiqueta() {
            return etiqueta;
        }
    }

    private int idMovimiento;
    private String codigoProducto;
    private String nombreProducto;
    private Tipo tipo;
    private int cantidad;
    private int existenciaAnterior;
    private int existenciaNueva;
    private String referencia;
    private Date fechaMovimiento;

    public MovimientoInventario(int idMovimiento, String codigoProducto, String nombreProducto,
                                Tipo tipo, int cantidad, int existenciaAnterior, int existenciaNueva,
                                String referencia, Date fechaMovimiento) {
        this.idMovimiento = idMovimiento;
        this.codigoProducto = codigoProducto;
        this.nombreProducto = nombreProducto;
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.existenciaAnterior = existenciaAnterior;
        this.existenciaNueva = existenciaNueva;
        this.referencia = referencia;
        this.fechaMovimiento = fechaMovimiento;
    }

    public int getIdMovimiento() {
        return idMovimiento;
    }

    public String getCodigoProducto() {
        return codigoProducto;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public int getCantidad() {
        return cantidad;
    }

    public int getExistenciaAnterior() {
        return existenciaAnterior;
    }

    public int getExistenciaNueva() {
        return existenciaNueva;
    }

    public String getReferencia() {
        return referencia;
    }

    public Date getFechaMovimiento() {
        return fechaMovimiento;
    }
}
