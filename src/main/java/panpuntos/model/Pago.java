package panpuntos.model;

import panpuntos.util.ExcepcionNegocio;
import panpuntos.util.Validaciones;

import java.util.Date;

public class Pago {

    private int idPago;
    private int idPedido;
    private TipoPago tipoPago;
    private double monto;
    private Double efectivoRecibido;
    private double cambio;
    private String referencia;
    private EstadoPago estado;
    private Date fechaPago;

    private Pago(TipoPago tipoPago, double monto, EstadoPago estado) {
        this.tipoPago = tipoPago;
        this.monto = Validaciones.redondear(monto);
        this.estado = estado;
        this.fechaPago = new Date();
    }

    public static Pago enEfectivo(double monto, double efectivoRecibido) {
        if (efectivoRecibido < 0) {
            throw new ExcepcionNegocio("El efectivo recibido no puede ser negativo.");
        }
        Pago pago = new Pago(TipoPago.EFECTIVO, monto, EstadoPago.APROBADO);
        pago.efectivoRecibido = Validaciones.redondear(efectivoRecibido);
        pago.cambio = Validaciones.redondear(efectivoRecibido - monto);
        if (pago.cambio < 0) {
            throw new ExcepcionNegocio("El efectivo recibido es insuficiente para cubrir el total del pedido.");
        }
        return pago;
    }

    public static Pago conTarjeta(double monto, String referencia) {
        Pago pago = new Pago(TipoPago.TARJETA, monto, EstadoPago.APROBADO);
        pago.referencia = Validaciones.textoRequerido(referencia, "referencia de la tarjeta");
        return pago;
    }

    public static Pago rechazadoTarjeta(double monto, String motivo) {
        Pago pago = new Pago(TipoPago.TARJETA, monto, EstadoPago.RECHAZADO);
        pago.referencia = Validaciones.textoOpcional(motivo);
        return pago;
    }

    public int getIdPago() {
        return idPago;
    }

    public void setIdPago(int idPago) {
        this.idPago = idPago;
    }

    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public TipoPago getTipoPago() {
        return tipoPago;
    }

    public double getMonto() {
        return monto;
    }

    public Double getEfectivoRecibido() {
        return efectivoRecibido;
    }

    public double getCambio() {
        return cambio;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public EstadoPago getEstado() {
        return estado;
    }

    public void setEstado(EstadoPago estado) {
        this.estado = estado;
    }

    public Date getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(Date fechaPago) {
        this.fechaPago = fechaPago;
    }
}
