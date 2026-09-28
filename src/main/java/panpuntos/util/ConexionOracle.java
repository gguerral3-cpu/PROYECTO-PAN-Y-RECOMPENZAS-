package panpuntos.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public final class ConexionOracle {

    private static final Properties CONFIGURACION = new Properties();
    private static boolean inicializado = false;

    private ConexionOracle() {
    }

    private static synchronized void inicializar() {
        if (inicializado) {
            return;
        }
        try (InputStream archivo = ConexionOracle.class
                .getResourceAsStream("/database.properties")) {
            if (archivo == null) {
                throw new IllegalStateException("No se encontro el archivo database.properties");
            }
            CONFIGURACION.load(archivo);
            Class.forName(CONFIGURACION.getProperty("db.driver"));
            inicializado = true;
        } catch (IOException | ClassNotFoundException e) {
            throw new ExcepcionBaseDatos("No fue posible cargar la configuracion de la base de datos", e);
        }
    }

    public static Connection conectar() {
        inicializar();
        try {
            return DriverManager.getConnection(
                    CONFIGURACION.getProperty("db.url"),
                    CONFIGURACION.getProperty("db.usuario"),
                    CONFIGURACION.getProperty("db.password"));
        } catch (SQLException e) {
            throw new ExcepcionBaseDatos(
                    "No fue posible conectar con Oracle. Verifique que la base de datos este iniciada "
                            + "y que las credenciales de database.properties sean correctas.", e);
        }
    }

    public static int puertoApi() {
        inicializar();
        return Integer.parseInt(CONFIGURACION.getProperty("api.puerto", "8080"));
    }

    public static void ejecutarActualizacion(Connection conexion, String sql, Object... parametros) {
        try (PreparedStatement statement = conexion.prepareStatement(sql)) {
            Vincular.registrar(statement, parametros);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw ErroresOracle.traducir(e);
        }
    }

    public static void cerrar(Connection conexion) {
        if (conexion == null) {
            return;
        }
        try {
            conexion.rollback();
        } catch (SQLException ignorada) {
        }
        try {
            conexion.close();
        } catch (SQLException ignorada) {
        }
    }

    public static void cerrar(ResultSet resultado) {
        try {
            if (resultado != null) {
                resultado.close();
            }
        } catch (SQLException ignorada) {
        }
    }

    public static void cerrar(PreparedStatement statement) {
        try {
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException ignorada) {
        }
    }
}
