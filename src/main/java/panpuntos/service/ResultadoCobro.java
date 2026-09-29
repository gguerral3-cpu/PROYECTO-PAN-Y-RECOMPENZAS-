package panpuntos.service;

import panpuntos.model.Pago;
import panpuntos.model.Pedido;

public record ResultadoCobro(boolean aprobado, Pago pago, Pedido pedido, int puntosAcreditados) {

    public static ResultadoCobro exitoso(Pago pago, Pedido pedido) {
        return new ResultadoCobro(true, pago, pedido, pedido.getPuntosGenerados());
    }

    public static ResultadoCobro rechazado(Pago intento, Pedido pedido) {
        return new ResultadoCobro(false, intento, pedido, 0);
    }
}
