package redsens.red;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/**
 * Carga la red vial desde un archivo de texto UTF-8.
 *
 * <pre>
 * # comentario
 * localidad;San Francisco
 * ruta;San Francisco;Córdoba;205
 * </pre>
 * Se puede declarar una localidad antes de usarla en una ruta; si una ruta
 * nombra una localidad no declarada, se crea automáticamente.
 */
public final class CargadorGrafo {

    /** Archivo por defecto, relativo a la raíz del repositorio. */
    public static final Path ARCHIVO_POR_DEFECTO = Path.of("datos", "red_vial.txt");

    private CargadorGrafo() {
    }

    public static GrafoLocalidades cargar(Path archivo) throws IOException {
        return desdeLineas(Files.readAllLines(archivo, StandardCharsets.UTF_8));
    }

    public static GrafoLocalidades cargarPorDefecto() throws IOException {
        return cargar(ARCHIVO_POR_DEFECTO);
    }

    /** Separado de la lectura del archivo para poder probarlo sin disco. */
    public static GrafoLocalidades desdeLineas(List<String> lineas) {
        GrafoLocalidades grafo = new GrafoLocalidades();
        for (int i = 0; i < lineas.size(); i++) {
            String linea = lineas.get(i).strip();
            if (i == 0 && linea.startsWith("\uFEFF")) {
                linea = linea.substring(1); // BOM de algunos editores de Windows
            }
            if (linea.isEmpty() || linea.startsWith("#")) {
                continue;
            }
            String[] campos = linea.split(";", -1);
            try {
                switch (campos[0].trim().toLowerCase(Locale.ROOT)) {
                    case "localidad" -> {
                        exigirCampos(campos, 2);
                        grafo.agregarLocalidad(campos[1]);
                    }
                    case "ruta" -> {
                        exigirCampos(campos, 4);
                        String origen = campos[1].trim();
                        String destino = campos[2].trim();
                        if (!grafo.existeLocalidad(origen)) {
                            grafo.agregarLocalidad(origen);
                        }
                        if (!grafo.existeLocalidad(destino)) {
                            grafo.agregarLocalidad(destino);
                        }
                        grafo.agregarRuta(origen, destino, Long.parseLong(campos[3].trim()));
                    }
                    default -> throw new IllegalArgumentException(
                            "tipo de línea desconocido '" + campos[0] + "' (se espera localidad o ruta)");
                }
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("línea " + (i + 1) + ": " + e.getMessage(), e);
            }
        }
        return grafo;
    }

    private static void exigirCampos(String[] campos, int cantidad) {
        if (campos.length != cantidad) {
            throw new IllegalArgumentException("se esperaban " + cantidad + " campos separados por ';'");
        }
    }
}
