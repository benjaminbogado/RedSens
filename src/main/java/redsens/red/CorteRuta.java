package redsens.red;

import java.util.ArrayList;
import java.util.List;

/**
 * Simula el corte de una ruta (elimina la arista) sobre una copia de la red,
 * así la red original no cambia. Informa con DFS si la red sigue conexa y qué
 * localidades quedan aisladas de la base, y con Dijkstra cuánto empeoran los
 * caminos desde la base.
 */
public final class CorteRuta {

    /** Cómo cambia el camino a una localidad por el corte. */
    public record Impacto(String destino, long kmAntes, long kmDespues,
                          List<String> caminoAntes, List<String> caminoDespues) {
        /** Tenía camino antes del corte y ya no lo tiene. */
        public boolean quedoAislada() {
            return kmAntes != CaminosMinimos.INFINITO && kmDespues == CaminosMinimos.INFINITO;
        }

        public boolean cambio() {
            return kmAntes != kmDespues || !caminoAntes.equals(caminoDespues);
        }
    }

    private final String base;
    private final String origen;
    private final String destino;
    private final long kmCortados;
    private final GrafoLocalidades redCortada;
    private final List<Impacto> impactos;
    private final List<String> sinAccesoAntes;

    private CorteRuta(String base, String origen, String destino, long kmCortados,
                      GrafoLocalidades redCortada, List<Impacto> impactos, List<String> sinAccesoAntes) {
        this.base = base;
        this.origen = origen;
        this.destino = destino;
        this.kmCortados = kmCortados;
        this.redCortada = redCortada;
        this.impactos = List.copyOf(impactos);
        this.sinAccesoAntes = List.copyOf(sinAccesoAntes);
    }

    public static CorteRuta simular(GrafoLocalidades grafo, String base, String origen, String destino) {
        CaminosMinimos.ResultadoDijkstra antes = CaminosMinimos.dijkstra(grafo, base);
        GrafoLocalidades cortada = grafo.copiar();
        long km = cortada.cortarRuta(origen, destino);
        CaminosMinimos.ResultadoDijkstra despues = CaminosMinimos.dijkstra(cortada, base);

        List<Impacto> impactos = new ArrayList<>();
        for (String localidad : grafo.getNombres()) {
            if (localidad.equals(grafo.nombreDe(grafo.indiceDe(base)))) {
                continue;
            }
            impactos.add(new Impacto(localidad, antes.distanciaA(localidad), despues.distanciaA(localidad),
                    antes.caminoA(localidad), despues.caminoA(localidad)));
        }
        return new CorteRuta(grafo.nombreDe(grafo.indiceDe(base)), origen, destino, km, cortada, impactos,
                Recorridos.inalcanzables(grafo, base));
    }

    public GrafoLocalidades getRedCortada() {
        return redCortada;
    }

    public List<Impacto> getImpactos() {
        return impactos;
    }

    public List<Impacto> afectadas() {
        return impactos.stream().filter(Impacto::cambio).toList();
    }

    /**
     * Localidades que quedan separadas de la base por este corte (DFS sobre la
     * red cortada), sin contar las que ya estaban sin acceso antes.
     */
    public List<String> aisladas() {
        return Recorridos.inalcanzables(redCortada, base).stream()
                .filter(l -> !sinAccesoAntes.contains(l)).toList();
    }

    public boolean sigueConexa() {
        return Recorridos.esConexo(redCortada, base);
    }

    /**
     * Prueba cortar cada ruta, una por vez, y devuelve las que desconectan la
     * red (en teoría de grafos, los "puentes"), como "Origen – Destino".
     * Fuerza bruta: E cortes × un DFS O(V + E) cada uno.
     */
    public static List<String> rutasQueDesconectan(GrafoLocalidades grafo) {
        List<String> puentes = new ArrayList<>();
        if (grafo.cantidadLocalidades() == 0) {
            return puentes;
        }
        String cualquiera = grafo.nombreDe(0);
        for (int i = 0; i < grafo.cantidadLocalidades(); i++) {
            for (Ruta r : grafo.rutasDesde(i)) {
                if (i < r.destino()) { // cada ruta una sola vez
                    GrafoLocalidades copia = grafo.copiar();
                    copia.cortarRuta(grafo.nombreDe(i), grafo.nombreDe(r.destino()));
                    if (!Recorridos.esConexo(copia, cualquiera)) {
                        puentes.add(grafo.nombreDe(i) + " – " + grafo.nombreDe(r.destino()));
                    }
                }
            }
        }
        return puentes;
    }

    public String texto() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Corte: %s – %s (%d km)%n", origen, destino, kmCortados));
        List<String> aisladas = aisladas();
        if (aisladas.isEmpty()) {
            sb.append("DFS: la red sigue siendo conexa; ninguna localidad queda aislada.").append(System.lineSeparator());
        } else {
            List<String> ladoBase = Recorridos.dfs(redCortada, base);
            sb.append("DFS: la red deja de ser conexa. Queda partida en dos:").append(System.lineSeparator());
            sb.append("  con la base: ").append(String.join(", ", ladoBase)).append(System.lineSeparator());
            sb.append("  sin acceso desde la base: ").append(String.join(", ", aisladas)).append(System.lineSeparator());
            if (ladoBase.size() == 1) {
                sb.append("  La base ").append(base).append(" queda aislada.").append(System.lineSeparator());
            }
        }
        boolean hayCambios = false;
        for (Impacto i : afectadas()) {
            if (i.quedoAislada()) {
                continue; // ya informadas arriba
            }
            if (!hayCambios) {
                sb.append("Caminos mínimos desde la base que empeoran (Dijkstra):").append(System.lineSeparator());
                hayCambios = true;
            }
            sb.append(String.format("  %-17s %4d km -> %4d km (+%d)  ahora vía %s%n", i.destino(),
                    i.kmAntes(), i.kmDespues(), i.kmDespues() - i.kmAntes(),
                    String.join(" > ", i.caminoDespues())));
        }
        if (!hayCambios && aisladas.isEmpty()) {
            sb.append("Ningún camino mínimo desde la base cambia.").append(System.lineSeparator());
        }
        return sb.toString();
    }
}
