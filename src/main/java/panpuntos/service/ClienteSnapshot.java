package panpuntos.service;

import panpuntos.model.Cliente;
import panpuntos.model.EstadoCliente;

public record ClienteSnapshot(int idCliente, String dpi, String nombre, int saldoPuntos, String estado) {

    public static ClienteSnapshot de(Cliente cliente) {
        return new ClienteSnapshot(
                cliente.getIdCliente(),
                cliente.getDpi(),
                cliente.getNombre(),
                cliente.getSaldoPuntos(),
                cliente.getEstado().name());
    }

    public Cliente aCliente() {
        return new Cliente(idCliente, dpi, nombre, null, null, saldoPuntos,
                EstadoCliente.desdeTexto(estado), null);
    }
}
