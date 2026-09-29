package panpuntos.dao;

import panpuntos.model.Categoria;
import panpuntos.model.EstadoRegistro;
import panpuntos.model.Producto;
import panpuntos.util.ErroresOracle;
import panpuntos.util.ExcepcionNegocio;
import panpuntos.util.Vincular;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    private static final String SQL_ABASTECER = "{ CALL PK_INVENTARIO.ABASTECER(?, ?, ?) }";

    private static final String SQL_DESCONTAR = "{ CALL PK_INVENTARIO.DESCONTAR(?, ?, ?, ?) }";

    private static final String SQL_DESCONTAR_MENU = "{ CALL PK_INVENTARIO.DESCONTAR_MENU(?, ?, ?, ?) }";

    private final Connection conexion;

    public ProductoDAO(Connection conexion) {
        this.conexion = conexion;
    }

    public int insertar(Producto producto) {
        return ejecutarInsercion(producto, producto.getExistencia());
    }

    public int insertarConStock(Producto producto, int existenciaInicial) {
        return ejecutarInsercion(producto, existenciaInicial);
    }

    private int ejecutarInsercion(Producto producto, int existenciaInicial) {
        String sql = """
                INSERT INTO PRODUCTO (ID_PRODUCTO, CODIGO, NOMBRE, CATEGORIA, PRECIO_UNITARIO, EXISTENCIA, ESTADO)
                VALUES (SEQ_PRODUCTO.NEXTVAL, ?, ?, ?, ?, 0, ?)
                """;
        try (PreparedStatement statement = conexion.prepareStatement(sql, new String[]{"ID_PRODUCTO"})) {
            Vincular.registrar(statement, producto.getCodigo(), producto.getNombre(),
                    producto.getCategoria().name(), producto.getPrecioUnitario(), producto.getEstado().name());
            statement.executeUpdate();
            int idProducto;
            try (ResultSet claves = statement.getGeneratedKeys()) {
                claves.next();
                idProducto = claves.getInt(1);
            }
            producto.setIdProducto(idProducto);
            producto.setExistencia(0);
            if (existenciaInicial > 0) {
                abastecer(idProducto, existenciaInicial, "CARGA-INICIAL");
            }
            return idProducto;
        } catch (SQLException e) {
            throw ErroresOracle.traducir(e);
        }
    }

    public void abastecer(int idProducto, int cantidad, String referencia) {
        invocar(SQL_ABASTECER, idProducto, cantidad, referencia, null);
    }

    public void descontarProducto(int idProducto, int cantidad, String tipoMovimiento, String referencia) {
        invocar(SQL_DESCONTAR, idProducto, cantidad, tipoMovimiento, referencia);
    }

    public void descontarMenu(int idMenu, int cantidad, String tipoMovimiento, String referencia) {
        invocar(SQL_DESCONTAR_MENU, idMenu, cantidad, tipoMovimiento, referencia);
    }

    private void invocar(String sql, int id, int cantidad, String texto, String referencia) {
        try (CallableStatement statement = conexion.prepareCall(sql)) {
            statement.setInt(1, id);
            statement.setInt(2, cantidad);
            if (sql.equals(SQL_ABASTECER)) {
                statement.setString(3, texto);
            } else {
                statement.setString(3, texto);
                statement.setString(4, referencia);
            }
            statement.execute();
        } catch (SQLException e) {
            throw ErroresOracle.traducir(e);
        }
    }

    public void actualizar(Producto producto) {
        String sql = """
                UPDATE PRODUCTO SET CODIGO = ?, NOMBRE = ?, CATEGORIA = ?, PRECIO_UNITARIO = ?, ESTADO = ?
                WHERE ID_PRODUCTO = ?
                """;
        try (PreparedStatement statement = conexion.prepareStatement(sql)) {
            Vincular.registrar(statement, producto.getCodigo(), producto.getNombre(),
                    producto.getCategoria().name(), producto.getPrecioUnitario(),
                    producto.getEstado().name(), producto.getIdProducto());
            if (statement.executeUpdate() == 0) {
                throw new ExcepcionNegocio("El producto ya no existe en la base de datos.");
            }
        } catch (SQLException e) {
            throw ErroresOracle.traducir(e);
        }
    }

    public void cambiarEstado(int idProducto, EstadoRegistro estado) {
        Conexion.ejecutar(conexion, "UPDATE PRODUCTO SET ESTADO = ? WHERE ID_PRODUCTO = ?",
                estado.name(), idProducto);
    }

    public Producto buscarPorId(int idProducto) {
        return consultarUno("""
                SELECT ID_PRODUCTO, CODIGO, NOMBRE, CATEGORIA, PRECIO_UNITARIO, EXISTENCIA, ESTADO
                FROM PRODUCTO WHERE ID_PRODUCTO = ?
                """, idProducto);
    }

    public Producto buscarPorCodigo(String codigo) {
        return consultarUno("""
                SELECT ID_PRODUCTO, CODIGO, NOMBRE, CATEGORIA, PRECIO_UNITARIO, EXISTENCIA, ESTADO
                FROM PRODUCTO WHERE UPPER(CODIGO) = UPPER(?)
                """, codigo);
    }

    public List<Producto> listar(String categoria, String filtro) {
        String sql = """
                SELECT ID_PRODUCTO, CODIGO, NOMBRE, CATEGORIA, PRECIO_UNITARIO, EXISTENCIA, ESTADO
                FROM PRODUCTO
                WHERE (? IS NULL OR UPPER(CATEGORIA) = UPPER(?))
                  AND (? IS NULL OR UPPER(NOMBRE) LIKE UPPER('%' || ? || '%')
                                  OR UPPER(CODIGO) LIKE UPPER('%' || ? || '%'))
                ORDER BY CATEGORIA, NOMBRE
                """;
        String cat = categoria == null || categoria.isBlank() ? null : categoria.trim();
        String texto = filtro == null || filtro.isBlank() ? null : filtro.trim();
        java.util.List<Producto> productos = new java.util.ArrayList<>();
        try (PreparedStatement statement = conexion.prepareStatement(sql)) {
            Vincular.registrar(statement, cat, cat, texto, texto, texto);
            try (ResultSet resultado = statement.executeQuery()) {
                while (resultado.next()) {
                    productos.add(mapear(resultado));
                }
            }
            return productos;
        } catch (SQLException e) {
            throw ErroresOracle.traducir(e);
        }
    }

    private Producto consultarUno(String sql, Object... parametros) {
        try (PreparedStatement statement = conexion.prepareStatement(sql)) {
            Vincular.registrar(statement, parametros);
            try (ResultSet resultado = statement.executeQuery()) {
                return resultado.next() ? mapear(resultado) : null;
            }
        } catch (SQLException e) {
            throw ErroresOracle.traducir(e);
        }
    }

    private Producto mapear(ResultSet resultado) throws SQLException {
        return new Producto(
                resultado.getInt("ID_PRODUCTO"),
                resultado.getString("CODIGO"),
                resultado.getString("NOMBRE"),
                Categoria.desdeTexto(resultado.getString("CATEGORIA")),
                resultado.getDouble("PRECIO_UNITARIO"),
                resultado.getInt("EXISTENCIA"),
                EstadoRegistro.valueOf(resultado.getString("ESTADO")));
    }
}
