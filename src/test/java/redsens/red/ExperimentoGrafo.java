package redsens.red;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import redsens.indice.ArbolAVL;
import redsens.modelo.Estacion;
import redsens.simulacion.GeneradorDatos;

/**
 * Corrida completa de M3 siguiendo los puntos del enunciado, con la base del
 * grupo 3 (San Francisco) y Córdoba como caso de control.
 * Su salida se guarda en resultados/resultados-m3.txt.
 *
 * Uso: java -cp out redsens.red.ExperimentoGrafo [archivo-de-red]
 */
public class ExperimentoGrafo {

    private static final String CONTROL = "Córdoba";

    public static void main(String[] args) throws IOException {
        Path archivo = args.length > 0 ? Path.of(args[0]) : CargadorGrafo.ARCHIVO_POR_DEFECTO;
        GrafoLocalidades red = CargadorGrafo.cargar(archivo);
        String base = PlanVisitas.BASE_GRUPO;

        titulo("0. Red cargada desde " + archivo);
        System.out.println(red.cantidadLocalidades() + " localidades, " + red.cantidadRutas() + " tramos (doble mano)");

        titulo("1. Representaciones");
        System.out.println("Lista de adyacencia:");
        System.out.print(red.textoListaAdyacencia());
        System.out.println();
        System.out.println("Matriz de adyacencia (km, '-' = sin ruta directa):");
        System.out.print(red.textoMatrizAdyacencia());
        int n = red.cantidadLocalidades();
        System.out.printf("Celdas de la matriz: %d, con ruta: %d (%.0f %%). Entradas en la lista: %d.%n",
                n * n, 2 * red.cantidadRutas(), 100.0 * 2 * red.cantidadRutas() / (n * n), 2 * red.cantidadRutas());

        titulo("2. BFS por niveles desde " + base);
        imprimirNiveles(Recorridos.nivelesBfs(red, base));
        System.out.println("Orden BFS (lista):  " + String.join(" > ", Recorridos.bfs(red, base)));
        System.out.println("Orden BFS (matriz): " + String.join(" > ", Recorridos.bfsMatriz(red, base)));

        titulo("3. DFS: conexidad y corte de rutas");
        System.out.println("Orden DFS (lista):  " + String.join(" > ", Recorridos.dfs(red, base)));
        System.out.println("Orden DFS (matriz): " + String.join(" > ", Recorridos.dfsMatriz(red, base)));
        System.out.println("¿Red conexa?: " + (Recorridos.esConexo(red, base) ? "sí" : "no"));
        System.out.println();
        CorteRuta corte = CorteRuta.simular(red, base, "Córdoba", "San Francisco");
        System.out.print(corte.texto());
        System.out.println();
        List<String> puentes = CorteRuta.rutasQueDesconectan(red);
        System.out.println("Cortando de a una cada una de las " + red.cantidadRutas() + " rutas: "
                + (puentes.isEmpty() ? "ninguna desconecta la red." : "desconectan " + puentes));
        System.out.println();
        GrafoLocalidades doble = red.copiar();
        doble.cortarRuta("Córdoba", "San Francisco");
        CorteRuta dobleCorte = CorteRuta.simular(doble, base, "Villa María", "San Francisco");
        System.out.println("Con Córdoba – San Francisco ya cortada:");
        System.out.print(dobleCorte.texto());

        titulo("4. Dijkstra");
        imprimirDijkstra(red, CONTROL);
        System.out.println();
        imprimirDijkstra(red, base);

        titulo("5. Floyd: distancias mínimas entre todos los pares");
        CaminosMinimos.ResultadoFloyd floyd = CaminosMinimos.floyd(red);
        for (int i = 0; i < n; i++) {
            System.out.printf("[%2d] %s%n", i, red.nombreDe(i));
        }
        System.out.print(floyd.textoMatriz());
        System.out.println("Floyd = Dijkstra desde cada origen: " + (floydIgualDijkstra(red, floyd) ? "sí" : "NO"));

        titulo("6. Plan de visitas desde " + base + " (localidades con estaciones críticas del M2)");
        for (int cantidad : new int[]{30, 1000}) {
            ArbolAVL avl = new ArbolAVL();
            for (Estacion e : GeneradorDatos.generarEstaciones(cantidad)) {
                avl.insertar(e);
            }
            List<Estacion> criticas = avl.listarCriticas();
            List<String> localidades = PlanVisitas.localidadesDe(criticas);
            PlanVisitas plan = PlanVisitas.planificar(red, base, localidades);
            System.out.println();
            System.out.printf("%d estaciones (semilla %d): %d críticas en %d localidades%n",
                    cantidad, GeneradorDatos.SEMILLA_GRUPO, criticas.size(), localidades.size());
            System.out.println("Localidades a visitar: " + String.join(", ", localidades));
            System.out.print(plan.texto());
            int optimo = circuitoOptimo(red, base, localidades, floyd);
            System.out.printf("Óptimo exacto (Held-Karp, solo referencia): %d km. Exceso del vecino más cercano: %d km (%.1f %%)%n",
                    optimo, plan.kmTotales() - optimo, 100.0 * (plan.kmTotales() - optimo) / optimo);
        }
    }

    // ------------------------------------------------------------------

    private static void imprimirNiveles(List<List<String>> niveles) {
        for (int k = 0; k < niveles.size(); k++) {
            String etiqueta = k == 0 ? "origen" : (k == 1 ? "1 tramo" : k + " tramos");
            System.out.printf("Nivel %d (%s): %s%n", k, etiqueta, String.join(", ", niveles.get(k)));
        }
    }

    private static void imprimirDijkstra(GrafoLocalidades red, String origen) {
        CaminosMinimos.ResultadoDijkstra dij = CaminosMinimos.dijkstra(red, origen);
        System.out.println("Desde " + origen + (origen.equals(CONTROL) ? " (caso de control)" : " (base del grupo)") + ":");
        for (String destino : red.getNombres()) {
            System.out.printf("  %-17s %4d km  %s%n", destino, dij.distanciaA(destino),
                    String.join(" > ", dij.caminoA(destino)));
        }
    }

    private static boolean floydIgualDijkstra(GrafoLocalidades red, CaminosMinimos.ResultadoFloyd floyd) {
        for (String origen : red.getNombres()) {
            CaminosMinimos.ResultadoDijkstra dij = CaminosMinimos.dijkstra(red, origen);
            for (String destino : red.getNombres()) {
                if (floyd.distancia(origen, destino) != dij.distanciaA(destino)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Circuito más corto que sale de la base, pasa por todas las localidades
     * indicadas y vuelve (Held-Karp, O(2^k · k²)). Solo sirve para redes chicas
     * y se usa únicamente para medir qué tan lejos queda la heurística.
     */
    private static int circuitoOptimo(GrafoLocalidades red, String base, List<String> aVisitar,
                                      CaminosMinimos.ResultadoFloyd floyd) {
        int[] otros = aVisitar.stream().filter(l -> !l.equals(base)).mapToInt(red::indiceDe).toArray();
        int b = red.indiceDe(base);
        int m = otros.length;
        if (m == 0) {
            return 0;
        }
        long inf = Long.MAX_VALUE / 4;
        long[][] mejor = new long[1 << m][m];
        for (long[] fila : mejor) {
            Arrays.fill(fila, inf);
        }
        for (int j = 0; j < m; j++) {
            mejor[1 << j][j] = d(red, floyd, b, otros[j]);
        }
        for (int conjunto = 1; conjunto < (1 << m); conjunto++) {
            for (int ultimo = 0; ultimo < m; ultimo++) {
                if ((conjunto & (1 << ultimo)) == 0 || mejor[conjunto][ultimo] >= inf) {
                    continue;
                }
                for (int sig = 0; sig < m; sig++) {
                    if ((conjunto & (1 << sig)) == 0) {
                        int nuevo = conjunto | (1 << sig);
                        mejor[nuevo][sig] = Math.min(mejor[nuevo][sig],
                                mejor[conjunto][ultimo] + d(red, floyd, otros[ultimo], otros[sig]));
                    }
                }
            }
        }
        long resultado = inf;
        for (int j = 0; j < m; j++) {
            resultado = Math.min(resultado, mejor[(1 << m) - 1][j] + d(red, floyd, otros[j], b));
        }
        return (int) resultado;
    }

    private static long d(GrafoLocalidades red, CaminosMinimos.ResultadoFloyd floyd, int i, int j) {
        return floyd.distancia(red.nombreDe(i), red.nombreDe(j));
    }

    private static void titulo(String texto) {
        System.out.println();
        System.out.println("=== " + texto + " ===");
    }
}
