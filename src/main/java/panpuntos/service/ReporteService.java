package panpuntos.service;

import panpuntos.dao.ReporteDAO;
import panpuntos.model.Reporte;
import panpuntos.util.Transaccion;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

public class ReporteService {

    public static final int VENTAS_POR_PERIODO = 1;
    public static final int PRODUCTOS_MAS_VENDIDOS = 2;
    public static final int INVENTARIO_CRITICO = 3;
    public static final int PUNTOS_POR_CLIENTE = 4;
    public static final int HISTORIAL_DE_CANJES = 5;
    public static final int VENTAS_DE_MENUS = 6;

    public Reporte generar(int tipoReporte, Date desde, Date hasta) {
        Timestamp inicio = aTimestamp(desde, false);
        Timestamp fin = aTimestamp(hasta, true);
        return Transaccion.ejecutar(conexion -> {
            ReporteDAO dao = new ReporteDAO(conexion);
            return switch (tipoReporte) {
                case VENTAS_POR_PERIODO -> dao.ventasPorPeriodo(inicio, fin);
                case PRODUCTOS_MAS_VENDIDOS -> dao.productosMasVendidos();
                case INVENTARIO_CRITICO -> dao.inventarioCritico();
                case PUNTOS_POR_CLIENTE -> dao.puntosPorCliente();
                case HISTORIAL_DE_CANJES -> dao.historialDeCanjes();
                case VENTAS_DE_MENUS -> dao.ventasDeMenus(inicio, fin);
                default -> throw new IllegalArgumentException("Reporte no reconocido: " + tipoReporte);
            };
        });
    }

    public static String nombreDe(int tipoReporte) {
        return switch (tipoReporte) {
            case VENTAS_POR_PERIODO -> "Ventas por periodo";
            case PRODUCTOS_MAS_VENDIDOS -> "Productos mas vendidos";
            case INVENTARIO_CRITICO -> "Productos agotados o con bajo inventario";
            case PUNTOS_POR_CLIENTE -> "Puntos acumulados por cliente";
            case HISTORIAL_DE_CANJES -> "Historial de canjes";
            case VENTAS_DE_MENUS -> "Ventas de menus completos";
            default -> "Reporte";
        };
    }

    public static int cantidadDeReportes() {
        return 6;
    }

    private static Timestamp aTimestamp(Date fecha, boolean finDelDia) {
        Calendar calendario = Calendar.getInstance();
        if (fecha == null) {
            calendario.set(2000, Calendar.JANUARY, 1, 0, 0, 0);
            return new Timestamp(calendario.getTimeInMillis());
        }
        calendario.setTime(fecha);
        calendario.set(Calendar.HOUR_OF_DAY, finDelDia ? 23 : 0);
        calendario.set(Calendar.MINUTE, finDelDia ? 59 : 0);
        calendario.set(Calendar.SECOND, finDelDia ? 59 : 0);
        calendario.set(Calendar.MILLISECOND, 0);
        return new Timestamp(calendario.getTimeInMillis());
    }
}
