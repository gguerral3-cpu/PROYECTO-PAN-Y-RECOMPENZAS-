package panpuntos.util;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Calendar;
import java.util.Date;

public final class Vincular {

    private Vincular() {
    }

    public static void registrar(PreparedStatement statement, Object... parametros) throws SQLException {
        if (parametros == null) {
            return;
        }
        for (int i = 0; i < parametros.length; i++) {
            int indice = i + 1;
            Object valor = parametros[i];
            if (valor == null) {
                statement.setNull(indice, Types.NULL);
            } else if (valor instanceof Integer numero) {
                statement.setInt(indice, numero);
            } else if (valor instanceof Long numero) {
                statement.setLong(indice, numero);
            } else if (valor instanceof Double numero) {
                statement.setDouble(indice, numero);
            } else if (valor instanceof Boolean indicador) {
                statement.setBoolean(indice, indicador);
            } else if (valor instanceof Date fecha) {
                statement.setTimestamp(indice, new java.sql.Timestamp(fecha.getTime()));
            } else if (valor instanceof Calendar momento) {
                statement.setTimestamp(indice, new java.sql.Timestamp(momento.getTimeInMillis()));
            } else {
                statement.setString(indice, valor.toString());
            }
        }
    }
}
