package panpuntos.model;

import java.util.ArrayList;
import java.util.List;

public class Reporte {

    private final String titulo;
    private final String[] columnas;
    private final List<Object[]> filas = new ArrayList<>();

    public Reporte(String titulo, String[] columnas) {
        this.titulo = titulo;
        this.columnas = columnas;
    }

    public void agregarFila(Object... valores) {
        filas.add(valores);
    }

    public String getTitulo() {
        return titulo;
    }

    public String[] getColumnas() {
        return columnas;
    }

    public List<Object[]> getFilas() {
        return filas;
    }

    public int getCantidadFilas() {
        return filas.size();
    }
}
