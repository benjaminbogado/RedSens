package redsens.simulacion;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import redsens.modelo.Estacion;

public class ComprobadorMediciones {

    public static void main(String[] args) throws IOException {

        List<Estacion> estaciones =
            GeneradorDatos.generarEstaciones(1000);

        LocalDate fecha = LocalDate.of(2026, 10, 13);
        Path archivo = Path.of("datos", "mediciones_grupo3.txt");
        Path repetido = Path.of("datos", "mediciones_verificacion.txt");

        GeneradorMediciones.generarArchivo(
            estaciones, 5000, fecha, 3026L, archivo
        );

        GeneradorMediciones.generarArchivo(
            estaciones, 5000, fecha, 3026L, repetido
        );

        List<String> lineas =
            Files.readAllLines(archivo, StandardCharsets.UTF_8);

        if (lineas.size() != 5000) {
            throw new IllegalStateException(
                "El archivo no tiene 5000 líneas"
            );
        }

        if (Files.mismatch(archivo, repetido) != -1) {
            throw new IllegalStateException(
                "Los archivos generados no son idénticos"
            );
        }

        String formato =
            "CBA-[0-9]{4};2026-10-13;[0-9]{2}:[0-9]{2};"
            + "T=-?[0-9]+\\.[0-9];H=[0-9]+;"
            + "P=[0-9]+\\.[0-9];BAT=[0-9]+";

        for (String linea : lineas) {
            if (!linea.matches(formato)) {
                throw new IllegalStateException(
                    "Formato incorrecto: " + linea
                );
            }
        }

        Files.delete(repetido);

        System.out.println("Líneas generadas: " + lineas.size());
        System.out.println("Formato de todas las líneas: OK");
        System.out.println("Archivo reproducible: OK");
        System.out.println("Ubicación: " + archivo.toAbsolutePath());
        System.out.println("\nPrimera medición:");
        System.out.println(lineas.get(0));
    }
}