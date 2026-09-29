package panpuntos.model;

import java.util.Date;

public class Canje {

    private int idCanje;
    private String dpi;
    private String nombreCliente;
    private String codigoRecompensa;
    private String nombreRecompensa;
    private int puntosUsados;
    private Date fechaCanje;

    public Canje(int idCanje, String dpi, String nombreCliente, String codigoRecompensa,
                 String nombreRecompensa, int puntosUsados, Date fechaCanje) {
        this.idCanje = idCanje;
        this.dpi = dpi;
        this.nombreCliente = nombreCliente;
        this.codigoRecompensa = codigoRecompensa;
        this.nombreRecompensa = nombreRecompensa;
        this.puntosUsados = puntosUsados;
        this.fechaCanje = fechaCanje;
    }

    public int getIdCanje() {
        return idCanje;
    }

    public String getDpi() {
        return dpi;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public String getCodigoRecompensa() {
        return codigoRecompensa;
    }

    public String getNombreRecompensa() {
        return nombreRecompensa;
    }

    public int getPuntosUsados() {
        return puntosUsados;
    }

    public Date getFechaCanje() {
        return fechaCanje;
    }
}
