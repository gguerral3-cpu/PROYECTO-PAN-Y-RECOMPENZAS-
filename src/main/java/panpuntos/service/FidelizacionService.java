package panpuntos.service;

import panpuntos.dao.CanjeDAO;
import panpuntos.dao.ClienteDAO;
import panpuntos.dao.MovimientoPuntosDAO;
import panpuntos.dao.ProductoDAO;
import panpuntos.model.Canje;
import panpuntos.model.Cliente;
import panpuntos.model.Recompensa;
import panpuntos.model.TipoItem;
import panpuntos.util.ExcepcionNegocio;
import panpuntos.util.Transaccion;

import java.util.Date;
import java.util.List;

public class FidelizacionService {

    public Canje canjear(int idCliente, int idRecompensa) {
        return Transaccion.ejecutar(conexion -> {
            Cliente cliente = new ClienteDAO(conexion).buscarPorId(idCliente);
            if (cliente == null) {
                throw new ExcepcionNegocio("El cliente indicado no existe.");
            }
            if (!cliente.puedeOperar()) {
                throw new ExcepcionNegocio("El cliente " + cliente.getNombre() + " esta inactivo y no puede canjear.");
            }

            CanjeDAO canjes = new CanjeDAO(conexion);
            Recompensa recompensa = canjes.buscarRecompensa(idRecompensa);
            if (recompensa == null) {
                throw new ExcepcionNegocio("La recompensa seleccionada no existe.");
            }
            if (!recompensa.getEstado().estaActivo()) {
                throw new ExcepcionNegocio("La recompensa " + recompensa.getNombre() + " no esta disponible.");
            }
            if (cliente.getSaldoPuntos() < recompensa.getPuntosNecesarios()) {
                throw new ExcepcionNegocio("Puntos insuficientes. El cliente tiene " + cliente.getSaldoPuntos()
                        + " y la recompensa requiere " + recompensa.getPuntosNecesarios() + ".");
            }

            int idCanje = canjes.registrar(idCliente, idRecompensa, recompensa.getPuntosNecesarios());
            String referencia = "CANJE-" + idCanje;

            new MovimientoPuntosDAO(conexion).descontar(idCliente, recompensa.getPuntosNecesarios(), referencia);

            panpuntos.dao.ProductoDAO productos = new ProductoDAO(conexion);
            if (recompensa.getTipoItem() == TipoItem.PRODUCTO) {
                productos.descontarProducto(recompensa.getIdProducto(), 1, "CANJE", referencia);
            } else {
                productos.descontarMenu(recompensa.getIdMenu(), 1, "CANJE", referencia);
            }

            Cliente actualizado = new ClienteDAO(conexion).buscarPorId(idCliente);
            return new Canje(idCanje, actualizado.getDpi(), actualizado.getNombre(),
                    recompensa.getCodigo(), recompensa.getNombre(), recompensa.getPuntosNecesarios(),
                    new Date());
        });
    }

    public List<Recompensa> listarRecompensas(String filtro) {
        return Transaccion.ejecutar(conexion -> new CanjeDAO(conexion).listarRecompensas(filtro));
    }

    public List<Canje> listarCanjes() {
        return Transaccion.ejecutar(conexion -> new CanjeDAO(conexion).listarHistorial());
    }

    public Cliente consultarCliente(int idCliente) {
        return Transaccion.ejecutar(conexion -> new ClienteDAO(conexion).buscarPorId(idCliente));
    }
}
