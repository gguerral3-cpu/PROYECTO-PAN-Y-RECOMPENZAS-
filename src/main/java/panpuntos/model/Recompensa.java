package panpuntos.model;

import panpuntos.util.ExcepcionNegocio;

import java.util.Date;

public class Recompensa {

    private int idRecompensa;
    private String codigo;
    private String nombre;
    private TipoItem tipoItem;
    private Integer idProducto;
    private Integer idMenu;
    private int puntosNecesarios;
    private EstadoRegistro estado;

    public Recompensa(int idRecompensa, String codigo, String nombre, TipoItem tipoItem,
                      Integer idProducto, Integer idMenu, int puntosNecesarios, EstadoRegistro estado) {
        if (puntosNecesarios <= 0) {
            throw new ExcepcionNegocio("Los puntos necesarios de la recompensa deben ser mayores que cero.");
        }
        this.idRecompensa = idRecompensa;
        this.codigo = codigo;
        this.nombre = nombre;
        this.tipoItem = tipoItem;
        this.idProducto = idProducto;
        this.idMenu = idMenu;
        this.puntosNecesarios = puntosNecesarios;
        this.estado = estado == null ? EstadoRegistro.ACTIVO : estado;
    }

    public int getIdRecompensa() {
        return idRecompensa;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public TipoItem getTipoItem() {
        return tipoItem;
    }

    public Integer getIdProducto() {
        return idProducto;
    }

    public Integer getIdMenu() {
        return idMenu;
    }

    public int getPuntosNecesarios() {
        return puntosNecesarios;
    }

    public EstadoRegistro getEstado() {
        return estado;
    }
}
