package redsens.simulacion;

import java.util.List;
import java.util.Locale;
import redsens.modelo.Estacion;

public class ComprobadorM6 {

    public static void main(String[] args) {

        List<Estacion> estaciones =
            GeneradorDatos.generarEstaciones(10000, 3026L);

        List<Estacion> repetidas =
            GeneradorDatos.generarEstaciones(10000, 3026L);

        List<Estacion> otraSemilla =
            GeneradorDatos.generarEstaciones(10000, 3027L);

        boolean[] usados =
            new boolean[GeneradorDatos.MAX_ESTACIONES];

        int[] frecuencias = new int[101];

        for (Estacion estacion : estaciones) {
            int numero = estacion.getNumero();

            comprobar(!usados[numero], "Código repetido");
            usados[numero] = true;

            frecuencias[estacion.getBateria()]++;
        }

        comprobar(estaciones.size() == 10000,
            "Cantidad incorrecta");

        comprobar(secuenciasIguales(estaciones, repetidas),
            "La misma semilla produjo datos diferentes");

        comprobar(!secuenciasIguales(estaciones, otraSemilla),
            "Las semillas probadas produjeron secuencias iguales");

        System.out.println("Grupo 3 - Semilla 3026");
        System.out.println("Estaciones generadas: 10000");
        System.out.println("Códigos únicos: OK");
        System.out.println("Misma semilla, mismos datos: OK");
        System.out.println("Semillas 3026 y 3027, datos distintos: OK");

        System.out.println("\nDistribución de baterías:");
        System.out.println("Intervalo | Observadas | Esperadas");

        // Los primeros diez intervalos contienen 10 valores cada uno.
        double esperado = estaciones.size() * 10.0 / 101;

        for (int inicio = 0; inicio < 100; inicio += 10) {
            int cantidad = 0;

            for (int valor = inicio; valor < inicio + 10; valor++) {
                cantidad += frecuencias[valor];
            }

            System.out.printf(
                Locale.ROOT,
                "%2d-%2d    | %4d       | %.2f%n",
                inicio, inicio + 9, cantidad, esperado
            );
        }

        // El 100 se muestra aparte porque es un único valor.
        System.out.printf(
            Locale.ROOT,
            "100      | %4d       | %.2f%n",
            frecuencias[100], estaciones.size() / 101.0
        );

        System.out.println(
            "\nLas frecuencias deben aproximarse a las esperadas;"
        );
        System.out.println(
            "no tienen que ser exactamente iguales."
        );
    }

    public static boolean secuenciasIguales(
            List<Estacion> primera, List<Estacion> segunda) {

        if (primera.size() != segunda.size()) {
            return false;
        }

        for (int i = 0; i < primera.size(); i++) {
            Estacion a = primera.get(i);
            Estacion b = segunda.get(i);

            if (!a.getCodigo().equals(b.getCodigo())
                    || a.getNumero() != b.getNumero()
                    || !a.getNombre().equals(b.getNombre())
                    || !a.getLocalidad().equals(b.getLocalidad())
                    || a.getTipo() != b.getTipo()
                    || a.getBateria() != b.getBateria()
                    || a.getDiasSinVisita() != b.getDiasSinVisita()) {
                return false;
            }
        }

        return true;
    }

    private static void comprobar(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new IllegalStateException(mensaje);
        }
    }
}