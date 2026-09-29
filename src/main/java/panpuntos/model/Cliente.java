package panpuntos.model;

import panpuntos.util.ExcepcionNegocio;
import panpuntos.util.Validaciones;

import java.util.Date;

public class Cliente {

    private int idCliente;
    private String dpi;
    private String nombre;
    private String telefono;
    private String email;
    private int saldoPuntos;
    private EstadoCliente estado;
    private Date fechaRegistro;

    public Cliente(int idCliente, String dpi, String nombre, String telefono, String email,
                   int saldoPuntos, EstadoCliente estado, Date fechaRegistro) {
        this.idCliente = idCliente;
        this.dpi = Validaciones.textoRequerido(dpi, "DPI");
        this.nombre = Validaciones.textoRequerido(nombre, "nombre");
        this.telefono = Validaciones.textoOpcional(telefono);
        this.email = Validaciones.correoValido(email);
        setSaldoPuntos(saldoPuntos);
        this.estado = estado == null ? EstadoCliente.ACTIVO : estado;
        this.fechaRegistro = fechaRegistro == null ? new Date() : fechaRegistro;
    }

    public boolean puedeOperar() {
        return estado.estaActivo();
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public String getDpi() {
        return dpi;
    }

    public void setDpi(String dpi) {
        this.dpi = Validaciones.dpiRequerido(dpi);
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = Validaciones.textoRequerido(nombre, "nombre");
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = Validaciones.textoOpcional(telefono);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = Validaciones.correoValido(email);
    }

    public int getSaldoPuntos() {
        return saldoPuntos;
    }

    public void setSaldoPuntos(int saldoPuntos) {
        if (saldoPuntos < 0) {
            throw new ExcepcionNegocio("El saldo de puntos no puede ser negativo.");
        }
        this.saldoPuntos = saldoPuntos;
    }

    public EstadoCliente getEstado() {
        return estado;
    }

    public void setEstado(EstadoCliente estado) {
        this.estado = estado;
    }

    public void activar() {
        this.estado = EstadoCliente.ACTIVO;
    }

    public void desactivar() {
        this.estado = EstadoCliente.INACTIVO;
    }

    public void suspender() {
        this.estado = EstadoCliente.SUSPENDIDO;
    }

    public Date getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Date fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}
