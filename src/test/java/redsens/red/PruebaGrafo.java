package redsens.red;

import java.io.IOException;
import java.util.List;

import redsens.indice.ArbolAVL;
import redsens.modelo.Estacion;
import redsens.modelo.TipoEstacion;

/**
 * Pruebas manuales de M3. Ejecuta todos los casos, informa cada verificación
 * con [OK] / [FALLA] y termina con código de salida 1 si algo falla.
 *
 * Uso (desde la raíz del repositorio): java -cp out redsens.red.PruebaGrafo
 */
public class PruebaGrafo {

    private static int ok = 0;
    private static int fallas = 0;

    public static void main(String[] args) throws IOException {
        probarConstruccionYRepresentaciones();
        probarValidaciones();
        probarCargador();
        probarRecorridos();
        probarDijkstra();
        probarFloyd();
        probarDistanciasGrandes();
        probarCorteDeRuta();
        probarPlanVisitas();
        probarRedDelGrupo();
        probarIntegracionM2();

        System.out.println();
        System.out.println("==============================================");
        System.out.printf("Resultado: %d verificaciones OK, %d fallas%n", ok, fallas);
        System.out.println("==============================================");
        if (fallas > 0) {
            System.exit(1);
        }
    }

    /**
     * Grafo chico de referencia, verificable a mano (F queda aislada):
     * <pre>
     *   A --4-- B --5-- D
     *   |     /         |
     *   1   2           3
     *   | /             |
     *   C ------8------ E        F
     * </pre>
     */
    private static GrafoLocalidades grafoChico() {
        return CargadorGrafo.desdeLineas(List.of(
                "# grafo de prueba",
                "localidad;A", "localidad;B", "localidad;C",
                "localidad;D", "localidad;E", "localidad;F",
                "ruta;A;B;4", "ruta;A;C;1", "ruta;B;C;2",
                "ruta;B;D;5", "ruta;C;E;8", "ruta;D;E;3"));
    }

    // ------------------------------------------------------------------

    private static void probarConstruccionYRepresentaciones() {
        titulo("1. Construcción, lista y matriz de adyacencia");
        GrafoLocalidades g = grafoChico();
        verificar("6 localidades", g.cantidadLocalidades() == 6);
        verificar("6 rutas", g.cantidadRutas() == 6);
        int a = g.indiceDe("A"), b = g.indiceDe("B"), c = g.indiceDe("C"), f = g.indiceDe("F");
        verificar("matriz A–B = 4", g.distanciaDirecta(a, b) == 4);
        verificar("matriz simétrica B–A = 4", g.distanciaDirecta(b, a) == 4);
        verificar("diagonal en 0", g.distanciaDirecta(c, c) == 0);
        verificar("A–F sin ruta", g.distanciaDirecta(a, f) == GrafoLocalidades.SIN_RUTA);
        verificar("lista de A tiene 2 rutas", g.rutasDesde(a).size() == 2);
        verificar("lista de F vacía", g.rutasDesde(f).isEmpty());
        verificar("lista y matriz coinciden", listaYMatrizCoinciden(g));
    }

    private static void probarValidaciones() {
        titulo("2. Validaciones");
        GrafoLocalidades g = grafoChico();
        verificar("localidad repetida", lanza(() -> g.agregarLocalidad("A")));
        verificar("localidad vacía", lanza(() -> g.agregarLocalidad("  ")));
        verificar("ruta repetida", lanza(() -> g.agregarRuta("B", "A", 9)));
        verificar("ruta a sí misma", lanza(() -> g.agregarRuta("A", "A", 1)));
        verificar("km no positivo", lanza(() -> g.agregarRuta("A", "F", 0)));
        verificar("localidad inexistente", lanza(() -> g.agregarRuta("A", "Z", 1)));
        verificar("cortar ruta inexistente", lanza(() -> g.cortarRuta("A", "F")));
        verificar("Dijkstra con origen inexistente", lanza(() -> CaminosMinimos.dijkstra(g, "Z")));
    }

    private static void probarCargador() {
        titulo("3. Carga desde texto");
        GrafoLocalidades g = CargadorGrafo.desdeLineas(List.of(
                "\uFEFF# con BOM", "", "ruta; X ; Y ; 7 "));
        verificar("crea localidades implícitas y recorta espacios",
                g.cantidadLocalidades() == 2 && g.distanciaDirecta(g.indiceDe("X"), g.indiceDe("Y")) == 7);
        verificar("tipo de línea desconocido", lanza(() -> CargadorGrafo.desdeLineas(List.of("camino;A;B;3"))));
        verificar("faltan campos", lanza(() -> CargadorGrafo.desdeLineas(List.of("ruta;A;B"))));
        verificar("km no numérico", lanza(() -> CargadorGrafo.desdeLineas(List.of("ruta;A;B;x"))));
        verificar("km negativo", lanza(() -> CargadorGrafo.desdeLineas(List.of("ruta;A;B;-3"))));
        try {
            CargadorGrafo.desdeLineas(List.of("localidad;A", "localidad;A"));
            verificar("el error informa la línea", false);
        } catch (IllegalArgumentException e) {
            verificar("el error informa la línea", e.getMessage().startsWith("línea 2"));
        }
    }

    private static void probarRecorridos() {
        titulo("4. BFS y DFS");
        GrafoLocalidades g = grafoChico();
        verificar("BFS desde A = A,B,C,D,E", Recorridos.bfs(g, "A").equals(List.of("A", "B", "C", "D", "E")));
        verificar("DFS desde A = A,B,C,E,D", Recorridos.dfs(g, "A").equals(List.of("A", "B", "C", "E", "D")));
        verificar("BFS lista = BFS matriz", Recorridos.bfs(g, "A").equals(Recorridos.bfsMatriz(g, "A")));
        verificar("DFS lista = DFS matriz", Recorridos.dfs(g, "A").equals(Recorridos.dfsMatriz(g, "A")));
        verificar("no es conexo (F aislada)", !Recorridos.esConexo(g, "A"));
        verificar("inalcanzables desde A = [F]", Recorridos.inalcanzables(g, "A").equals(List.of("F")));
        verificar("BFS desde F = [F]", Recorridos.bfs(g, "F").equals(List.of("F")));
        List<List<String>> niveles = Recorridos.nivelesBfs(g, "A");
        verificar("niveles BFS: [A] [B,C] [D,E]", niveles.equals(List.of(
                List.of("A"), List.of("B", "C"), List.of("D", "E"))));
    }

    private static void probarDijkstra() {
        titulo("5. Dijkstra");
        GrafoLocalidades g = grafoChico();
        CaminosMinimos.ResultadoDijkstra r = CaminosMinimos.dijkstra(g, "A");
        verificar("A→A = 0", r.distanciaA("A") == 0);
        verificar("A→B = 3 (por C, no la directa de 4)", r.distanciaA("B") == 3);
        verificar("camino A→B = A,C,B", r.caminoA("B").equals(List.of("A", "C", "B")));
        verificar("A→D = 8 por A,C,B,D", r.distanciaA("D") == 8 && r.caminoA("D").equals(List.of("A", "C", "B", "D")));
        verificar("A→E = 9 por A,C,E", r.distanciaA("E") == 9 && r.caminoA("E").equals(List.of("A", "C", "E")));
        verificar("A→F sin camino", !r.hayCamino("F") && r.caminoA("F").isEmpty());
    }

    private static void probarFloyd() {
        titulo("6. Floyd");
        GrafoLocalidades g = grafoChico();
        CaminosMinimos.ResultadoFloyd f = CaminosMinimos.floyd(g);
        verificar("B→E = 8 por B,D,E", f.distancia("B", "E") == 8 && f.camino("B", "E").equals(List.of("B", "D", "E")));
        verificar("simétrica E→B = 8", f.distancia("E", "B") == 8);
        verificar("C→F infinito", f.distancia("C", "F") == CaminosMinimos.INFINITO && f.camino("C", "F").isEmpty());
        verificar("no modifica la matriz del grafo", g.distanciaDirecta(g.indiceDe("B"), g.indiceDe("E")) == GrafoLocalidades.SIN_RUTA);
        verificar("Floyd = Dijkstra para todos los pares", floydIgualDijkstra(g));
    }

    private static void probarDistanciasGrandes() {
        titulo("6 bis. Distancias grandes y prevención de desbordamientos");
        GrafoLocalidades g = CargadorGrafo.desdeLineas(List.of(
                "ruta;A;B;1500000000",
                "ruta;B;C;1000000000",
                "ruta;A;C;1800000000"));
        CaminosMinimos.ResultadoDijkstra d = CaminosMinimos.dijkstra(g, "A");
        CaminosMinimos.ResultadoFloyd f = CaminosMinimos.floyd(g);
        verificar("la ruta directa A-C de 1.800.000.000 se conserva en matriz y lista",
                g.distanciaDirecta(g.indiceDe("A"), g.indiceDe("C")) == 1_800_000_000L
                        && g.rutasDesde(g.indiceDe("A")).stream()
                        .anyMatch(r -> r.destino() == g.indiceDe("C") && r.km() == 1_800_000_000L));
        verificar("Dijkstra identifica el camino directo A-C",
                d.distanciaA("C") == 1_800_000_000L && d.caminoA("C").equals(List.of("A", "C")));
        verificar("Floyd identifica el camino directo A-C",
                f.distancia("A", "C") == 1_800_000_000L && f.camino("A", "C").equals(List.of("A", "C")));

        GrafoLocalidades soloTramos = CargadorGrafo.desdeLineas(List.of(
                "ruta;A;B;1500000000", "ruta;B;C;1000000000"));
        verificar("Dijkstra representa A-B-C = 2.500.000.000",
                CaminosMinimos.dijkstra(soloTramos, "A").distanciaA("C") == 2_500_000_000L);
        verificar("Floyd representa A-B-C = 2.500.000.000",
                CaminosMinimos.floyd(soloTramos).distancia("A", "C") == 2_500_000_000L);

        GrafoLocalidades limite = new GrafoLocalidades();
        limite.agregarLocalidad("X");
        limite.agregarLocalidad("Y");
        limite.agregarRuta("X", "Y", Long.MAX_VALUE);
        verificar("Long.MAX_VALUE sigue siendo una distancia válida, no ausencia de ruta",
                CaminosMinimos.dijkstra(limite, "X").distanciaA("Y") == Long.MAX_VALUE
                        && CaminosMinimos.floyd(limite).distancia("X", "Y") == Long.MAX_VALUE);
    }

    private static void probarCorteDeRuta() {
        titulo("7. Corte de ruta");
        GrafoLocalidades g = grafoChico();
        CorteRuta corte = CorteRuta.simular(g, "A", "B", "D");
        CorteRuta.Impacto d = corte.getImpactos().stream().filter(i -> i.destino().equals("D")).findFirst().orElseThrow();
        verificar("A→D pasa de 8 a 12", d.kmAntes() == 8 && d.kmDespues() == 12);
        verificar("nuevo camino A,C,E,D", d.caminoDespues().equals(List.of("A", "C", "E", "D")));
        verificar("solo D cambia", corte.afectadas().size() == 1);
        verificar("el original no se modifica", g.cantidadRutas() == 6 && g.distanciaDirecta(g.indiceDe("B"), g.indiceDe("D")) == 5);
        verificar("la copia sí perdió la ruta", corte.getRedCortada().cantidadRutas() == 5 && listaYMatrizCoinciden(corte.getRedCortada()));

        GrafoLocalidades doble = g.copiar();
        doble.cortarRuta("B", "D");
        CorteRuta aislar = CorteRuta.simular(doble, "A", "D", "E");
        verificar("doble corte aísla D (F ya estaba aislada y no se cuenta)", aislar.aisladas().equals(List.of("D")));
        verificar("corte que no aísla: lista vacía", corte.aisladas().isEmpty());

        GrafoLocalidades conexo = CargadorGrafo.desdeLineas(List.of(
                "ruta;A;B;1", "ruta;B;C;1", "ruta;C;A;1", "ruta;C;D;1"));
        verificar("puente detectado: solo C – D desconecta",
                CorteRuta.rutasQueDesconectan(conexo).equals(List.of("C – D")));
    }

    private static void probarPlanVisitas() {
        titulo("8. Plan de visitas (vecino más cercano)");
        GrafoLocalidades g = grafoChico();
        PlanVisitas plan = PlanVisitas.planificar(g, "A");
        verificar("orden A,C,B,D,E,A", plan.ordenDeVisita().equals(List.of("A", "C", "B", "D", "E", "A")));
        verificar("total 1+2+5+3+9 = 20 km", plan.kmTotales() == 20);
        verificar("F informada sin acceso", plan.getSinAcceso().equals(List.of("F")));
        verificar("tramo E→A expandido E,C,A", plan.getTramos().get(4).camino().equals(List.of("E", "C", "A")));

        PlanVisitas parcial = PlanVisitas.planificar(g, "A", List.of("E", "B"));
        verificar("parcial: A,B,E,A", parcial.ordenDeVisita().equals(List.of("A", "B", "E", "A")));
        verificar("parcial: 3+8+9 = 20 km", parcial.kmTotales() == 20);

        PlanVisitas soloBase = PlanVisitas.planificar(g, "A", List.of("A"));
        verificar("solo la base: sin tramos", soloBase.getTramos().isEmpty() && soloBase.kmTotales() == 0);
    }

    private static void probarRedDelGrupo() throws IOException {
        titulo("9. Red del enunciado (datos/red_vial.txt) con base San Francisco");
        GrafoLocalidades red = CargadorGrafo.cargarPorDefecto();
        String base = PlanVisitas.BASE_GRUPO;
        verificar("12 localidades y 18 tramos", red.cantidadLocalidades() == 12 && red.cantidadRutas() == 18);
        verificar("incluye la base", red.existeLocalidad(base));
        verificar("tramo Villa Carlos Paz – Cosquín = 24 km",
                red.distanciaDirecta(red.indiceDe("Villa Carlos Paz"), red.indiceDe("Cosquín")) == 24);
        verificar("no hay tramo directo Córdoba – Río Cuarto",
                red.distanciaDirecta(red.indiceDe("Córdoba"), red.indiceDe("Río Cuarto")) == GrafoLocalidades.SIN_RUTA);
        verificar("lista y matriz coinciden", listaYMatrizCoinciden(red));
        verificar("red conexa (DFS)", Recorridos.esConexo(red, base));
        verificar("BFS/DFS: lista = matriz", Recorridos.bfs(red, base).equals(Recorridos.bfsMatriz(red, base))
                && Recorridos.dfs(red, base).equals(Recorridos.dfsMatriz(red, base)));

        List<List<String>> niveles = Recorridos.nivelesBfs(red, base);
        verificar("BFS nivel 1 = Córdoba, Villa María", niveles.get(1).equals(List.of("Córdoba", "Villa María")));
        verificar("BFS nivel 2 = VCP, Alta Gracia, Jesús María, Río Tercero, Río Cuarto",
                niveles.get(2).equals(List.of("Villa Carlos Paz", "Alta Gracia", "Jesús María", "Río Tercero", "Río Cuarto")));
        verificar("BFS nivel 3 = Cosquín, Villa Dolores, Deán Funes",
                niveles.get(3).equals(List.of("Cosquín", "Villa Dolores", "Deán Funes")));
        verificar("BFS nivel 4 = La Falda", niveles.size() == 5 && niveles.get(4).equals(List.of("La Falda")));

        CaminosMinimos.ResultadoDijkstra control = CaminosMinimos.dijkstra(red, "Córdoba");
        verificar("control Córdoba→Río Cuarto = 212 por Río Tercero",
                control.distanciaA("Río Cuarto") == 212
                        && control.caminoA("Río Cuarto").equals(List.of("Córdoba", "Río Tercero", "Río Cuarto")));
        verificar("control Córdoba→La Falda = 80 por VCP y Cosquín", control.distanciaA("La Falda") == 80);

        CaminosMinimos.ResultadoDijkstra dij = CaminosMinimos.dijkstra(red, base);
        verificar("San Francisco→Río Tercero = 220 por Villa María",
                dij.distanciaA("Río Tercero") == 220
                        && dij.caminoA("Río Tercero").equals(List.of("San Francisco", "Villa María", "Río Tercero")));
        verificar("San Francisco→Villa Dolores = 381",
                dij.caminoA("Villa Dolores").equals(List.of("San Francisco", "Córdoba", "Villa Carlos Paz", "Villa Dolores"))
                        && dij.distanciaA("Villa Dolores") == 381);
        verificar("Floyd = Dijkstra para todos los pares", floydIgualDijkstra(red));

        CorteRuta corte = CorteRuta.simular(red, base, "San Francisco", "Córdoba");
        verificar("corte Córdoba – San Francisco: Córdoba pasa de 205 a 296 km",
                corte.getImpactos().stream().anyMatch(i -> i.destino().equals("Córdoba")
                        && i.kmAntes() == 205 && i.kmDespues() == 296));
        verificar("corte Córdoba – San Francisco: sigue conexa", corte.aisladas().isEmpty() && corte.sigueConexa());
        verificar("ningún corte simple desconecta la red", CorteRuta.rutasQueDesconectan(red).isEmpty());
        GrafoLocalidades sinRn19 = red.copiar();
        sinRn19.cortarRuta("Córdoba", "San Francisco");
        CorteRuta doble = CorteRuta.simular(sinRn19, base, "San Francisco", "Villa María");
        verificar("doble corte: San Francisco queda aislada", doble.aisladas().size() == 11 && !doble.sigueConexa());

        PlanVisitas plan = PlanVisitas.planificar(red, base);
        List<String> orden = plan.ordenDeVisita();
        verificar("plan sale y vuelve a la base", orden.get(0).equals(base) && orden.get(orden.size() - 1).equals(base));
        verificar("plan visita las 11 localidades una vez",
                orden.size() == 13 && orden.subList(1, 12).stream().distinct().count() == 11);
        verificar("plan completo = 1354 km", plan.kmTotales() == 1354);
        verificar("plan: suma de tramos = total", plan.getTramos().stream().mapToLong(PlanVisitas.Tramo::km).sum() == plan.kmTotales());
    }

    private static void probarIntegracionM2() {
        titulo("10. Integración M2 → M3 (reporte de críticas del AVL)");
        ArbolAVL avl = new ArbolAVL();
        avl.insertar(estacion(10, "Cosquín", 5));
        avl.insertar(estacion(3, "Río Cuarto", 15));
        avl.insertar(estacion(7, "Cosquín", 90));
        avl.insertar(estacion(1, "Cosquín", 10));
        List<String> localidades = PlanVisitas.localidadesDe(avl.listarCriticas());
        verificar("localidades sin repetir, en orden del reporte", localidades.equals(List.of("Cosquín", "Río Cuarto")));
    }

    private static Estacion estacion(int numero, String localidad, int bateria) {
        return new Estacion(String.format("CBA-%04d", numero), numero, "Estación " + numero, localidad,
                TipoEstacion.PLUVIOMETRICA, bateria, 0);
    }

    // ------------------------------------------------------------------
    // Auxiliares
    // ------------------------------------------------------------------

    private static boolean listaYMatrizCoinciden(GrafoLocalidades g) {
        int n = g.cantidadLocalidades();
        int aristasEnMatriz = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                long km = g.distanciaDirecta(i, j);
                if (i == j || km == GrafoLocalidades.SIN_RUTA) {
                    continue;
                }
                aristasEnMatriz++;
                final int destino = j;
                if (g.rutasDesde(i).stream().noneMatch(r -> r.destino() == destino && r.km() == km)) {
                    return false;
                }
            }
        }
        int aristasEnLista = 0;
        for (int i = 0; i < n; i++) {
            aristasEnLista += g.rutasDesde(i).size();
        }
        return aristasEnMatriz == aristasEnLista && aristasEnLista == 2 * g.cantidadRutas();
    }

    private static boolean floydIgualDijkstra(GrafoLocalidades g) {
        CaminosMinimos.ResultadoFloyd floyd = CaminosMinimos.floyd(g);
        for (String origen : g.getNombres()) {
            CaminosMinimos.ResultadoDijkstra dij = CaminosMinimos.dijkstra(g, origen);
            for (String destino : g.getNombres()) {
                if (floyd.distancia(origen, destino) != dij.distanciaA(destino)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean lanza(Runnable accion) {
        try {
            accion.run();
            return false;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    private static void titulo(String texto) {
        System.out.println();
        System.out.println("--- " + texto + " ---");
    }

    private static void verificar(String descripcion, boolean condicion) {
        if (condicion) {
            ok++;
            System.out.println("[OK]    " + descripcion);
        } else {
            fallas++;
            System.out.println("[FALLA] " + descripcion);
        }
    }
}
