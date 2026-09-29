package panpuntos.dao;

import panpuntos.model.Categoria;
import panpuntos.model.EstadoRegistro;
import panpuntos.model.Reporte;
import panpuntos.model.TipoPago;
import panpuntos.util.ErroresOracle;
import panpuntos.util.Formato;
import panpuntos.util.Vincular;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

public class ReporteDAO {

    private static final String SQL_VENTAS_PERIODO = """
            SELECT TIPO_PAGO, CANTIDAD_PEDIDOS, TOTAL_VENDIDO, TOTAL_CAMBIO
            FROM (
                SELECT PA.TIPO_PAGO AS TIPO_PAGO,
                       COUNT(*) AS CANTIDAD_PEDIDOS,
                       SUM(PE.TOTAL) AS TOTAL_VENDIDO,
                       NVL(SUM(PA.CAMBIO), 0) AS TOTAL_CAMBIO
                FROM PEDIDO PE JOIN PAGO PA ON PA.ID_PEDIDO = PE.ID_PEDIDO
                WHERE PE.ESTADO IN ('PAGADO', 'ENTREGADO')
                  AND PA.ESTADO = 'APROBADO'
                  AND PE.FECHA_PEDIDO >= ?
                  AND PE.FECHA_PEDIDO < ?
                GROUP BY PA.TIPO_PAGO
            ) ORDER BY TIPO_PAGO
            """;

    private static final String SQL_VENTAS_TOTALES = """
            SELECT COUNT(*) AS CANTIDAD_PEDIDOS,
                   NVL(SUM(PE.TOTAL), 0) AS TOTAL_VENDIDO,
                   NVL(SUM(CASE WHEN PA.TIPO_PAGO = 'EFECTIVO' THEN PE.TOTAL END), 0) AS TOTAL_EFECTIVO,
                   NVL(SUM(CASE WHEN PA.TIPO_PAGO = 'TARJETA'  THEN PE.TOTAL END), 0) AS TOTAL_TARJETA
            FROM PEDIDO PE JOIN PAGO PA ON PA.ID_PEDIDO = PE.ID_PEDIDO
            WHERE PE.ESTADO IN ('PAGADO', 'ENTREGADO')
              AND PA.ESTADO = 'APROBADO'
              AND PE.FECHA_PEDIDO >= ?
              AND PE.FECHA_PEDIDO < ?
            """;

    private static final String SQL_MAS_VENDIDOS = """
            SELECT CODIGO, NOMBRE, CATEGORIA, SUM(UNIDADES) AS UNIDADES, SUM(IMPORTE) AS IMPORTE
            FROM VW_VENTAS_UNIDADES
            GROUP BY CODIGO, NOMBRE, CATEGORIA
            ORDER BY UNIDADES DESC, NOMBRE
            """;

    private static final String SQL_INVENTARIO_CRITICO = """
            SELECT CODIGO, NOMBRE, CATEGORIA, EXISTENCIA FROM VW_INVENTARIO_CRITICO
            """;

    private static final String SQL_PUNTOS_CLIENTE = """
            SELECT DPI, NOMBRE, ESTADO, TOTAL_ACUMULADO, TOTAL_CANJEADO, SALDO_PUNTOS
            FROM VW_PUNTOS_CLIENTE ORDER BY SALDO_PUNTOS DESC, NOMBRE
            """;

    private static final String SQL_HISTORIAL_CANJES = """
            SELECT CA.ID_CANJE, CA.FECHA_CANJE, C.DPI, C.NOMBRE, R.CODIGO, R.NOMBRE, CA.PUNTOS_USADOS
            FROM CANJE CA
            JOIN CLIENTE C ON C.ID_CLIENTE = CA.ID_CLIENTE
            JOIN RECOMPENSA R ON R.ID_RECOMPENSA = CA.ID_RECOMPENSA
            ORDER BY CA.FECHA_CANJE DESC, CA.ID_CANJE DESC
            """;

    private static final String SQL_VENTAS_MENUS = """
            SELECT M.CODIGO, M.NOMBRE, SUM(DP.CANTIDAD) AS CANTIDAD,
                   SUM(DP.SUBTOTAL) AS IMPORTE, SUM(DP.PUNTOS) AS PUNTOS
            FROM DETALLE_PEDIDO DP
            JOIN PEDIDO PE ON PE.ID_PEDIDO = DP.ID_PEDIDO
            JOIN MENU M ON M.ID_MENU = DP.ID_MENU
            WHERE DP.TIPO_ITEM = 'MENU'
              AND PE.ESTADO IN ('PAGADO', 'ENTREGADO')
              AND PE.FECHA_PEDIDO >= ?
              AND PE.FECHA_PEDIDO < ?
            GROUP BY M.CODIGO, M.NOMBRE
            ORDER BY CANTIDAD DESC, M.NOMBRE
            """;

    private final Connection conexion;

    public ReporteDAO(Connection conexion) {
        this.conexion = conexion;
    }

    public Reporte ventasPorPeriodo(Timestamp desde, Timestamp hasta) {
        Reporte reporte = new Reporte("Ventas por periodo",
                new String[]{"Metodo de pago", "Pedidos", "Total vendido", "Cambio entregado"});
        consultar(reporte, SQL_VENTAS_PERIODO, desde, hasta, resultado -> {
            reporte.agregarFila(
                    TipoPago.valueOf(resultado.getString("TIPO_PAGO")).getEtiqueta(),
                    resultado.getInt("CANTIDAD_PEDIDOS"),
                    Formato.moneda(resultado.getDouble("TOTAL_VENDIDO")),
                    Formato.moneda(resultado.getDouble("TOTAL_CAMBIO")));
        });
        consultar(reporte, SQL_VENTAS_TOTALES, desde, hasta, resultado -> {
            reporte.agregarFila("TOTAL GENERAL",
                    resultado.getInt("CANTIDAD_PEDIDOS"),
                    Formato.moneda(resultado.getDouble("TOTAL_VENDIDO")),
                    "Efectivo: " + Formato.moneda(resultado.getDouble("TOTAL_EFECTIVO"))
                            + " | Tarjeta: " + Formato.moneda(resultado.getDouble("TOTAL_TARJETA")));
        });
        return reporte;
    }

    public Reporte productosMasVendidos() {
        Reporte reporte = new Reporte("Productos mas vendidos",
                new String[]{"Codigo", "Producto", "Categoria", "Unidades", "Importe"});
        consultar(reporte, SQL_MAS_VENDIDOS, resultado -> {
            reporte.agregarFila(
                    resultado.getString("CODIGO"),
                    resultado.getString("NOMBRE"),
                    Categoria.desdeTexto(resultado.getString("CATEGORIA")).getEtiqueta(),
                    resultado.getInt("UNIDADES"),
                    Formato.moneda(resultado.getDouble("IMPORTE")));
        });
        return reporte;
    }

    public Reporte inventarioCritico() {
        Reporte reporte = new Reporte("Productos agotados o con bajo inventario",
                new String[]{"Codigo", "Producto", "Categoria", "Existencia"});
        consultar(reporte, SQL_INVENTARIO_CRITICO, resultado -> {
            reporte.agregarFila(
                    resultado.getString("CODIGO"),
                    resultado.getString("NOMBRE"),
                    Categoria.desdeTexto(resultado.getString("CATEGORIA")).getEtiqueta(),
                    resultado.getInt("EXISTENCIA"));
        });
        return reporte;
    }

    public Reporte puntosPorCliente() {
        Reporte reporte = new Reporte("Puntos acumulados por cliente",
                new String[]{"DPI", "Cliente", "Estado", "Acumulados", "Canjeados", "Saldo actual"});
        consultar(reporte, SQL_PUNTOS_CLIENTE, resultado -> {
            reporte.agregarFila(
                    resultado.getString("DPI"),
                    resultado.getString("NOMBRE"),
                    EstadoRegistro.valueOf(resultado.getString("ESTADO")).getEtiqueta(),
                    resultado.getInt("TOTAL_ACUMULADO"),
                    resultado.getInt("TOTAL_CANJEADO"),
                    resultado.getInt("SALDO_PUNTOS"));
        });
        return reporte;
    }

    public Reporte historialDeCanjes() {
        Reporte reporte = new Reporte("Historial de canjes",
                new String[]{"Canje", "Fecha", "DPI", "Cliente", "Recompensa", "Puntos utilizados"});
        consultar(reporte, SQL_HISTORIAL_CANJES, resultado -> {
            Timestamp fecha = resultado.getTimestamp("FECHA_CANJE");
            reporte.agregarFila(
                    resultado.getInt("ID_CANJE"),
                    Formato.fechaHora(fecha == null ? null : new Date(fecha.getTime())),
                    resultado.getString("DPI"),
                    resultado.getString("NOMBRE"),
                    resultado.getString("NOMBRE"),
                    resultado.getInt("PUNTOS_USADOS"));
        });
        return reporte;
    }

    public Reporte ventasDeMenus(Timestamp desde, Timestamp hasta) {
        Reporte reporte = new Reporte("Ventas de menus completos",
                new String[]{"Codigo", "Menu", "Cantidad", "Importe", "Puntos generados"});
        consultar(reporte, SQL_VENTAS_MENUS, desde, hasta, resultado -> {
            reporte.agregarFila(
                    resultado.getString("CODIGO"),
                    resultado.getString("NOMBRE"),
                    resultado.getInt("CANTIDAD"),
                    Formato.moneda(resultado.getDouble("IMPORTE")),
                    resultado.getInt("PUNTOS"));
        });
        return reporte;
    }

    @FunctionalInterface
    private interface Lector {
        void leer(ResultSet resultado) throws SQLException;
    }

    private void consultar(Reporte reporte, String sql, Lector lector) {
        consultar(reporte, sql, new Object[0], lector);
    }

    private void consultar(Reporte reporte, String sql, Timestamp desde, Timestamp hasta, Lector lector) {
        consultar(reporte, sql, new Object[]{desde, hasta}, lector);
    }

    private void consultar(Reporte reporte, String sql, Object[] parametros, Lector lector) {
        try (PreparedStatement statement = conexion.prepareStatement(sql)) {
            Vincular.registrar(statement, parametros);
            try (ResultSet resultado = statement.executeQuery()) {
                while (resultado.next()) {
                    lector.leer(resultado);
                }
            }
        } catch (SQLException e) {
            throw ErroresOracle.traducir(e);
        }
    }

    public List<String> nombresDeReportes() {
        return List.of(
                "1. Ventas por periodo",
                "2. Productos mas vendidos",
                "3. Productos agotados o con bajo inventario",
                "4. Puntos acumulados por cliente",
                "5. Historial de canjes",
                "6. Ventas de menus completos");
    }
}
