package redsens.red;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import redsens.modelo.Estacion;

/**
 * Plan de visitas de la cuadrilla con la heurística del vecino más cercano.
 *
 * <p>Sale de la base (San Francisco para el grupo 3), va siempre a la
 * localidad pendiente más cercana y, al terminar, vuelve a la base. Las
 * distancias entre localidades son las de Floyd, así que dos localidades
 * sin ruta directa también se pueden encadenar (el tramo pasa por otras).
 *
 * <p>Es una heurística voraz: rápida (O(V²) una vez calculado Floyd) pero no
 * garantiza el recorrido óptimo. Decide cada paso mirando solo la distancia
 * desde donde está, sin considerar cuánto costará después llegar a las
 * localidades que va dejando atrás ni volver a la base; una elección barata
 * ahora puede obligar a tramos muy largos al final. Encontrar el óptimo es el
 * problema del viajante (NP-difícil).
 * Empates de distancia: gana el menor índice, para que el plan sea reproducible.
 */
public final class PlanVisitas {

    /** Base de la cuadrilla asignada al grupo 3. */
    public static final String BASE_GRUPO = "San Francisco";

    /** Un tramo del plan: de una parada a la siguiente por el camino mínimo. */
    public record Tramo(String desde, String hasta, int km, List<String> camino) {
    }

    private final String base;
    private final List<Tramo> tramos;
    private final List<String> sinAcceso;

    private PlanVisitas(String base, List<Tramo> tramos, List<String> sinAcceso) {
        this.base = base;
        this.tramos = List.copyOf(tramos);
        this.sinAcceso = List.copyOf(sinAcceso);
    }

    /** Visita todas las localidades de la red. */
    public static PlanVisitas planificar(GrafoLocalidades grafo, String base) {
        return planificar(grafo, base, grafo.getNombres());
    }

    /**
     * Visita solo las localidades indicadas (por ejemplo, las que tienen
     * estaciones críticas). Las que no tienen camino desde la base se informan
     * aparte en {@link #getSinAcceso()}.
     */
    public static PlanVisitas planificar(GrafoLocalidades grafo, String base, Collection<String> aVisitar) {
        CaminosMinimos.ResultadoFloyd floyd = CaminosMinimos.floyd(grafo);
        int inicio = grafo.indiceDe(base);

        boolean[] pendiente = new boolean[grafo.cantidadLocalidades()];
        int cantidadPendientes = 0;
        List<String> sinAcceso = new ArrayList<>();
        for (String nombre : aVisitar) {
            int i = grafo.indiceDe(nombre);
            if (i == inicio) {
                continue;
            }
            if (floyd.distancia(inicio, i) == CaminosMinimos.INFINITO) {
                if (!sinAcceso.contains(grafo.nombreDe(i))) {
                    sinAcceso.add(grafo.nombreDe(i));
                }
            } else if (!pendiente[i]) {
                pendiente[i] = true;
                cantidadPendientes++;
            }
        }

        List<Tramo> tramos = new ArrayList<>();
        int actual = inicio;
        while (cantidadPendientes > 0) {
            // Vecino más cercano: la pendiente con menor distancia de Floyd.
            int elegido = -1;
            for (int candidato = 0; candidato < pendiente.length; candidato++) {
                if (!pendiente[candidato]) {
                    continue;
                }
                if (elegido == -1
                        || floyd.distancia(actual, candidato) < floyd.distancia(actual, elegido)
                        || (floyd.distancia(actual, candidato) == floyd.distancia(actual, elegido) && candidato < elegido)) {
                    elegido = candidato;
                }
            }
            tramos.add(tramo(grafo, floyd, actual, elegido));
            pendiente[elegido] = false;
            cantidadPendientes--;
            actual = elegido;
        }
        if (actual != inicio) {
            tramos.add(tramo(grafo, floyd, actual, inicio)); // regreso a la base
        }
        return new PlanVisitas(grafo.nombreDe(inicio), tramos, sinAcceso);
    }

    /**
     * Localidades distintas del reporte de estaciones críticas de M2, en el
     * orden en que aparecen (el reporte viene ordenado por número de estación).
     */
    public static List<String> localidadesDe(List<Estacion> reporteCriticas) {
        List<String> localidades = new ArrayList<>();
        for (Estacion e : reporteCriticas) {
            if (!localidades.contains(e.getLocalidad())) {
                localidades.add(e.getLocalidad());
            }
        }
        return localidades;
    }

    private static Tramo tramo(GrafoLocalidades grafo, CaminosMinimos.ResultadoFloyd floyd, int desde, int hasta) {
        return new Tramo(grafo.nombreDe(desde), grafo.nombreDe(hasta),
                floyd.distancia(desde, hasta), floyd.camino(desde, hasta));
    }

    public String getBase() {
        return base;
    }

    public List<Tramo> getTramos() {
        return tramos;
    }

    public List<String> getSinAcceso() {
        return sinAcceso;
    }

    /** Orden de paradas: base, localidades visitadas y regreso a la base. */
    public List<String> ordenDeVisita() {
        List<String> orden = new ArrayList<>();
        orden.add(base);
        for (Tramo t : tramos) {
            orden.add(t.hasta());
        }
        return orden;
    }

    public int kmTotales() {
        int total = 0;
        for (Tramo t : tramos) {
            total += t.km();
        }
        return total;
    }

    public String texto() {
        StringBuilder sb = new StringBuilder();
        sb.append("Base: ").append(base).append(System.lineSeparator());
        int acumulado = 0;
        for (int i = 0; i < tramos.size(); i++) {
            Tramo t = tramos.get(i);
            acumulado += t.km();
            sb.append(String.format("%2d. %-17s -> %-17s %4d km (acum. %4d)  vía %s%n",
                    i + 1, t.desde(), t.hasta(), t.km(), acumulado, String.join(" > ", t.camino())));
        }
        sb.append("Total: ").append(kmTotales()).append(" km").append(System.lineSeparator());
        if (!sinAcceso.isEmpty()) {
            sb.append("Sin acceso desde la base: ").append(String.join(", ", sinAcceso)).append(System.lineSeparator());
        }
        return sb.toString();
    }
}
