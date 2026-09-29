package panpuntos.model;

import java.util.Date;

public class MovimientoPuntos {

    public enum Tipo {
        ACUMULACION("Acumulacion"),
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
    private int idCliente;
    private Tipo tipo;
    private int puntos;
    private int saldoResultante;
    private String referencia;
    private Date fechaMovimiento;

    public MovimientoPuntos(int idMovimiento, int idCliente, Tipo tipo, int puntos,
                            int saldoResultante, String referencia, Date fechaMovimiento) {
        this.idMovimiento = idMovimiento;
        this.idCliente = idCliente;
        this.tipo = tipo;
        this.puntos = puntos;
        this.saldoResultante = saldoResultante;
        this.referencia = referencia;
        this.fechaMovimiento = fechaMovimiento;
    }

    public int getIdMovimiento() {
        return idMovimiento;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public int getPuntos() {
        return puntos;
    }

    public int getSaldoResultante() {
        return saldoResultante;
    }

    public String getReferencia() {
        return referencia;
    }

    public Date getFechaMovimiento() {
        return fechaMovimiento;
    }
}
