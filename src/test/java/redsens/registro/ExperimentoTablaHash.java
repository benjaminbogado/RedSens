package redsens.registro;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import redsens.modelo.Estacion;
import redsens.simulacion.GeneradorDatos;

/**
 * Experimento obligatorio del M1: inserta 10.000 estaciones simuladas con cada
 * estrategia de exploración y compara colisiones, sondeos promedio por búsqueda
 * exitosa y no exitosa, y cantidad de rehashings.
 *
 * <p>Uso: {@code java -cp out redsens.registro.ExperimentoTablaHash [semilla]}
 * (por defecto 3026, la del grupo 3).
 *
 * <p>Partes:
 * <ol>
 *   <li>Experimento obligatorio: capacidad inicial 17, umbral propio de cada estrategia.</li>
 *   <li>Control: las dos estrategias con el mismo umbral (0,5), para aislar el efecto
 *       del agrupamiento primario del efecto del umbral.</li>
 *   <li>Barrido de factor de carga con capacidad fija (sin rehashing), comparado con
 *       las fórmulas teóricas.</li>
 * </ol>
 *
 * <p>Las 10.000 estaciones simuladas salen de {@link GeneradorDatos} (M6) con la
 * semilla del grupo, como pide el enunciado.
 */
public final class ExperimentoTablaHash {

    private static final long SEMILLA_GRUPO = 3026L;
    private static final int CAPACIDAD_INICIAL = 17;
    private static final int CANTIDAD = 10000;
    private static final int MUESTRA_FALLIDAS = 1000;
    private static final int CALENTAMIENTO = 3;
    private static final int REPETICIONES = 10;
    private static final int CAPACIDAD_FIJA = 10007; // primo, para el barrido
    private static final double[] FACTORES = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};

    private ExperimentoTablaHash() {
    }

    /** Resultado de un escenario de 10.000 inserciones. */
    private static final class Resultado {
        String estrategia;
        double umbral;
        long colisionesInsercion;
        long sondeosInsercion;
        int rehashings;
        int capacidadFinal;
        double factorFinal;
        double sondeosExitosa;
        double colisionesExitosa;
        double nsExitosa;
        double sondeosFallidaConBorrados;
        double sondeosFallidaSinBorrados;
    }

    public static void main(String[] args) {
        long semilla = args.length > 0 ? Long.parseLong(args[0]) : SEMILLA_GRUPO;
        Locale.setDefault(Locale.forLanguageTag("es-AR"));

        List<Estacion> estaciones = GeneradorDatos.generarEstaciones(CANTIDAD, semilla);
        // Orden fijo de búsqueda (distinto del de inserción), igual para ambas estrategias.
        List<Estacion> ordenBusqueda = new ArrayList<>(estaciones);
        Collections.shuffle(ordenBusqueda, new Random(semilla + 1));
        // Muestra para búsquedas fallidas: se toma del mismo orden de búsqueda.
        List<Estacion> muestra = ordenBusqueda.subList(0, MUESTRA_FALLIDAS);

        System.out.println("RedSens · Experimento M1 (tabla hash)");
        System.out.printf("Semilla %d · capacidad inicial %d · %d estaciones · Java %s · %s %s%n%n",
                semilla, CAPACIDAD_INICIAL, CANTIDAD, System.getProperty("java.version"),
                System.getProperty("os.name"), System.getProperty("os.arch"));

        // 1. Experimento obligatorio
        List<Resultado> obligatorio = new ArrayList<>();
        for (EstrategiaExploracion e : EstrategiaExploracion.values()) {
            obligatorio.add(ejecutar(e, TablaHash.umbralPorDefecto(e), estaciones, ordenBusqueda, muestra));
        }
        System.out.println("1. EXPERIMENTO OBLIGATORIO (umbral propio de cada estrategia)");
        imprimir(obligatorio);

        // 2. Control con el mismo umbral
        List<Resultado> control = new ArrayList<>();
        for (EstrategiaExploracion e : EstrategiaExploracion.values()) {
            control.add(ejecutar(e, 0.5, estaciones, ordenBusqueda, muestra));
        }
        System.out.println("2. CONTROL (ambas estrategias con umbral 0,5)");
        imprimir(control);

        // 3. Barrido de factor de carga
        System.out.println("3. BARRIDO DE FACTOR DE CARGA (capacidad fija " + CAPACIDAD_FIJA + ", sin rehashing)");
        barrido(semilla);
    }

    // ------------------------------------------------------------------
    // Escenario principal
    // ------------------------------------------------------------------

    private static Resultado ejecutar(EstrategiaExploracion e, double umbral, List<Estacion> estaciones,
                                      List<Estacion> ordenBusqueda, List<Estacion> muestra) {
        Resultado r = new Resultado();
        r.estrategia = e.name();
        r.umbral = umbral;

        // Inserción de las 10.000 desde la capacidad del grupo
        TablaHash t = new TablaHash(CAPACIDAD_INICIAL, e, umbral);
        for (Estacion s : estaciones) {
            t.insertar(s);
        }
        r.colisionesInsercion = t.getColisionesTotales();
        r.sondeosInsercion = t.getSondeosTotales();
        r.rehashings = t.getRehashings();
        r.capacidadFinal = t.capacidad();
        r.factorFinal = t.factorCarga();

        // Búsquedas exitosas: las 10.000, verificando el resultado
        t.reiniciarContadores();
        for (Estacion s : ordenBusqueda) {
            exigir(t.buscar(s.getCodigo()) == s, "búsqueda exitosa incorrecta: " + s.getCodigo());
        }
        r.sondeosExitosa = (double) t.getSondeosTotales() / ordenBusqueda.size();
        r.colisionesExitosa = (double) t.getColisionesTotales() / ordenBusqueda.size();

        // Tiempo de búsqueda exitosa (calentamiento JIT + repeticiones medidas)
        String[] codigos = new String[ordenBusqueda.size()];
        for (int i = 0; i < codigos.length; i++) {
            codigos[i] = ordenBusqueda.get(i).getCodigo();
        }
        for (int i = 0; i < CALENTAMIENTO; i++) {
            buscarTodas(t, codigos);
        }
        long total = 0;
        for (int i = 0; i < REPETICIONES; i++) {
            total += buscarTodas(t, codigos);
        }
        r.nsExitosa = (double) total / REPETICIONES / codigos.length;

        // Búsquedas no exitosas (a): con los 10.000 CBA-NNNN cargados no queda ningún
        // código válido ausente, así que se borran (perezosamente) 1.000 y se buscan.
        for (Estacion s : muestra) {
            t.eliminar(s.getCodigo());
        }
        t.reiniciarContadores();
        for (Estacion s : muestra) {
            exigir(t.buscar(s.getCodigo()) == null, "una clave borrada se encontró: " + s.getCodigo());
        }
        r.sondeosFallidaConBorrados = (double) t.getSondeosTotales() / muestra.size();

        // Búsquedas no exitosas (b): tabla sin borrados, con 9.000 insertadas, se buscan las 1.000 restantes.
        boolean[] enMuestra = new boolean[CANTIDAD];
        for (Estacion s : muestra) {
            enMuestra[s.getNumero()] = true;
        }
        TablaHash sinBorrados = new TablaHash(CAPACIDAD_INICIAL, e, umbral);
        for (Estacion s : estaciones) {
            if (!enMuestra[s.getNumero()]) {
                sinBorrados.insertar(s);
            }
        }
        sinBorrados.reiniciarContadores();
        for (Estacion s : muestra) {
            exigir(sinBorrados.buscar(s.getCodigo()) == null, "clave ausente encontrada: " + s.getCodigo());
        }
        r.sondeosFallidaSinBorrados = (double) sinBorrados.getSondeosTotales() / muestra.size();
        return r;
    }

    private static long buscarTodas(TablaHash t, String[] codigos) {
        int encontradas = 0;
        long inicio = System.nanoTime();
        for (String c : codigos) {
            if (t.buscar(c) != null) {
                encontradas++;
            }
        }
        long fin = System.nanoTime();
        exigir(encontradas == codigos.length, "faltan estaciones en la medición de tiempo");
        return fin - inicio;
    }

    // ------------------------------------------------------------------
    // Barrido de factor de carga
    // ------------------------------------------------------------------

    private static void barrido(long semilla) {
        List<Estacion> estaciones = GeneradorDatos.generarEstaciones(CANTIDAD, semilla);
        System.out.println("Promedio de sondeos por búsqueda. Teoría lineal (Knuth): exitosa ½(1+1/(1−λ)),"
                + " no exitosa ½(1+1/(1−λ)²).");
        System.out.println("Referencia sin agrupamiento (hashing uniforme): exitosa (1/λ)·ln(1/(1−λ)),"
                + " no exitosa 1/(1−λ).");
        System.out.println();
        System.out.printf("%-5s | %-23s | %-23s | %-23s | %-23s%n", "λ",
                "LINEAL exitosa (teoría)", "LINEAL fallida (teoría)",
                "CUADR. exitosa (unif.)", "CUADR. fallida (unif.)");
        System.out.println("-".repeat(108));
        for (double lambda : FACTORES) {
            int n = (int) Math.round(lambda * CAPACIDAD_FIJA);
            double[] lin = medirBarrido(EstrategiaExploracion.LINEAL, estaciones, n);
            double[] cua = medirBarrido(EstrategiaExploracion.CUADRATICA, estaciones, n);
            System.out.printf("%-5.1f | %8.2f (%8.2f)     | %8.2f (%8.2f)     | %-23s | %-23s%n", lambda,
                    lin[0], 0.5 * (1 + 1 / (1 - lambda)),
                    lin[1], 0.5 * (1 + 1 / ((1 - lambda) * (1 - lambda))),
                    cua == null ? "falla al insertar" : String.format("%8.2f (%8.2f)",
                            cua[0], Math.log(1 / (1 - lambda)) / lambda),
                    cua == null ? "" : String.format("%8.2f (%8.2f)", cua[1], 1 / (1 - lambda)));
        }
        System.out.println();
        System.out.println("Para λ > 0,5 la cuadrática se corre igual, a modo de prueba: con m primo su garantía");
        System.out.println("de encontrar casilla libre vale únicamente hasta λ = 0,5.");
    }

    /** Inserta las n primeras en una tabla de capacidad fija y busca todas + las ausentes. */
    private static double[] medirBarrido(EstrategiaExploracion e, List<Estacion> estaciones, int n) {
        TablaHash t = new TablaHash(CAPACIDAD_FIJA, e); // capacidad fija
        try {
            for (int i = 0; i < n; i++) {
                t.insertar(estaciones.get(i));
            }
        } catch (IllegalStateException ex) {
            return null;
        }
        exigir(t.capacidad() == CAPACIDAD_FIJA, "el barrido no debe hacer rehashing");
        t.reiniciarContadores();
        for (int i = 0; i < n; i++) {
            t.buscar(estaciones.get(i).getCodigo());
        }
        double exitosa = (double) t.getSondeosTotales() / n;
        int ausentes = Math.min(MUESTRA_FALLIDAS, estaciones.size() - n);
        t.reiniciarContadores();
        for (int i = n; i < n + ausentes; i++) {
            t.buscar(estaciones.get(i).getCodigo());
        }
        double fallida = (double) t.getSondeosTotales() / ausentes;
        return new double[] {exitosa, fallida};
    }

    // ------------------------------------------------------------------
    // Datos y salida
    // ------------------------------------------------------------------

    private static void imprimir(List<Resultado> resultados) {
        System.out.printf("%-11s | %6s | %11s | %10s | %5s | %9s | %6s | %10s | %10s | %10s | %10s | %10s%n",
                "Estrategia", "Umbral", "Colis. ins.", "Sond. ins.", "Rehash", "Cap. final", "λ final",
                "Sond. éxito", "Colis. éx.", "ns/búsq.", "Fall. c/bor", "Fall. s/bor");
        System.out.println("-".repeat(145));
        for (Resultado r : resultados) {
            System.out.printf("%-11s | %6.2f | %11d | %10d | %5d | %9d | %6.3f | %10.3f | %10.3f | %10.1f | %10.3f | %10.3f%n",
                    r.estrategia, r.umbral, r.colisionesInsercion, r.sondeosInsercion, r.rehashings,
                    r.capacidadFinal, r.factorFinal, r.sondeosExitosa, r.colisionesExitosa, r.nsExitosa,
                    r.sondeosFallidaConBorrados, r.sondeosFallidaSinBorrados);
        }
        System.out.println();
    }

    private static void exigir(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new IllegalStateException(mensaje);
        }
    }
}
