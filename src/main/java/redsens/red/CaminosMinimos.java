package redsens.red;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Caminos mínimos de la red vial.
 *
 * <ul>
 *   <li><b>Dijkstra</b> (un origen, todos los destinos): en cada paso elige
 *       con una búsqueda lineal el vértice no definitivo de menor distancia y
 *       relaja sus rutas (lista de adyacencia). O(V² + E): con 12 vértices es
 *       más simple que una cola de prioridad y no usa estructuras de librería.</li>
 *   <li><b>Floyd</b> (todos contra todos): programación dinámica sobre la
 *       matriz de adyacencia. O(V³) en tiempo y O(V²) en memoria.</li>
 * </ul>
 * Las distancias son km (todas positivas), condición que Dijkstra necesita.
 */
public final class CaminosMinimos {

    /** Distancia de una localidad inalcanzable. */
    public static final int INFINITO = Integer.MAX_VALUE;

    private CaminosMinimos() {
    }

    // ------------------------------------------------------------------
    // Dijkstra
    // ------------------------------------------------------------------

    public static ResultadoDijkstra dijkstra(GrafoLocalidades grafo, String origen) {
        int n = grafo.cantidadLocalidades();
        int inicio = grafo.indiceDe(origen);
        int[] distancia = new int[n];
        int[] previo = new int[n];
        boolean[] definitivo = new boolean[n];
        for (int i = 0; i < n; i++) {
            distancia[i] = INFINITO;
            previo[i] = -1;
        }
        distancia[inicio] = 0;

        for (int paso = 0; paso < n; paso++) {
            // 1) Vértice no definitivo con menor distancia conocida.
            int u = -1;
            for (int i = 0; i < n; i++) {
                if (!definitivo[i] && distancia[i] != INFINITO && (u == -1 || distancia[i] < distancia[u])) {
                    u = i;
                }
            }
            if (u == -1) {
                break; // lo que queda no es alcanzable desde el origen
            }
            definitivo[u] = true; // su distancia ya no puede mejorar (pesos positivos)

            // 2) Relajación de las rutas que salen de u.
            for (Ruta r : grafo.rutasDesde(u)) {
                int v = r.destino();
                int candidata = distancia[u] + r.km();
                if (!definitivo[v] && candidata < distancia[v]) {
                    distancia[v] = candidata;
                    previo[v] = u;
                }
            }
        }
        return new ResultadoDijkstra(grafo, inicio, distancia, previo);
    }

    /** Resultado de Dijkstra: distancias desde el origen y árbol de caminos. */
    public static final class ResultadoDijkstra {
        private final GrafoLocalidades grafo;
        private final int origen;
        private final int[] distancia;
        private final int[] previo;

        private ResultadoDijkstra(GrafoLocalidades grafo, int origen, int[] distancia, int[] previo) {
            this.grafo = grafo;
            this.origen = origen;
            this.distancia = distancia;
            this.previo = previo;
        }

        public String getOrigen() {
            return grafo.nombreDe(origen);
        }

        /** km hasta el destino, o {@link #INFINITO} si no hay camino. */
        public int distanciaA(String destino) {
            return distancia[grafo.indiceDe(destino)];
        }

        public boolean hayCamino(String destino) {
            return distanciaA(destino) != INFINITO;
        }

        /** Localidades desde el origen hasta el destino; vacío si no hay camino. */
        public List<String> caminoA(String destino) {
            int actual = grafo.indiceDe(destino);
            if (distancia[actual] == INFINITO) {
                return List.of();
            }
            List<String> camino = new ArrayList<>();
            while (actual != -1) {
                camino.add(grafo.nombreDe(actual));
                actual = previo[actual];
            }
            Collections.reverse(camino);
            return camino;
        }
    }

    // ------------------------------------------------------------------
    // Floyd
    // ------------------------------------------------------------------

    public static ResultadoFloyd floyd(GrafoLocalidades grafo) {
        int n = grafo.cantidadLocalidades();
        int[][] d = grafo.copiaMatriz(); // la diagonal ya es 0
        int[][] siguiente = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                siguiente[i][j] = (i == j || d[i][j] != GrafoLocalidades.SIN_RUTA) ? j : -1;
            }
        }

        // d[i][j] pasa a ser el mejor camino que usa solo los intermedios 0..k.
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                if (d[i][k] == INFINITO) {
                    continue;
                }
                for (int j = 0; j < n; j++) {
                    if (d[k][j] == INFINITO) {
                        continue;
                    }
                    int porK = d[i][k] + d[k][j];
                    if (porK < d[i][j]) {
                        d[i][j] = porK;
                        siguiente[i][j] = siguiente[i][k];
                    }
                }
            }
        }
        return new ResultadoFloyd(grafo, d, siguiente);
    }

    /** Resultado de Floyd: matriz de distancias mínimas entre todos los pares. */
    public static final class ResultadoFloyd {
        private final GrafoLocalidades grafo;
        private final int[][] distancia;
        private final int[][] siguiente;

        private ResultadoFloyd(GrafoLocalidades grafo, int[][] distancia, int[][] siguiente) {
            this.grafo = grafo;
            this.distancia = distancia;
            this.siguiente = siguiente;
        }

        public int distancia(String origen, String destino) {
            return distancia[grafo.indiceDe(origen)][grafo.indiceDe(destino)];
        }

        int distancia(int i, int j) {
            return distancia[i][j];
        }

        public List<String> camino(String origen, String destino) {
            return camino(grafo.indiceDe(origen), grafo.indiceDe(destino));
        }

        List<String> camino(int i, int j) {
            if (siguiente[i][j] == -1) {
                return List.of();
            }
            List<String> camino = new ArrayList<>();
            camino.add(grafo.nombreDe(i));
            while (i != j) {
                i = siguiente[i][j];
                camino.add(grafo.nombreDe(i));
            }
            return camino;
        }

        /** Matriz de distancias mínimas en texto; "inf" si no hay camino. */
        public String textoMatriz() {
            int n = grafo.cantidadLocalidades();
            StringBuilder sb = new StringBuilder("     ");
            for (int j = 0; j < n; j++) {
                sb.append(String.format("%5d", j));
            }
            sb.append(System.lineSeparator());
            for (int i = 0; i < n; i++) {
                sb.append(String.format("[%2d] ", i));
                for (int j = 0; j < n; j++) {
                    sb.append(String.format("%5s", distancia[i][j] == INFINITO ? "inf" : Integer.toString(distancia[i][j])));
                }
                sb.append(System.lineSeparator());
            }
            return sb.toString();
        }
    }
}
