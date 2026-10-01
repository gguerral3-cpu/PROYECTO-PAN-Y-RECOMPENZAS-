package panpuntos.api;

import panpuntos.model.Cliente;
import panpuntos.model.MovimientoPuntos;
import panpuntos.model.Recompensa;
import panpuntos.service.ClienteService;
import panpuntos.service.FidelizacionService;
import panpuntos.util.ConexionOracle;
import panpuntos.util.ExcepcionNegocio;
import panpuntos.util.Formato;
import panpuntos.util.Json;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class ApiPuntos {

    private static final ClienteService clienteService = new ClienteService();
    private static final FidelizacionService fidelizacionService = new FidelizacionService();

    public static void main(String[] args) throws IOException {
        int puerto = ConexionOracle.puertoApi();
        HttpServer servidor = HttpServer.create(new InetSocketAddress(puerto), 0);
        servidor.setExecutor(Executors.newFixedThreadPool(4));

        servidor.createContext("/api/salud", ApiPuntos::salud);
        servidor.createContext("/api/recompensas", ApiPuntos::recompensas);
        servidor.createContext("/api/clientes", ApiPuntos::clientes);

        servidor.start();
        System.out.println("API de puntos disponible en http://localhost:" + puerto);
        System.out.println("  GET /api/salud");
        System.out.println("  GET /api/recompensas");
        System.out.println("  GET /api/clientes/{dpi}");
        System.out.println("  GET /api/clientes/{dpi}/puntos");
        System.out.println("  GET /api/clientes/{dpi}/historial");
    }

    private static void salud(HttpExchange intercambio) throws IOException {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("servicio", "Pan Puntos y Premios");
        cuerpo.put("baseDeDatos", baseDeDatosActiva() ? "conectada" : "sin conexion");
        responder(intercambio, 200, cuerpo);
    }

    private static void recompensas(HttpExchange intercambio) throws IOException {
        try {
            responder(intercambio, 200, catalogoDeRecompensas());
        } catch (RuntimeException e) {
            responder(intercambio, 500, error(e));
        }
    }

    private static void clientes(HttpExchange intercambio) throws IOException {
        String[] partes = intercambio.getRequestURI().getPath().split("/");
        if (partes.length < 4) {
            Map<String, Object> cuerpo = new LinkedHashMap<>();
            cuerpo.put("mensaje", "Uso: /api/clientes/{dpi}, /api/clientes/{dpi}/puntos, "
                    + "/api/clientes/{dpi}/historial");
            responder(intercambio, 400, cuerpo);
            return;
        }
        String dpi = URLDecoder.decode(partes[3], StandardCharsets.UTF_8);
        String seccion = partes.length > 4 ? partes[4] : "";

        try {
            Cliente cliente = clienteService.buscarPorDpi(dpi);
            Map<String, Object> cuerpo = new LinkedHashMap<>();
            cuerpo.put("dpi", cliente.getDpi());
            cuerpo.put("nombre", cliente.getNombre());
            cuerpo.put("puntos", cliente.getSaldoPuntos());
            cuerpo.put("estado", cliente.getEstado().getEtiqueta());

            if (seccion.equalsIgnoreCase("historial")) {
                cuerpo.put("historial", historial(cliente.getIdCliente()));
            }
            if (seccion.isEmpty() || seccion.equalsIgnoreCase("puntos")) {
                cuerpo.put("recompensas", catalogoDeRecompensas());
            }
            responder(intercambio, 200, cuerpo);
        } catch (ExcepcionNegocio e) {
            Map<String, Object> cuerpo = new LinkedHashMap<>();
            cuerpo.put("mensaje", e.getMessage());
            responder(intercambio, 404, cuerpo);
        } catch (RuntimeException e) {
            responder(intercambio, 500, error(e));
        }
    }

    private static List<Map<String, Object>> historial(int idCliente) {
        List<Map<String, Object>> lista = new ArrayList<>();
        for (MovimientoPuntos movimiento : clienteService.historialDePuntos(idCliente)) {
            Map<String, Object> fila = new LinkedHashMap<>();
            fila.put("fecha", Formato.fechaHora(movimiento.getFechaMovimiento()));
            fila.put("tipo", movimiento.getTipo().getEtiqueta());
            fila.put("puntos", movimiento.getPuntos());
            fila.put("saldo", movimiento.getSaldoResultante());
            fila.put("referencia", movimiento.getReferencia() == null ? "" : movimiento.getReferencia());
            lista.add(fila);
        }
        return lista;
    }

    private static List<Map<String, Object>> catalogoDeRecompensas() {
        List<Map<String, Object>> lista = new ArrayList<>();
        for (Recompensa recompensa : fidelizacionService.listarRecompensas(null)) {
            Map<String, Object> fila = new LinkedHashMap<>();
            fila.put("codigo", recompensa.getCodigo());
            fila.put("nombre", recompensa.getNombre());
            fila.put("puntos", recompensa.getPuntosNecesarios());
            fila.put("estado", recompensa.getEstado().getEtiqueta());
            lista.add(fila);
        }
        return lista;
    }

    private static boolean baseDeDatosActiva() {
        Connection conexion = ConexionOracle.conectar();
        try {
            return conexion.isValid(3);
        } catch (Exception e) {
            return false;
        } finally {
            ConexionOracle.cerrar(conexion);
        }
    }

    private static Map<String, Object> error(RuntimeException e) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("mensaje", e.getMessage() == null ? e.toString() : e.getMessage());
        return cuerpo;
    }

    private static void responder(HttpExchange intercambio, int codigo, Object cuerpo) {
        try {
            byte[] json = Json.escribir(cuerpo).getBytes(StandardCharsets.UTF_8);
            intercambio.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
            intercambio.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            intercambio.sendResponseHeaders(codigo, json.length);
            try (OutputStream salida = intercambio.getResponseBody()) {
                salida.write(json);
            }
        } catch (IOException e) {
            intercambio.close();
        }
    }
}
