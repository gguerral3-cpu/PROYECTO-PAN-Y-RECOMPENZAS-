package panpuntos.dao;

import panpuntos.model.ComponenteMenu;
import panpuntos.model.EstadoRegistro;
import panpuntos.model.Menu;
import panpuntos.util.ErroresOracle;
import panpuntos.util.ExcepcionNegocio;
import panpuntos.util.Vincular;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MenuDAO {

    private static final String SQL_INSERTAR_MENU = """
            INSERT INTO MENU (ID_MENU, CODIGO, NOMBRE, PRECIO_MENU, ESTADO)
            VALUES (SEQ_MENU.NEXTVAL, ?, ?, ?, ?)
            """;

    private static final String SQL_INSERTAR_COMPONENTE = """
            INSERT INTO DETALLE_MENU (ID_MENU, ID_PRODUCTO, CANTIDAD) VALUES (?, ?, ?)
            """;

    private static final String SQL_BORRAR_COMPONENTES = "DELETE FROM DETALLE_MENU WHERE ID_MENU = ?";

    private static final String SQL_ACTUALIZAR = """
            UPDATE MENU SET CODIGO = ?, NOMBRE = ?, PRECIO_MENU = ?, ESTADO = ? WHERE ID_MENU = ?
            """;

    private static final String SQL_ESTADO = "UPDATE MENU SET ESTADO = ? WHERE ID_MENU = ?";

    private static final String SQL_POR_ID = """
            SELECT ID_MENU, CODIGO, NOMBRE, PRECIO_MENU, ESTADO FROM MENU WHERE ID_MENU = ?
            """;

    private static final String SQL_POR_CODIGO = """
            SELECT ID_MENU, CODIGO, NOMBRE, PRECIO_MENU, ESTADO FROM MENU WHERE UPPER(CODIGO) = UPPER(?)
            """;

    private static final String SQL_LISTAR = """
            SELECT ID_MENU, CODIGO, NOMBRE, PRECIO_MENU, ESTADO FROM MENU
            WHERE (? IS NULL OR UPPER(NOMBRE) LIKE UPPER('%' || ? || '%')
                          OR UPPER(CODIGO) LIKE UPPER('%' || ? || '%'))
            ORDER BY NOMBRE
            """;

    private static final String SQL_COMPONENTES = """
            SELECT DM.ID_PRODUCTO, P.NOMBRE, DM.CANTIDAD, P.PRECIO_UNITARIO
            FROM DETALLE_MENU DM JOIN PRODUCTO P ON P.ID_PRODUCTO = DM.ID_PRODUCTO
            WHERE DM.ID_MENU = ? ORDER BY P.NOMBRE
            """;

    private final Connection conexion;

    public MenuDAO(Connection conexion) {
        this.conexion = conexion;
    }

    public int insertar(Menu menu) {
        if (menu.getComponentes().isEmpty()) {
            throw new ExcepcionNegocio("El menu debe tener al menos un componente: sandwich, bebida y acompanamiento.");
        }
        try (PreparedStatement statement = conexion.prepareStatement(SQL_INSERTAR_MENU, new String[]{"ID_MENU"})) {
            Vincular.registrar(statement, menu.getCodigo(), menu.getNombre(),
                    menu.getPrecioMenu(), menu.getEstado().name());
            statement.executeUpdate();
            int idMenu;
            try (ResultSet claves = statement.getGeneratedKeys()) {
                claves.next();
                idMenu = claves.getInt(1);
            }
            menu.setIdMenu(idMenu);
            guardarComponentes(menu);
            return idMenu;
        } catch (SQLException e) {
            throw ErroresOracle.traducir(e);
        }
    }

    public void actualizar(Menu menu) {
        try (PreparedStatement statement = conexion.prepareStatement(SQL_ACTUALIZAR)) {
            Vincular.registrar(statement, menu.getCodigo(), menu.getNombre(),
                    menu.getPrecioMenu(), menu.getEstado().name(), menu.getIdMenu());
            if (statement.executeUpdate() == 0) {
                throw new ExcepcionNegocio("El menu ya no existe en la base de datos.");
            }
        } catch (SQLException e) {
            throw ErroresOracle.traducir(e);
        }
        Conexion.ejecutar(conexion, SQL_BORRAR_COMPONENTES, menu.getIdMenu());
        guardarComponentes(menu);
    }

    public void cambiarEstado(int idMenu, EstadoRegistro estado) {
        Conexion.ejecutar(conexion, SQL_ESTADO, estado.name(), idMenu);
    }

    public Menu buscarPorId(int idMenu) {
        return consultarUno(SQL_POR_ID, idMenu);
    }

    public Menu buscarPorCodigo(String codigo) {
        Menu menu = consultarUno(SQL_POR_CODIGO, codigo);
        if (menu == null) {
            throw new ExcepcionNegocio("No existe un menu con el codigo " + codigo + ".");
        }
        return menu;
    }

    public List<Menu> listar(String filtro) {
        String texto = filtro == null || filtro.isBlank() ? null : filtro.trim();
        List<Menu> menus = new ArrayList<>();
        try (PreparedStatement statement = conexion.prepareStatement(SQL_LISTAR)) {
            Vincular.registrar(statement, texto, texto, texto);
            try (ResultSet resultado = statement.executeQuery()) {
                while (resultado.next()) {
                    Menu menu = mapear(resultado);
                    cargarComponentes(menu);
                    menus.add(menu);
                }
            }
            return menus;
        } catch (SQLException e) {
            throw ErroresOracle.traducir(e);
        }
    }

    private void guardarComponentes(Menu menu) {
        try (PreparedStatement statement = conexion.prepareStatement(SQL_INSERTAR_COMPONENTE)) {
            for (ComponenteMenu componente : menu.getComponentes()) {
                Vincular.registrar(statement, menu.getIdMenu(), componente.getIdProducto(), componente.getCantidad());
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            throw ErroresOracle.traducir(e);
        }
    }

    private void cargarComponentes(Menu menu) {
        try (PreparedStatement statement = conexion.prepareStatement(SQL_COMPONENTES)) {
            Vincular.registrar(statement, menu.getIdMenu());
            try (ResultSet resultado = statement.executeQuery()) {
                while (resultado.next()) {
                    menu.agregarComponente(new ComponenteMenu(
                            resultado.getInt("ID_PRODUCTO"),
                            resultado.getString("NOMBRE"),
                            resultado.getInt("CANTIDAD"),
                            resultado.getDouble("PRECIO_UNITARIO")));
                }
            }
        } catch (SQLException e) {
            throw ErroresOracle.traducir(e);
        }
    }

    private Menu consultarUno(String sql, Object... parametros) {
        try (PreparedStatement statement = conexion.prepareStatement(sql)) {
            Vincular.registrar(statement, parametros);
            try (ResultSet resultado = statement.executeQuery()) {
                if (!resultado.next()) {
                    return null;
                }
                Menu menu = mapear(resultado);
                cargarComponentes(menu);
                return menu;
            }
        } catch (SQLException e) {
            throw ErroresOracle.traducir(e);
        }
    }

    private Menu mapear(ResultSet resultado) throws SQLException {
        return new Menu(
                resultado.getInt("ID_MENU"),
                resultado.getString("CODIGO"),
                resultado.getString("NOMBRE"),
                resultado.getDouble("PRECIO_MENU"),
                EstadoRegistro.valueOf(resultado.getString("ESTADO")));
    }
}
