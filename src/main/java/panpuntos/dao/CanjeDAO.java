package panpuntos.dao;

import panpuntos.model.Canje;
import panpuntos.model.EstadoRegistro;
import panpuntos.model.Recompensa;
import panpuntos.model.TipoItem;
import panpuntos.util.ErroresOracle;
import panpuntos.util.Vincular;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class CanjeDAO {

    private static final String SQL_INSERTAR = """
            INSERT INTO CANJE (ID_CANJE, ID_CLIENTE, ID_RECOMPENSA, PUNTOS_USADOS, FECHA_CANJE)
            VALUES (SEQ_CANJE.NEXTVAL, ?, ?, ?, SYSDATE)
            """;

    private static final String SQL_RECOMPENSAS = """
            SELECT ID_RECOMPENSA, CODIGO, NOMBRE, TIPO_ITEM, ID_PRODUCTO, ID_MENU, PUNTOS_NECESARIOS, ESTADO
            FROM RECOMPENSA
            WHERE (? IS NULL OR UPPER(NOMBRE) LIKE UPPER('%' || ? || '%') OR UPPER(CODIGO) = UPPER(?))
            ORDER BY PUNTOS_NECESARIOS
            """;

    private static final String SQL_RECOMPENSA_POR_ID = """
            SELECT ID_RECOMPENSA, CODIGO, NOMBRE, TIPO_ITEM, ID_PRODUCTO, ID_MENU, PUNTOS_NECESARIOS, ESTADO
            FROM RECOMPENSA WHERE ID_RECOMPENSA = ?
            """;

    private static final String SQL_HISTORIAL = """
            SELECT CA.ID_CANJE, CA.FECHA_CANJE, C.DPI, C.NOMBRE, R.CODIGO, R.NOMBRE, CA.PUNTOS_USADOS
            FROM CANJE CA
            JOIN CLIENTE C ON C.ID_CLIENTE = CA.ID_CLIENTE
            JOIN RECOMPENSA R ON R.ID_RECOMPENSA = CA.ID_RECOMPENSA
            ORDER BY CA.FECHA_CANJE DESC, CA.ID_CANJE DESC
            """;

    private final Connection conexion;

    public CanjeDAO(Connection conexion) {
        this.conexion = conexion;
    }

    public int registrar(int idCliente, int idRecompensa, int puntosUsados) {
        try (PreparedStatement statement = conexion.prepareStatement(SQL_INSERTAR, new String[]{"ID_CANJE"})) {
            Vincular.registrar(statement, idCliente, idRecompensa, puntosUsados);
            statement.executeUpdate();
            try (ResultSet claves = statement.getGeneratedKeys()) {
                claves.next();
                return claves.getInt(1);
            }
        } catch (SQLException e) {
            throw ErroresOracle.traducir(e);
        }
    }

    public Recompensa buscarRecompensa(int idRecompensa) {
        try (PreparedStatement statement = conexion.prepareStatement(SQL_RECOMPENSA_POR_ID)) {
            Vincular.registrar(statement, idRecompensa);
            try (ResultSet resultado = statement.executeQuery()) {
                return resultado.next() ? mapearRecompensa(resultado) : null;
            }
        } catch (SQLException e) {
            throw ErroresOracle.traducir(e);
        }
    }

    public List<Recompensa> listarRecompensas(String filtro) {
        String texto = filtro == null || filtro.isBlank() ? null : filtro.trim();
        List<Recompensa> recompensas = new ArrayList<>();
        try (PreparedStatement statement = conexion.prepareStatement(SQL_RECOMPENSAS)) {
            Vincular.registrar(statement, texto, texto, texto);
            try (ResultSet resultado = statement.executeQuery()) {
                while (resultado.next()) {
                    recompensas.add(mapearRecompensa(resultado));
                }
            }
            return recompensas;
        } catch (SQLException e) {
            throw ErroresOracle.traducir(e);
        }
    }

    public List<Canje> listarHistorial() {
        List<Canje> canjes = new ArrayList<>();
        try (PreparedStatement statement = conexion.prepareStatement(SQL_HISTORIAL);
             ResultSet resultado = statement.executeQuery()) {
            while (resultado.next()) {
                Timestamp fecha = resultado.getTimestamp("FECHA_CANJE");
                canjes.add(new Canje(
                        resultado.getInt("ID_CANJE"),
                        resultado.getString("DPI"),
                        resultado.getString("NOMBRE"),
                        resultado.getString("CODIGO"),
                        resultado.getString("NOMBRE"),
                        resultado.getInt("PUNTOS_USADOS"),
                        fecha == null ? null : new java.util.Date(fecha.getTime())));
            }
            return canjes;
        } catch (SQLException e) {
            throw ErroresOracle.traducir(e);
        }
    }

    private Recompensa mapearRecompensa(ResultSet resultado) throws SQLException {
        Integer idProducto = resultado.getInt("ID_PRODUCTO");
        if (resultado.wasNull()) {
            idProducto = null;
        }
        Integer idMenu = resultado.getInt("ID_MENU");
        if (resultado.wasNull()) {
            idMenu = null;
        }
        return new Recompensa(
                resultado.getInt("ID_RECOMPENSA"),
                resultado.getString("CODIGO"),
                resultado.getString("NOMBRE"),
                TipoItem.valueOf(resultado.getString("TIPO_ITEM")),
                idProducto,
                idMenu,
                resultado.getInt("PUNTOS_NECESARIOS"),
                EstadoRegistro.valueOf(resultado.getString("ESTADO")));
    }
}
