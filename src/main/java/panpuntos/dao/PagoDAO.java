package panpuntos.dao;

import panpuntos.model.EstadoPago;
import panpuntos.model.Pago;
import panpuntos.model.TipoPago;
import panpuntos.util.ErroresOracle;
import panpuntos.util.Vincular;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class PagoDAO {

    private static final String SQL_INSERTAR = """
            INSERT INTO PAGO
                (ID_PAGO, ID_PEDIDO, TIPO_PAGO, MONTO, EFECTIVO_RECIBIDO, CAMBIO, REFERENCIA, ESTADO, FECHA_PAGO)
            VALUES (SEQ_PAGO.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, SYSDATE)
            """;

    private static final String SQL_POR_PEDIDO = """
            SELECT ID_PAGO, ID_PEDIDO, TIPO_PAGO, MONTO, EFECTIVO_RECIBIDO, CAMBIO, REFERENCIA, ESTADO
            FROM PAGO WHERE ID_PEDIDO = ? ORDER BY ID_PAGO DESC
            """;

    private final Connection conexion;

    public PagoDAO(Connection conexion) {
        this.conexion = conexion;
    }

    public int registrar(Pago pago, int idPedido) {
        try (PreparedStatement statement = conexion.prepareStatement(SQL_INSERTAR, new String[]{"ID_PAGO"})) {
            Vincular.registrar(statement,
                    idPedido,
                    pago.getTipoPago().name(),
                    pago.getMonto(),
                    pago.getTipoPago() == TipoPago.EFECTIVO ? pago.getEfectivoRecibido() : null,
                    pago.getTipoPago() == TipoPago.EFECTIVO ? pago.getCambio() : 0,
                    pago.getReferencia(),
                    pago.getEstado().name());
            statement.executeUpdate();
            try (ResultSet claves = statement.getGeneratedKeys()) {
                claves.next();
                int id = claves.getInt(1);
                pago.setIdPago(id);
                pago.setIdPedido(idPedido);
                return id;
            }
        } catch (SQLException e) {
            throw ErroresOracle.traducir(e);
        }
    }

    public Pago buscarPorPedido(int idPedido) {
        try (PreparedStatement statement = conexion.prepareStatement(SQL_POR_PEDIDO)) {
            Vincular.registrar(statement, idPedido);
            try (ResultSet resultado = statement.executeQuery()) {
                if (!resultado.next()) {
                    return null;
                }
                TipoPago tipo = TipoPago.valueOf(resultado.getString("TIPO_PAGO"));
                double monto = resultado.getDouble("MONTO");
                Pago pago = tipo == TipoPago.EFECTIVO
                        ? Pago.enEfectivo(monto, resultado.getDouble("EFECTIVO_RECIBIDO"))
                        : Pago.conTarjeta(monto, resultado.getString("REFERENCIA"));
                pago.setIdPago(resultado.getInt("ID_PAGO"));
                pago.setIdPedido(resultado.getInt("ID_PEDIDO"));
                pago.setEstado(EstadoPago.valueOf(resultado.getString("ESTADO")));
                Timestamp fecha = resultado.getTimestamp("FECHA_PAGO");
                if (fecha != null) {
                    pago.setFechaPago(new java.util.Date(fecha.getTime()));
                }
                return pago;
            }
        } catch (SQLException e) {
            throw ErroresOracle.traducir(e);
        }
    }
}
