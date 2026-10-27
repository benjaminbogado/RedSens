package redsens.simulacion;

import java.util.Arrays;
import java.util.function.Supplier;

public class MedidorRendimiento {

    public record Resultado<T>(
            T valor,
            long nanosegundos) {
    }

    /**
     * Ejecuta una tarea y devuelve su resultado y duración.
     * La preparación de los datos debe hacerse antes de llamar.
     */
    public static <T> Resultado<T> medir(Supplier<T> tarea) {

        if (tarea == null) {
            throw new IllegalArgumentException(
                "La tarea no puede ser nula"
            );
        }

        long inicio = System.nanoTime();

        T valor = tarea.get();

        long duracion = System.nanoTime() - inicio;

        return new Resultado<>(valor, duracion);
    }

    /**
     * Calcula la mediana de varias duraciones.
     * Evita que un tiempo excepcional distorsione el resultado.
     */
    public static long mediana(long[] tiempos) {

        if (tiempos == null || tiempos.length == 0) {
            throw new IllegalArgumentException(
                "Debe existir al menos una medición"
            );
        }

        long[] copia = tiempos.clone();

        for (long tiempo : copia) {
            if (tiempo < 0) {
                throw new IllegalArgumentException(
                    "Las duraciones no pueden ser negativas"
                );
            }
        }

        Arrays.sort(copia);

        int mitad = copia.length / 2;

        if (copia.length % 2 != 0) {
            return copia[mitad];
        }

        long menor = copia[mitad - 1];
        long mayor = copia[mitad];

        return menor + (mayor - menor) / 2;
    }
}