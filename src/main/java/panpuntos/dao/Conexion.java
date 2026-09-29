package panpuntos.dao;

import panpuntos.util.ConexionOracle;

import java.sql.Connection;

final class Conexion {

    private Conexion() {
    }

    static void ejecutar(Connection conexion, String sql, Object... parametros) {
        ConexionOracle.ejecutarActualizacion(conexion, sql, parametros);
    }
}
