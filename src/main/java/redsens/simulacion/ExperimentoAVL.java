package redsens.simulacion;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import redsens.indice.ArbolAVL;
import redsens.modelo.Estacion;

public class ExperimentoAVL {

    // Conserva el resultado para que las búsquedas sean observables.
    private static volatile long control;

    public static void main(String[] args) throws IOException {

        List<String> resultados = new ArrayList<>();
        resultados.add(
            "estructura,cantidad,orden,altura,busquedas,"
            + "mediana_lote_ns,promedio_ns_por_busqueda"
        );

        int[] cantidades = {1000, 5000, 10000};

        System.out.println("Experimento AVL - Grupo 3");
        System.out.println(
            "Tiempo: mediana de 7 lotes de búsquedas exitosas\n"
        );

        for (int cantidad : cantidades) {

            List<Estacion> aleatorias =
                GeneradorDatos.generarEstaciones(cantidad);

            List<Estacion> ordenadas =
                new ArrayList<>(aleatorias);

            ordenadas.sort(
                Comparator.comparingInt(Estacion::getNumero)
            );

            // Mismas consultas y mismo orden para ambos árboles.
            int[] consultas = new int[cantidad];

            for (int i = 0; i < cantidad; i++) {
                consultas[i] = aleatorias.get(i).getNumero();
            }

            ejecutar(ordenadas, consultas, "ascendente", resultados);
            ejecutar(aleatorias, consultas, "aleatorio", resultados);
        }
System.out.println("Control de búsquedas: " + control);
        Path archivo = Path.of("datos", "rendimiento_avl.csv");
        Files.createDirectories(archivo.toAbsolutePath().getParent());
        Files.write(archivo, resultados, StandardCharsets.UTF_8);

        System.out.println(
            "\nResultados guardados en: " + archivo.toAbsolutePath()
        );
    }

    private static void ejecutar(
            List<Estacion> estaciones,
            int[] consultas,
            String orden,
            List<String> resultados) {

        ArbolAVL arbol = new ArbolAVL();

        // La generación y la inserción quedan fuera del tiempo
        // medido: aquí queremos medir solamente las búsquedas.
        for (Estacion estacion : estaciones) {
            arbol.insertar(estacion);
        }

        // Comprueba que todas las estaciones se puedan encontrar.
        for (int numero : consultas) {
            Estacion encontrada = arbol.buscar(numero);

            if (encontrada == null || encontrada.getNumero() != numero) {
                throw new IllegalStateException(
                    "Búsqueda incorrecta: " + numero
                );
            }
        }

        // Calentamiento previo de la JVM.
        for (int i = 0; i < 5; i++) {
            control = buscarTodas(arbol, consultas);
        }

        long[] tiempos = new long[7];

        for (int i = 0; i < tiempos.length; i++) {

            MedidorRendimiento.Resultado<Long> resultado =
                MedidorRendimiento.medir(
                    () -> buscarTodas(arbol, consultas)
                );

            tiempos[i] = resultado.nanosegundos();
            control = resultado.valor();
        }

        long mediana = MedidorRendimiento.mediana(tiempos);
        double promedio = mediana / (double) consultas.length;
        int altura = arbol.altura();

        System.out.printf(
            Locale.ROOT,
            "N=%d | %s | Altura=%d | ns/búsqueda=%.2f%n",
            estaciones.size(), orden, altura, promedio
        );

        resultados.add(String.format(
            Locale.ROOT,
            "AVL,%d,%s,%d,%d,%d,%.2f",
            estaciones.size(),
            orden,
            altura,
            consultas.length,
            mediana,
            promedio
        ));
    }

    private static long buscarTodas(
            ArbolAVL arbol, int[] consultas) {

        long suma = 0;

        for (int numero : consultas) {
            Estacion encontrada = arbol.buscar(numero);

            if (encontrada != null) {
                suma += encontrada.getNumero();
            }
        }

        return suma;
    }
}