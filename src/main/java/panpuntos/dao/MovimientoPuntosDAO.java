package panpuntos.dao;

import panpuntos.model.MovimientoPuntos;
import panpuntos.util.ErroresOracle;
import panpuntos.util.ExcepcionNegocio;
import panpuntos.util.Vincular;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class MovimientoPuntosDAO {

    private static final String SQL_ACREDITAR = "{ CALL PK_PUNTOS.ACREDITAR(?, ?, ?) }";
    private static final String SQL_DESCONTAR = "{ CALL PK_PUNTOS.DESCONTAR(?, ?, ?) }";

    private static final String SQL_AJUSTE = """
            INSERT INTO MOVIMIENTO_PUNTOS
                (ID_MOVIMIENTO, ID_CLIENTE, TIPO_MOVIMIENTO, PUNTOS, SALDO_RESULTANTE, REFERENCIA, FECHA_MOVIMIENTO)
            SELECT SEQ_MOV_PUNTOS.NEXTVAL, C.ID_CLIENTE, 'AJUSTE', C.SALDO_PUNTOS + ?, C.SALDO_PUNTOS + ?, ?, SYSDATE
            FROM CLIENTE C WHERE C.ID_CLIENTE = ?
            """;

    private static final String SQL_POR_CLIENTE = """
            SELECT ID_MOVIMIENTO, ID_CLIENTE, TIPO_MOVIMIENTO, PUNTOS, SALDO_RESULTANTE, REFERENCIA, FECHA_MOVIMIENTO
            FROM MOVIMIENTO_PUNTOS WHERE ID_CLIENTE = ? ORDER BY FECHA_MOVIMIENTO, ID_MOVIMIENTO
            """;

    private final Connection conexion;

    public MovimientoPuntosDAO(Connection conexion) {
        this.conexion = conexion;
    }

    public void acreditar(int idCliente, int puntos, String referencia) {
        invocar(SQL_ACREDITAR, idCliente, puntos, referencia);
    }

    public void descontar(int idCliente, int puntos, String referencia) {
        invocar(SQL_DESCONTAR, idCliente, puntos, referencia);
    }

    public void ajustar(int idCliente, int delta, String referencia) {
        if (delta == 0) {
            throw new ExcepcionNegocio("El ajuste de puntos debe ser distinto de cero.");
        }
        try (PreparedStatement statement = conexion.prepareStatement(SQL_AJUSTE)) {
            Vincular.registrar(statement, delta, delta, referencia, idCliente);
            if (statement.executeUpdate() == 0) {
                throw new ExcepcionNegocio("El cliente indicado no existe.");
            }
        } catch (SQLException e) {
            throw ErroresOracle.traducir(e);
        }
    }

    public List<MovimientoPuntos> listarPorCliente(int idCliente) {
        List<MovimientoPuntos> movimientos = new ArrayList<>();
        try (PreparedStatement statement = conexion.prepareStatement(SQL_POR_CLIENTE)) {
            Vincular.registrar(statement, idCliente);
            try (ResultSet resultado = statement.executeQuery()) {
                while (resultado.next()) {
                    Timestamp fecha = resultado.getTimestamp("FECHA_MOVIMIENTO");
                    movimientos.add(new MovimientoPuntos(
                            resultado.getInt("ID_MOVIMIENTO"),
                            resultado.getInt("ID_CLIENTE"),
                            MovimientoPuntos.Tipo.valueOf(resultado.getString("TIPO_MOVIMIENTO")),
                            resultado.getInt("PUNTOS"),
                            resultado.getInt("SALDO_RESULTANTE"),
                            resultado.getString("REFERENCIA"),
                            fecha == null ? null : new java.util.Date(fecha.getTime())));
                }
            }
            return movimientos;
        } catch (SQLException e) {
            throw ErroresOracle.traducir(e);
        }
    }

    private void invocar(String sql, int idCliente, int puntos, String referencia) {
        try (CallableStatement statement = conexion.prepareCall(sql)) {
            statement.setInt(1, idCliente);
            statement.setInt(2, puntos);
            statement.setString(3, referencia);
            statement.execute();
        } catch (SQLException e) {
            throw ErroresOracle.traducir(e);
        }
    }
}
