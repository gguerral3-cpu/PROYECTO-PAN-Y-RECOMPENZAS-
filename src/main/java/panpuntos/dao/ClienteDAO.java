package panpuntos.dao;

import panpuntos.model.Cliente;
import panpuntos.model.EstadoCliente;
import panpuntos.util.ErroresOracle;
import panpuntos.util.ExcepcionNegocio;
import panpuntos.util.Vincular;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {

    private static final String SQL_INSERTAR = """
            INSERT INTO CLIENTE (ID_CLIENTE, DPI, NOMBRE, TELEFONO, EMAIL, SALDO_PUNTOS, ESTADO, FECHA_REGISTRO)
            VALUES (SEQ_CLIENTE.NEXTVAL, ?, ?, ?, ?, ?, ?, SYSDATE)
            """;

    private static final String SQL_ACTUALIZAR = """
            UPDATE CLIENTE SET DPI = ?, NOMBRE = ?, TELEFONO = ?, EMAIL = ?, ESTADO = ?
            WHERE ID_CLIENTE = ?
            """;

    private static final String SQL_ESTADO = "UPDATE CLIENTE SET ESTADO = ? WHERE ID_CLIENTE = ?";

    private static final String SQL_POR_ID = """
            SELECT ID_CLIENTE, DPI, NOMBRE, TELEFONO, EMAIL, SALDO_PUNTOS, ESTADO, FECHA_REGISTRO
            FROM CLIENTE WHERE ID_CLIENTE = ?
            """;

    private static final String SQL_POR_DPI = """
            SELECT ID_CLIENTE, DPI, NOMBRE, TELEFONO, EMAIL, SALDO_PUNTOS, ESTADO, FECHA_REGISTRO
            FROM CLIENTE WHERE UPPER(DPI) = UPPER(?)
            """;

    private static final String SQL_LISTAR = """
            SELECT ID_CLIENTE, DPI, NOMBRE, TELEFONO, EMAIL, SALDO_PUNTOS, ESTADO, FECHA_REGISTRO
            FROM CLIENTE
            WHERE ? IS NULL
               OR UPPER(NOMBRE) LIKE UPPER('%' || ? || '%')
               OR UPPER(DPI) LIKE UPPER('%' || ? || '%')
            ORDER BY NOMBRE
            """;

    private static final String SQL_EXISTE_PEDIDOS = """
            SELECT COUNT(*) FROM PEDIDO WHERE ID_CLIENTE = ?
            """;

    private static final String SQL_EXISTE_MOVIMIENTOS = """
            SELECT COUNT(*) FROM MOVIMIENTO_PUNTOS WHERE ID_CLIENTE = ?
            """;

    private final Connection conexion;

    public ClienteDAO(Connection conexion) {
        this.conexion = conexion;
    }

    public int insertar(Cliente cliente) {
        try (PreparedStatement statement = conexion.prepareStatement(SQL_INSERTAR, new String[]{"ID_CLIENTE"})) {
            Vincular.registrar(statement, cliente.getDpi(), cliente.getNombre(), cliente.getTelefono(),
                    cliente.getEmail(), 0, cliente.getEstado().name());
            statement.executeUpdate();
            try (ResultSet claves = statement.getGeneratedKeys()) {
                claves.next();
                int id = claves.getInt(1);
                cliente.setIdCliente(id);
                return id;
            }
        } catch (SQLException e) {
            throw ErroresOracle.traducir(e);
        }
    }

    public void actualizar(Cliente cliente) {
        try (PreparedStatement statement = conexion.prepareStatement(SQL_ACTUALIZAR)) {
            Vincular.registrar(statement, cliente.getDpi(), cliente.getNombre(), cliente.getTelefono(),
                    cliente.getEmail(), cliente.getEstado().name(), cliente.getIdCliente());
            if (statement.executeUpdate() == 0) {
                throw new ExcepcionNegocio("El cliente ya no existe en la base de datos.");
            }
        } catch (SQLException e) {
            throw ErroresOracle.traducir(e);
        }
    }

    public void cambiarEstado(int idCliente, EstadoCliente estado) {
        Conexion.ejecutar(conexion, SQL_ESTADO, estado.name(), idCliente);
    }

    public Cliente buscarPorId(int idCliente) {
        return consultarUno(SQL_POR_ID, idCliente);
    }

    public Cliente buscarPorDpi(String dpi) {
        Cliente cliente = consultarUno(SQL_POR_DPI, dpi);
        if (cliente == null) {
            throw new ExcepcionNegocio("No existe un cliente registrado con el DPI " + dpi + ".");
        }
        return cliente;
    }

    public boolean existeOtroConDpi(String dpi, int idClienteExcluido) {
        Cliente cliente = consultarUno(SQL_POR_DPI, dpi);
        return cliente != null && cliente.getIdCliente() != idClienteExcluido;
    }

    public List<Cliente> listar(String filtro) {
        String texto = filtro == null || filtro.isBlank() ? null : filtro.trim();
        List<Cliente> clientes = new ArrayList<>();
        try (PreparedStatement statement = conexion.prepareStatement(SQL_LISTAR)) {
            Vincular.registrar(statement, texto, texto, texto);
            try (ResultSet resultado = statement.executeQuery()) {
                while (resultado.next()) {
                    clientes.add(mapear(resultado));
                }
            }
            return clientes;
        } catch (SQLException e) {
            throw ErroresOracle.traducir(e);
        }
    }

    public boolean tieneMovimientos(int idCliente) {
        return contar(SQL_EXISTE_PEDIDOS, idCliente) > 0 || contar(SQL_EXISTE_MOVIMIENTOS, idCliente) > 0;
    }

    private int contar(String sql, Object... parametros) {
        try (PreparedStatement statement = conexion.prepareStatement(sql)) {
            Vincular.registrar(statement, parametros);
            try (ResultSet resultado = statement.executeQuery()) {
                resultado.next();
                return resultado.getInt(1);
            }
        } catch (SQLException e) {
            throw ErroresOracle.traducir(e);
        }
    }

    private Cliente consultarUno(String sql, Object... parametros) {
        try (PreparedStatement statement = conexion.prepareStatement(sql)) {
            Vincular.registrar(statement, parametros);
            try (ResultSet resultado = statement.executeQuery()) {
                return resultado.next() ? mapear(resultado) : null;
            }
        } catch (SQLException e) {
            throw ErroresOracle.traducir(e);
        }
    }

    private Cliente mapear(ResultSet resultado) throws SQLException {
        Timestamp fecha = resultado.getTimestamp("FECHA_REGISTRO");
        return new Cliente(
                resultado.getInt("ID_CLIENTE"),
                resultado.getString("DPI"),
                resultado.getString("NOMBRE"),
                resultado.getString("TELEFONO"),
                resultado.getString("EMAIL"),
                resultado.getInt("SALDO_PUNTOS"),
                EstadoCliente.desdeTexto(resultado.getString("ESTADO")),
                fecha == null ? null : new Date(fecha.getTime()));
    }
}
