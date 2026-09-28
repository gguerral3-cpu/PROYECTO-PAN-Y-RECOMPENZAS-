package panpuntos.util;

import java.sql.Connection;
import java.sql.SQLException;

public final class Transaccion {

    @FunctionalInterface
    public interface Accion<T> {
        T aplicar(Connection conexion);
    }

    @FunctionalInterface
    public interface Operacion {
        void aplicar(Connection conexion);
    }

    private Transaccion() {
    }

    public static <T> T ejecutar(Accion<T> accion) {
        Connection conexion = ConexionOracle.conectar();
        try {
            conexion.setAutoCommit(false);
            T resultado = accion.aplicar(conexion);
            conexion.commit();
            return resultado;
        } catch (SQLException e) {
            deshacer(conexion);
            throw ErroresOracle.traducir(e);
        } catch (RuntimeException e) {
            deshacer(conexion);
            throw e;
        } finally {
            cerrar(conexion);
        }
    }

    public static void ejecutarVoid(Operacion operacion) {
        ejecutar(conexion -> {
            operacion.aplicar(conexion);
            return null;
        });
    }

    private static void deshacer(Connection conexion) {
        try {
            conexion.rollback();
        } catch (SQLException ignorada) {
        }
    }

    private static void cerrar(Connection conexion) {
        try {
            conexion.setAutoCommit(true);
        } catch (SQLException ignorada) {
        }
        ConexionOracle.cerrar(conexion);
    }
}
