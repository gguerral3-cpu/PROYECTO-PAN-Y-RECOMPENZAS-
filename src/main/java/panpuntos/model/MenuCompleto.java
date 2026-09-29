package panpuntos.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MenuCompleto extends ItemVenta {

    private final Menu menu;

    public MenuCompleto(Menu menu, int cantidad) {
        super(menu.getIdMenu(), menu.getCodigo(), menu.getNombre(), cantidad, menu.getPrecioMenu());
        this.menu = menu;
    }

    public MenuCompleto(Menu menu, int cantidad, double precioUnitario) {
        super(menu.getIdMenu(), menu.getCodigo(), menu.getNombre(), cantidad, precioUnitario);
        this.menu = menu;
    }

    @Override
    public int calcularPuntos() {
        return getCantidad() * Menu.PUNTOS_POR_MENU;
    }

    @Override
    public Map<Integer, Integer> unidadesDeInventario() {
        Map<Integer, Integer> unidades = new LinkedHashMap<>();
        for (ComponenteMenu componente : menu.getComponentes()) {
            int total = componente.getCantidad() * getCantidad();
            unidades.merge(componente.getIdProducto(), total, Integer::sum);
        }
        return unidades;
    }

    @Override
    public String getTipoItem() {
        return "MENU";
    }

    @Override
    public Integer getIdProducto() {
        return null;
    }

    @Override
    public Integer getIdMenu() {
        return menu.getIdMenu();
    }

    @Override
    public String getDetalleComposicion() {
        return menu.getDescripcionComponentes();
    }

    public Menu getMenu() {
        return menu;
    }

    public List<ComponenteMenu> getComponentes() {
        return menu.getComponentes();
    }
}
