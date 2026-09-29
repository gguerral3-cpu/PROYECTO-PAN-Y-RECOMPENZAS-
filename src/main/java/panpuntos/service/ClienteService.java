package panpuntos.service;

import panpuntos.dao.ClienteDAO;
import panpuntos.model.Cliente;
import panpuntos.model.EstadoCliente;
import panpuntos.model.MovimientoPuntos;
import panpuntos.util.ExcepcionNegocio;
import panpuntos.util.Transaccion;

import java.util.List;

public class ClienteService {

    public Cliente registrar(String dpi, String nombre, String telefono, String email) {
        Cliente cliente = new Cliente(0, dpi, nombre, telefono, email, 0, EstadoCliente.ACTIVO, null);
        return Transaccion.ejecutar(conexion -> {
            ClienteDAO dao = new ClienteDAO(conexion);
            if (dao.existeOtroConDpi(cliente.getDpi(), 0)) {
                throw new ExcepcionNegocio("Ya existe un cliente registrado con el DPI " + cliente.getDpi() + ".");
            }
            dao.insertar(cliente);
            return cliente;
        });
    }

    public Cliente actualizar(Cliente cliente) {
        return Transaccion.ejecutar(conexion -> {
            ClienteDAO dao = new ClienteDAO(conexion);
            Cliente actual = dao.buscarPorId(cliente.getIdCliente());
            if (actual == null) {
                throw new ExcepcionNegocio("El cliente seleccionado ya no existe.");
            }
            if (dao.existeOtroConDpi(cliente.getDpi(), cliente.getIdCliente())) {
                throw new ExcepcionNegocio("El DPI " + cliente.getDpi() + " ya pertenece a otro cliente.");
            }
            dao.actualizar(cliente);
            return cliente;
        });
    }

    public Cliente buscarPorDpi(String dpi) {
        return Transaccion.ejecutar(conexion -> new ClienteDAO(conexion).buscarPorDpi(dpi));
    }

    public Cliente buscarPorId(int idCliente) {
        return Transaccion.ejecutar(conexion -> new ClienteDAO(conexion).buscarPorId(idCliente));
    }

    public List<Cliente> listar(String filtro) {
        return Transaccion.ejecutar(conexion -> new ClienteDAO(conexion).listar(filtro));
    }

    public void desactivar(int idCliente) {
        Transaccion.ejecutarVoid(conexion -> {
            ClienteDAO dao = new ClienteDAO(conexion);
            if (dao.buscarPorId(idCliente) == null) {
                throw new ExcepcionNegocio("El cliente seleccionado ya no existe.");
            }
            dao.cambiarEstado(idCliente, EstadoCliente.INACTIVO);
        });
    }

    public void activar(int idCliente) {
        Transaccion.ejecutarVoid(conexion ->
                new ClienteDAO(conexion).cambiarEstado(idCliente, EstadoCliente.ACTIVO));
    }

    public void suspender(int idCliente) {
        Transaccion.ejecutarVoid(conexion ->
                new ClienteDAO(conexion).cambiarEstado(idCliente, EstadoCliente.SUSPENDIDO));
    }

    public void cambiarEstado(int idCliente, EstadoCliente estado) {
        if (estado == null) {
            throw new ExcepcionNegocio("Debe seleccionar un estado valido.");
        }
        Transaccion.ejecutarVoid(conexion -> {
            ClienteDAO dao = new ClienteDAO(conexion);
            Cliente cliente = dao.buscarPorId(idCliente);
            if (cliente == null) {
                throw new ExcepcionNegocio("El cliente seleccionado ya no existe.");
            }
            if (cliente.getSaldoPuntos() > 0 && estado != EstadoCliente.ACTIVO) {
                throw new ExcepcionNegocio(
                        "No se puede pasar a " + estado.getEtiqueta()
                                + " un cliente con saldo de puntos pendiente.");
            }
            dao.cambiarEstado(idCliente, estado);
        });
    }

    public List<MovimientoPuntos> historialDePuntos(int idCliente) {
        return Transaccion.ejecutar(conexion -> {
            ClienteDAO clientes = new ClienteDAO(conexion);
            if (clientes.buscarPorId(idCliente) == null) {
                throw new ExcepcionNegocio("El cliente seleccionado ya no existe.");
            }
            return new panpuntos.dao.MovimientoPuntosDAO(conexion).listarPorCliente(idCliente);
        });
    }

    public void ajustarPuntos(int idCliente, int delta, String motivo) {
        Transaccion.ejecutarVoid(conexion ->
                new panpuntos.dao.MovimientoPuntosDAO(conexion).ajustar(idCliente, delta, motivo));
    }

    public int cantidadDeClientes() {
        return Transaccion.ejecutar(conexion -> new ClienteDAO(conexion).listar(null).size());
    }
}
