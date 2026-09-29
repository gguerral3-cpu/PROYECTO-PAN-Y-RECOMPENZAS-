package panpuntos.dao;

import panpuntos.model.EstadoRegistro;
import panpuntos.model.MovimientoInventario;
import panpuntos.model.Producto;
import panpuntos.util.ErroresOracle;
import panpuntos.util.Vincular;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class InventarioDAO {

    private static final String SQL_ABASTECER = "{ CALL PK_INVENTARIO.ABASTECER(?, ?, ?) }";

    private static final String SQL_MOVIMIENTOS = """
            SELECT MI.ID_MOVIMIENTO, P.CODIGO, P.NOMBRE, MI.TIPO_MOVIMIENTO, MI.CANTIDAD,
                   MI.EXISTENCIA_ANTERIOR, MI.EXISTENCIA_NUEVA, MI.REFERENCIA, MI.FECHA_MOVIMIENTO
            FROM MOVIMIENTO_INVENTARIO MI
            JOIN PRODUCTO P ON P.ID_PRODUCTO = MI.ID_PRODUCTO
            WHERE (? IS NULL OR MI.ID_PRODUCTO = ?)
            ORDER BY MI.FECHA_MOVIMIENTO DESC, MI.ID_MOVIMIENTO DESC
            """;

    private static final String SQL_CRITICOS = """
            SELECT CODIGO, NOMBRE, CATEGORIA, EXISTENCIA FROM VW_INVENTARIO_CRITICO
            """;

    private final Connection conexion;

    public InventarioDAO(Connection conexion) {
        this.conexion = conexion;
    }

    public void abastecer(int idProducto, int cantidad, String referencia) {
        try (CallableStatement statement = conexion.prepareCall(SQL_ABASTECER)) {
            statement.setInt(1, idProducto);
            statement.setInt(2, cantidad);
            statement.setString(3, referencia);
            statement.execute();
        } catch (SQLException e) {
            throw ErroresOracle.traducir(e);
        }
    }

    public List<MovimientoInventario> listarMovimientos(Integer idProducto) {
        List<MovimientoInventario> movimientos = new ArrayList<>();
        try (PreparedStatement statement = conexion.prepareStatement(SQL_MOVIMIENTOS)) {
            Vincular.registrar(statement, idProducto, idProducto);
            try (ResultSet resultado = statement.executeQuery()) {
                while (resultado.next()) {
                    Timestamp fecha = resultado.getTimestamp("FECHA_MOVIMIENTO");
                    movimientos.add(new MovimientoInventario(
                            resultado.getInt("ID_MOVIMIENTO"),
                            resultado.getString("CODIGO"),
                            resultado.getString("NOMBRE"),
                            MovimientoInventario.Tipo.valueOf(resultado.getString("TIPO_MOVIMIENTO")),
                            resultado.getInt("CANTIDAD"),
                            resultado.getInt("EXISTENCIA_ANTERIOR"),
                            resultado.getInt("EXISTENCIA_NUEVA"),
                            resultado.getString("REFERENCIA"),
                            fecha == null ? null : new java.util.Date(fecha.getTime())));
                }
            }
            return movimientos;
        } catch (SQLException e) {
            throw ErroresOracle.traducir(e);
        }
    }

    public List<Producto> listarCriticos() {
        List<Producto> productos = new ArrayList<>();
        try (PreparedStatement statement = conexion.prepareStatement(SQL_CRITICOS);
             ResultSet resultado = statement.executeQuery()) {
            while (resultado.next()) {
                productos.add(new Producto(
                        0,
                        resultado.getString("CODIGO"),
                        resultado.getString("NOMBRE"),
                        panpuntos.model.Categoria.desdeTexto(resultado.getString("CATEGORIA")),
                        0,
                        resultado.getInt("EXISTENCIA"),
                        EstadoRegistro.ACTIVO));
            }
            return productos;
        } catch (SQLException e) {
            throw ErroresOracle.traducir(e);
        }
    }
}
