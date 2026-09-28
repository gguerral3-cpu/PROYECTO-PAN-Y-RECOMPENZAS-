package panpuntos.util;

import java.util.Collection;
import java.util.Map;

public final class Json {

    private Json() {
    }

    public static String escribir(Object valor) {
        StringBuilder texto = new StringBuilder();
        construir(texto, valor);
        return texto.toString();
    }

    private static void construir(StringBuilder texto, Object valor) {
        if (valor == null) {
            texto.append("null");
        } else if (valor instanceof Map<?, ?> mapa) {
            texto.append('{');
            boolean primero = true;
            for (Map.Entry<?, ?> entrada : mapa.entrySet()) {
                if (!primero) {
                    texto.append(',');
                }
                primero = false;
                texto.append('"').append(escapar(String.valueOf(entrada.getKey()))).append("\":");
                construir(texto, entrada.getValue());
            }
            texto.append('}');
        } else if (valor instanceof Collection<?> coleccion) {
            texto.append('[');
            boolean primero = true;
            for (Object elemento : coleccion) {
                if (!primero) {
                    texto.append(',');
                }
                primero = false;
                construir(texto, elemento);
            }
            texto.append(']');
        } else if (valor instanceof Object[] arreglo) {
            construir(texto, java.util.Arrays.asList(arreglo));
        } else if (valor instanceof Number || valor instanceof Boolean) {
            texto.append(valor);
        } else {
            texto.append('"').append(escapar(valor.toString())).append('"');
        }
    }

    private static String escapar(String valor) {
        StringBuilder texto = new StringBuilder();
        for (int i = 0; i < valor.length(); i++) {
            char caracter = valor.charAt(i);
            switch (caracter) {
                case '"' -> texto.append("\\\"");
                case '\\' -> texto.append("\\\\");
                case '\n' -> texto.append("\\n");
                case '\r' -> texto.append("\\r");
                case '\t' -> texto.append("\\t");
                default -> {
                    if (caracter < 0x20) {
                        texto.append(String.format("\\u%04x", (int) caracter));
                    } else {
                        texto.append(caracter);
                    }
                }
            }
        }
        return texto.toString();
    }
}
