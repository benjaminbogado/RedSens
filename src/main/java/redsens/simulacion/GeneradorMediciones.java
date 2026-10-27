package redsens.simulacion;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import redsens.modelo.Estacion;

public class GeneradorMediciones {

    public static void generarArchivo(
            List<Estacion> estaciones,
            int cantidadLineas,
            LocalDate fecha,
            long semilla,
            Path destino) throws IOException {

        if (estaciones == null || estaciones.isEmpty()) {
            throw new IllegalArgumentException(
                "Debe existir al menos una estación"
            );
        }

        if (cantidadLineas <= 0 || fecha == null || destino == null) {
            throw new IllegalArgumentException(
                "Cantidad, fecha o destino inválidos"
            );
        }

        for (Estacion estacion : estaciones) {
            if (estacion == null) {
                throw new IllegalArgumentException(
                    "La lista contiene una estación nula"
                );
            }
        }

        Random random = new Random(semilla);

        Path carpeta = destino.toAbsolutePath().getParent();
        Files.createDirectories(carpeta);

        try (BufferedWriter escritor = Files.newBufferedWriter(
                destino, StandardCharsets.UTF_8)) {

            for (int i = 0; i < cantidadLineas; i++) {

                // Distribuye las mediciones entre las estaciones.
                Estacion estacion =
                    estaciones.get(i % estaciones.size());

                // Distribuye los registros a lo largo del mismo día.
                int minuto = (int) (i * 1440L / cantidadLineas);
                int hora = minuto / 60;
                int minutos = minuto % 60;

                // Rangos elegidos para esta simulación.
                double temperatura = -5 + random.nextInt(501) / 10.0;
                int humedad = random.nextInt(101);
                double precipitacion = random.nextInt(501) / 10.0;

                String linea = String.format(
                    Locale.ROOT,
                    "%s;%s;%02d:%02d;T=%.1f;H=%d;P=%.1f;BAT=%d",
                    estacion.getCodigo(),
                    fecha,
                    hora,
                    minutos,
                    temperatura,
                    humedad,
                    precipitacion,
                    estacion.getBateria()
                );

                escritor.write(linea);
                escritor.write('\n');
            }
        }
    }
}