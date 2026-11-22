package redsens.red;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * BFS y DFS sobre la red vial.
 *
 * <p>Ambos se implementan con la lista de adyacencia (O(V + E)) y también con
 * la matriz de adyacencia (O(V²)) para poder mostrar la diferencia entre las
 * dos representaciones. Con la misma red y el mismo origen, la versión con
 * lista y la versión con matriz visitan en el mismo orden porque los vecinos
 * se recorren por índice ascendente en ambos casos.
 */
public final class Recorridos {

    private Recorridos() {
    }

    // ------------------------------------------------------------------
    // BFS: por niveles, con cola
    // ------------------------------------------------------------------

    public static List<String> bfs(GrafoLocalidades grafo, String origen) {
        int n = grafo.cantidadLocalidades();
        boolean[] visitado = new boolean[n];
        List<String> orden = new ArrayList<>();
        Deque<Integer> cola = new ArrayDeque<>();

        int inicio = grafo.indiceDe(origen);
        visitado[inicio] = true;
        cola.addLast(inicio);
        while (!cola.isEmpty()) {
            int actual = cola.removeFirst();
            orden.add(grafo.nombreDe(actual));
            for (int vecino : vecinosOrdenados(grafo, actual)) {
                if (!visitado[vecino]) {
                    visitado[vecino] = true;
                    cola.addLast(vecino);
                }
            }
        }
        return orden;
    }

    /**
     * BFS por niveles: el elemento k de la lista son las localidades a las que
     * se llega con k tramos como mínimo (nivel 0 = el origen). Es lo que pide
     * el enunciado para "alcanzables en 1, 2 y 3 tramos".
     */
    public static List<List<String>> nivelesBfs(GrafoLocalidades grafo, String origen) {
        int n = grafo.cantidadLocalidades();
        int[] nivel = new int[n];
        for (int i = 0; i < n; i++) {
            nivel[i] = -1; // -1 = no visitado
        }
        List<List<String>> niveles = new ArrayList<>();
        Deque<Integer> cola = new ArrayDeque<>();

        int inicio = grafo.indiceDe(origen);
        nivel[inicio] = 0;
        cola.addLast(inicio);
        while (!cola.isEmpty()) {
            int actual = cola.removeFirst();
            if (niveles.size() == nivel[actual]) {
                niveles.add(new ArrayList<>());
            }
            niveles.get(nivel[actual]).add(grafo.nombreDe(actual));
            for (int vecino : vecinosOrdenados(grafo, actual)) {
                if (nivel[vecino] == -1) {
                    nivel[vecino] = nivel[actual] + 1;
                    cola.addLast(vecino);
                }
            }
        }
        return niveles;
    }

    public static List<String> bfsMatriz(GrafoLocalidades grafo, String origen) {
        int n = grafo.cantidadLocalidades();
        boolean[] visitado = new boolean[n];
        List<String> orden = new ArrayList<>();
        Deque<Integer> cola = new ArrayDeque<>();

        int inicio = grafo.indiceDe(origen);
        visitado[inicio] = true;
        cola.addLast(inicio);
        while (!cola.isEmpty()) {
            int actual = cola.removeFirst();
            orden.add(grafo.nombreDe(actual));
            for (int j = 0; j < n; j++) { // recorre toda la fila: O(V) por vértice
                if (j != actual && grafo.distanciaDirecta(actual, j) != GrafoLocalidades.SIN_RUTA && !visitado[j]) {
                    visitado[j] = true;
                    cola.addLast(j);
                }
            }
        }
        return orden;
    }

    // ------------------------------------------------------------------
    // DFS: en profundidad, recursivo
    // ------------------------------------------------------------------

    public static List<String> dfs(GrafoLocalidades grafo, String origen) {
        boolean[] visitado = new boolean[grafo.cantidadLocalidades()];
        List<String> orden = new ArrayList<>();
        dfsLista(grafo, grafo.indiceDe(origen), visitado, orden);
        return orden;
    }

    private static void dfsLista(GrafoLocalidades grafo, int actual, boolean[] visitado, List<String> orden) {
        visitado[actual] = true;
        orden.add(grafo.nombreDe(actual));
        for (int vecino : vecinosOrdenados(grafo, actual)) {
            if (!visitado[vecino]) {
                dfsLista(grafo, vecino, visitado, orden);
            }
        }
    }

    public static List<String> dfsMatriz(GrafoLocalidades grafo, String origen) {
        boolean[] visitado = new boolean[grafo.cantidadLocalidades()];
        List<String> orden = new ArrayList<>();
        dfsMatriz(grafo, grafo.indiceDe(origen), visitado, orden);
        return orden;
    }

    private static void dfsMatriz(GrafoLocalidades grafo, int actual, boolean[] visitado, List<String> orden) {
        visitado[actual] = true;
        orden.add(grafo.nombreDe(actual));
        for (int j = 0; j < grafo.cantidadLocalidades(); j++) {
            if (j != actual && grafo.distanciaDirecta(actual, j) != GrafoLocalidades.SIN_RUTA && !visitado[j]) {
                dfsMatriz(grafo, j, visitado, orden);
            }
        }
    }

    // ------------------------------------------------------------------
    // Utilidades
    // ------------------------------------------------------------------

    /**
     * Conexidad con DFS (como pide el enunciado): en un grafo no dirigido, si
     * un DFS desde cualquier vértice visita a todos, la red es conexa.
     */
    public static boolean esConexo(GrafoLocalidades grafo, String origen) {
        return dfs(grafo, origen).size() == grafo.cantidadLocalidades();
    }

    /** Localidades que el DFS desde el origen no alcanza (quedan aisladas de él). */
    public static List<String> inalcanzables(GrafoLocalidades grafo, String origen) {
        List<String> alcanzadas = dfs(grafo, origen);
        List<String> faltan = new ArrayList<>();
        for (String nombre : grafo.getNombres()) {
            if (!alcanzadas.contains(nombre)) {
                faltan.add(nombre);
            }
        }
        return faltan;
    }

    /** Vecinos por índice ascendente, para que el orden no dependa del archivo. */
    private static List<Integer> vecinosOrdenados(GrafoLocalidades grafo, int indice) {
        List<Integer> vecinos = new ArrayList<>();
        for (Ruta r : grafo.rutasDesde(indice)) {
            vecinos.add(r.destino());
        }
        vecinos.sort(null);
        return vecinos;
    }
}
