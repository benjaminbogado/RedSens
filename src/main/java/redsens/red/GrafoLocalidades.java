package redsens.red;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Red vial de M3: grafo no dirigido y ponderado (km) entre localidades.
 *
 * <p>Mantiene a la vez las dos representaciones pedidas:
 * <ul>
 *   <li><b>Lista de adyacencia</b>: para cada localidad, sus rutas salientes.
 *       Ocupa O(V + E) y es la que usan BFS, DFS y Dijkstra.</li>
 *   <li><b>Matriz de adyacencia</b>: {@code matriz[i][j]} = km de la ruta
 *       directa, o {@link #SIN_RUTA}. Ocupa O(V²), responde "¿hay ruta entre
 *       i y j?" en O(1) y es la base de Floyd.</li>
 * </ul>
 * Las dos se actualizan juntas en cada alta o baja, así nunca se contradicen.
 *
 * <p>El enunciado prohíbe HashMap/TreeMap en M3, así que el nombre de una
 * localidad se traduce a su índice con una búsqueda secuencial en la lista de
 * nombres: O(V), despreciable con 12 localidades. Los algoritmos trabajan
 * siempre con índices enteros; los nombres solo se usan al entrar y al mostrar.
 */
public class GrafoLocalidades {

    /** Valor de la matriz cuando no hay ruta directa. */
    public static final int SIN_RUTA = Integer.MAX_VALUE;

    private final List<String> nombres = new ArrayList<>();
    private final List<List<Ruta>> adyacencia = new ArrayList<>();
    private int[][] matriz = new int[0][0];
    private int cantidadRutas;

    // ------------------------------------------------------------------
    // Construcción
    // ------------------------------------------------------------------

    /** Agrega una localidad y devuelve su índice. No admite repetidas. */
    public int agregarLocalidad(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la localidad no puede estar vacío");
        }
        String limpio = nombre.trim();
        if (buscarIndice(limpio) != -1) {
            throw new IllegalArgumentException("La localidad ya existe: " + limpio);
        }
        int indice = nombres.size();
        nombres.add(limpio);
        adyacencia.add(new ArrayList<>());
        agrandarMatriz();
        return indice;
    }

    /** Agrega una ruta de doble mano entre dos localidades existentes. */
    public void agregarRuta(String origen, String destino, int km) {
        int a = indiceDe(origen);
        int b = indiceDe(destino);
        if (a == b) {
            throw new IllegalArgumentException("Una ruta no puede unir una localidad consigo misma");
        }
        if (km <= 0) {
            throw new IllegalArgumentException("La distancia debe ser positiva");
        }
        if (matriz[a][b] != SIN_RUTA) {
            throw new IllegalArgumentException("Ya existe una ruta entre " + origen + " y " + destino);
        }
        adyacencia.get(a).add(new Ruta(b, km));
        adyacencia.get(b).add(new Ruta(a, km));
        matriz[a][b] = km;
        matriz[b][a] = km;
        cantidadRutas++;
    }

    /**
     * Corta (elimina) la ruta directa entre dos localidades.
     *
     * @return los km de la ruta cortada
     */
    public int cortarRuta(String origen, String destino) {
        int a = indiceDe(origen);
        int b = indiceDe(destino);
        int km = matriz[a][b];
        if (km == SIN_RUTA) {
            throw new IllegalArgumentException("No existe ruta directa entre " + origen + " y " + destino);
        }
        adyacencia.get(a).removeIf(r -> r.destino() == b);
        adyacencia.get(b).removeIf(r -> r.destino() == a);
        matriz[a][b] = SIN_RUTA;
        matriz[b][a] = SIN_RUTA;
        cantidadRutas--;
        return km;
    }

    /** Copia independiente: sirve para simular un corte sin tocar el original. */
    public GrafoLocalidades copiar() {
        GrafoLocalidades copia = new GrafoLocalidades();
        for (String nombre : nombres) {
            copia.agregarLocalidad(nombre);
        }
        for (int i = 0; i < cantidadLocalidades(); i++) {
            for (Ruta r : adyacencia.get(i)) {
                if (i < r.destino()) {
                    copia.agregarRuta(nombres.get(i), nombres.get(r.destino()), r.km());
                }
            }
        }
        return copia;
    }

    private void agrandarMatriz() {
        int n = nombres.size();
        int[][] nueva = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i < matriz.length && j < matriz.length) {
                    nueva[i][j] = matriz[i][j];
                } else {
                    nueva[i][j] = (i == j) ? 0 : SIN_RUTA;
                }
            }
        }
        matriz = nueva;
    }

    // ------------------------------------------------------------------
    // Consultas
    // ------------------------------------------------------------------

    public int cantidadLocalidades() {
        return nombres.size();
    }

    public int cantidadRutas() {
        return cantidadRutas;
    }

    public boolean existeLocalidad(String nombre) {
        return nombre != null && buscarIndice(nombre.trim()) != -1;
    }

    public int indiceDe(String nombre) {
        int indice = nombre == null ? -1 : buscarIndice(nombre.trim());
        if (indice == -1) {
            throw new IllegalArgumentException("Localidad inexistente: " + nombre);
        }
        return indice;
    }

    /** Búsqueda secuencial del nombre; -1 si no está. */
    private int buscarIndice(String nombre) {
        for (int i = 0; i < nombres.size(); i++) {
            if (nombres.get(i).equals(nombre)) {
                return i;
            }
        }
        return -1;
    }

    public String nombreDe(int indice) {
        return nombres.get(indice);
    }

    public List<String> getNombres() {
        return Collections.unmodifiableList(nombres);
    }

    /** Rutas que salen de la localidad (lista de adyacencia, solo lectura). */
    public List<Ruta> rutasDesde(int indice) {
        return Collections.unmodifiableList(adyacencia.get(indice));
    }

    /** km de la ruta directa i–j, o {@link #SIN_RUTA} (matriz de adyacencia, O(1)). */
    public int distanciaDirecta(int i, int j) {
        return matriz[i][j];
    }

    /** Copia de la matriz de adyacencia (para que Floyd no altere el grafo). */
    public int[][] copiaMatriz() {
        int[][] copia = new int[matriz.length][];
        for (int i = 0; i < matriz.length; i++) {
            copia[i] = matriz[i].clone();
        }
        return copia;
    }

    // ------------------------------------------------------------------
    // Presentación
    // ------------------------------------------------------------------

    /** Texto de la lista de adyacencia, una localidad por línea. */
    public String textoListaAdyacencia() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nombres.size(); i++) {
            sb.append(String.format("[%2d] %-17s -> ", i, nombres.get(i)));
            List<Ruta> rutas = adyacencia.get(i);
            for (int k = 0; k < rutas.size(); k++) {
                Ruta r = rutas.get(k);
                if (k > 0) {
                    sb.append(", ");
                }
                sb.append(nombres.get(r.destino())).append(" (").append(r.km()).append(" km)");
            }
            sb.append(System.lineSeparator());
        }
        return sb.toString();
    }

    /** Texto de la matriz de adyacencia; "-" indica que no hay ruta directa. */
    public String textoMatrizAdyacencia() {
        StringBuilder sb = new StringBuilder("     ");
        for (int j = 0; j < nombres.size(); j++) {
            sb.append(String.format("%5d", j));
        }
        sb.append(System.lineSeparator());
        for (int i = 0; i < nombres.size(); i++) {
            sb.append(String.format("[%2d] ", i));
            for (int j = 0; j < nombres.size(); j++) {
                sb.append(String.format("%5s", matriz[i][j] == SIN_RUTA ? "-" : Integer.toString(matriz[i][j])));
            }
            sb.append(System.lineSeparator());
        }
        return sb.toString();
    }
}
