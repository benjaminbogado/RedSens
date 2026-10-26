package redsens.indice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import redsens.modelo.Estacion;
import redsens.modelo.TipoEstacion;

/**
 * Experimento obligatorio del M2: compara ABB y AVL con 1.000, 5.000 y 10.000
 * estaciones insertadas en orden ascendente y en orden aleatorio.
 *
 * <p>Uso: {@code java -cp out redsens.indice.ExperimentoArboles <semilla>}. La
 * semilla es obligatoria y debe ser la asignada al grupo en el enunciado.
 *
 * <p>El ABB es recursivo y, insertado en orden ascendente, degenera en una
 * lista de hasta 10.000 niveles. Para no modificar su implementación, el
 * experimento corre en un hilo propio con una pila más grande; antes se hace
 * una prueba con la pila por defecto de la JVM y se informa si desborda.
 */
public final class ExperimentoArboles {

    private static final int[] CANTIDADES = {1000, 5000, 10000};
    /** Los códigos CBA-NNNN admiten exactamente los números 0..9999. */
    private static final int NUMERO_MAXIMO = 9999;
    private static final int REPETICIONES_CALENTAMIENTO = 3;
    private static final int REPETICIONES_MEDIDAS = 10;
    private static final long PILA_EXPERIMENTO = 512L * 1024 * 1024;
    private static final String[] LOCALIDADES = {
        "Córdoba", "Río Cuarto", "Villa María", "San Francisco", "Villa Dolores", "Deán Funes"
    };

    private ExperimentoArboles() {
    }

    private enum Orden {
        ASCENDENTE("ascendente"), ALEATORIO("aleatorio");

        private final String etiqueta;

        Orden(String etiqueta) {
            this.etiqueta = etiqueta;
        }
    }

    /** Resultado medido de un escenario (cantidad + orden). */
    private static final class Resultado {
        int cantidad;
        Orden orden;
        int alturaAbb;
        int alturaAvl;
        double construccionAbbMs;
        double construccionAvlMs;
        double busquedaTotalAbbMs;
        double busquedaTotalAvlMs;
        double busquedaPromedioAbbNs;
        double busquedaPromedioAvlNs;
    }

    public static void main(String[] args) throws InterruptedException {
        if (args.length != 1) {
            System.err.println("Uso: java -cp out redsens.indice.ExperimentoArboles <semilla del grupo>");
            System.exit(2);
        }
        long semilla = Long.parseLong(args[0]);

        System.out.println("Experimento M2 — ABB vs AVL");
        System.out.println("Java " + System.getProperty("java.version") + " · "
                + System.getProperty("os.name") + " " + System.getProperty("os.arch"));
        System.out.println("Semilla: " + semilla + " · repeticiones de búsqueda: "
                + REPETICIONES_MEDIDAS + " (+" + REPETICIONES_CALENTAMIENTO + " de calentamiento)");
        System.out.println();

        probarPilaPorDefecto();

        List<Resultado> resultados = new ArrayList<>();
        Throwable[] error = new Throwable[1];
        Thread hilo = new Thread(null, () -> {
            try {
                for (int cantidad : CANTIDADES) {
                    for (Orden orden : Orden.values()) {
                        resultados.add(ejecutarEscenario(cantidad, orden, semilla));
                    }
                }
            } catch (Throwable t) {
                error[0] = t;
            }
        }, "experimento-m2", PILA_EXPERIMENTO);
        hilo.start();
        hilo.join();
        if (error[0] != null) {
            System.out.println("ERROR durante el experimento: " + error[0]);
            System.exit(1);
        }

        imprimirTabla(resultados);
    }

    // ------------------------------------------------------------------
    // Prueba de pila
    // ------------------------------------------------------------------

    /** Inserta 10.000 claves ascendentes en el ABB con la pila por defecto de la JVM. */
    private static void probarPilaPorDefecto() throws InterruptedException {
        List<Estacion> estaciones = generarEstaciones(numerosAscendentes(10000), new Random(0));
        String[] resultado = new String[1];
        Thread hilo = new Thread(() -> {
            ArbolBinarioBusqueda abb = new ArbolBinarioBusqueda();
            try {
                for (Estacion e : estaciones) {
                    abb.insertar(e);
                }
                resultado[0] = "no desbordó (altura " + abb.altura() + ")";
            } catch (StackOverflowError e) {
                resultado[0] = "StackOverflowError tras insertar " + abb.tamanio() + " estaciones";
            }
        });
        hilo.start();
        hilo.join();
        System.out.println("Prueba con pila por defecto (ABB, 10.000 ascendentes): " + resultado[0]);
        System.out.println("El experimento corre en un hilo con pila de "
                + (PILA_EXPERIMENTO / (1024 * 1024)) + " MB.");
        System.out.println();
    }

    // ------------------------------------------------------------------
    // Escenario
    // ------------------------------------------------------------------

    private static Resultado ejecutarEscenario(int cantidad, Orden orden, long semilla) {
        // Generación de datos: fuera de toda medición.
        Random random = new Random(semilla);
        List<Integer> numeros = elegirNumeros(cantidad, random);
        if (orden == Orden.ALEATORIO) {
            Collections.shuffle(numeros, random);
        }
        List<Estacion> estaciones = generarEstaciones(numeros, random);

        List<Integer> clavesBusqueda = new ArrayList<>(numeros);
        Collections.shuffle(clavesBusqueda, random);
        int[] claves = new int[cantidad];
        Estacion[] esperadas = new Estacion[cantidad];
        Estacion[] porNumero = new Estacion[NUMERO_MAXIMO + 1];
        for (Estacion e : estaciones) {
            porNumero[e.getNumero()] = e;
        }
        for (int i = 0; i < cantidad; i++) {
            claves[i] = clavesBusqueda.get(i);
            esperadas[i] = porNumero[claves[i]];
        }

        // Construcción (medida aparte de las búsquedas).
        ArbolBinarioBusqueda abb = new ArbolBinarioBusqueda();
        long inicio = System.nanoTime();
        for (Estacion e : estaciones) {
            abb.insertar(e);
        }
        long construccionAbb = System.nanoTime() - inicio;

        ArbolAVL avl = new ArbolAVL();
        inicio = System.nanoTime();
        for (Estacion e : estaciones) {
            avl.insertar(e);
        }
        long construccionAvl = System.nanoTime() - inicio;

        verificar(abb, avl, estaciones, porNumero, claves, esperadas, cantidad, orden);

        // Búsquedas: calentamiento y repeticiones medidas, alternando qué árbol va primero.
        for (int r = 0; r < REPETICIONES_CALENTAMIENTO; r++) {
            buscarTodasAbb(abb, claves, esperadas);
            buscarTodasAvl(avl, claves, esperadas);
        }
        long totalAbb = 0;
        long totalAvl = 0;
        for (int r = 0; r < REPETICIONES_MEDIDAS; r++) {
            if (r % 2 == 0) {
                totalAbb += buscarTodasAbb(abb, claves, esperadas);
                totalAvl += buscarTodasAvl(avl, claves, esperadas);
            } else {
                totalAvl += buscarTodasAvl(avl, claves, esperadas);
                totalAbb += buscarTodasAbb(abb, claves, esperadas);
            }
        }

        Resultado res = new Resultado();
        res.cantidad = cantidad;
        res.orden = orden;
        res.alturaAbb = abb.altura();
        res.alturaAvl = avl.altura();
        res.construccionAbbMs = construccionAbb / 1e6;
        res.construccionAvlMs = construccionAvl / 1e6;
        res.busquedaTotalAbbMs = totalAbb / 1e6 / REPETICIONES_MEDIDAS;
        res.busquedaTotalAvlMs = totalAvl / 1e6 / REPETICIONES_MEDIDAS;
        res.busquedaPromedioAbbNs = (double) totalAbb / REPETICIONES_MEDIDAS / cantidad;
        res.busquedaPromedioAvlNs = (double) totalAvl / REPETICIONES_MEDIDAS / cantidad;
        return res;
    }

    /** Busca todas las claves y devuelve el tiempo en ns; falla si algún resultado es incorrecto. */
    private static long buscarTodasAbb(ArbolBinarioBusqueda abb, int[] claves, Estacion[] esperadas) {
        int incorrectas = 0;
        long inicio = System.nanoTime();
        for (int i = 0; i < claves.length; i++) {
            if (abb.buscar(claves[i]) != esperadas[i]) {
                incorrectas++;
            }
        }
        long tiempo = System.nanoTime() - inicio;
        exigir(incorrectas == 0, "ABB: " + incorrectas + " búsquedas incorrectas");
        return tiempo;
    }

    /** Busca todas las claves y devuelve el tiempo en ns; falla si algún resultado es incorrecto. */
    private static long buscarTodasAvl(ArbolAVL avl, int[] claves, Estacion[] esperadas) {
        int incorrectas = 0;
        long inicio = System.nanoTime();
        for (int i = 0; i < claves.length; i++) {
            if (avl.buscar(claves[i]) != esperadas[i]) {
                incorrectas++;
            }
        }
        long tiempo = System.nanoTime() - inicio;
        exigir(incorrectas == 0, "AVL: " + incorrectas + " búsquedas incorrectas");
        return tiempo;
    }

    // ------------------------------------------------------------------
    // Verificación
    // ------------------------------------------------------------------

    private static void verificar(ArbolBinarioBusqueda abb, ArbolAVL avl, List<Estacion> estaciones,
                                  Estacion[] porNumero, int[] claves, Estacion[] esperadas,
                                  int cantidad, Orden orden) {
        String escenario = cantidad + " " + orden.etiqueta;
        exigir(abb.cantidadNodos() == cantidad, escenario + ": ABB no tiene " + cantidad + " nodos");
        exigir(avl.cantidadNodos() == cantidad, escenario + ": AVL no tiene " + cantidad + " nodos");

        List<Integer> esperado = new ArrayList<>();
        for (Estacion e : estaciones) {
            esperado.add(e.getNumero());
        }
        Collections.sort(esperado);
        exigir(numeros(abb.inorden()).equals(esperado), escenario + ": inorden del ABB incorrecto");
        exigir(numeros(avl.inorden()).equals(esperado), escenario + ": inorden del AVL incorrecto");

        for (int i = 0; i < claves.length; i++) {
            exigir(abb.buscar(claves[i]) == esperadas[i], escenario + ": ABB no encuentra " + claves[i]);
            exigir(avl.buscar(claves[i]) == esperadas[i], escenario + ": AVL no encuentra " + claves[i]);
        }

        // Claves ausentes: fuera de rango y, si quedan, números no elegidos.
        List<Integer> ausentes = new ArrayList<>(List.of(-1, NUMERO_MAXIMO + 1));
        for (int n = 0; n <= NUMERO_MAXIMO && ausentes.size() < 102; n++) {
            if (porNumero[n] == null) {
                ausentes.add(n);
            }
        }
        for (int n : ausentes) {
            exigir(abb.buscar(n) == null, escenario + ": ABB encuentra la ausente " + n);
            exigir(avl.buscar(n) == null, escenario + ": AVL encuentra la ausente " + n);
        }
        System.out.println("[OK] " + escenario + ": " + cantidad + " estaciones en ambos árboles, "
                + claves.length + " búsquedas correctas, " + ausentes.size() + " ausentes devuelven null");
    }

    private static void exigir(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new IllegalStateException(mensaje);
        }
    }

    // ------------------------------------------------------------------
    // Datos
    // ------------------------------------------------------------------

    /**
     * Elige {@code cantidad} números únicos entre 0 y 9999 y los devuelve
     * ordenados de forma ascendente. Con 10.000 se usan todos.
     */
    private static List<Integer> elegirNumeros(int cantidad, Random random) {
        List<Integer> todos = numerosAscendentes(NUMERO_MAXIMO + 1);
        Collections.shuffle(todos, random);
        List<Integer> elegidos = new ArrayList<>(todos.subList(0, cantidad));
        Collections.sort(elegidos);
        return elegidos;
    }

    private static List<Integer> numerosAscendentes(int cantidad) {
        List<Integer> numeros = new ArrayList<>(cantidad);
        for (int n = 0; n < cantidad; n++) {
            numeros.add(n);
        }
        return numeros;
    }

    /** Crea una estación válida por número, en el mismo orden recibido. */
    private static List<Estacion> generarEstaciones(List<Integer> numeros, Random random) {
        TipoEstacion[] tipos = TipoEstacion.values();
        List<Estacion> estaciones = new ArrayList<>(numeros.size());
        for (int numero : numeros) {
            String codigo = String.format("CBA-%04d", numero);
            estaciones.add(new Estacion(codigo, numero, "Estación " + numero,
                    LOCALIDADES[random.nextInt(LOCALIDADES.length)],
                    tipos[random.nextInt(tipos.length)],
                    random.nextInt(101),
                    random.nextInt(31)));
        }
        return estaciones;
    }

    private static List<Integer> numeros(List<Estacion> estaciones) {
        List<Integer> resultado = new ArrayList<>(estaciones.size());
        for (Estacion e : estaciones) {
            resultado.add(e.getNumero());
        }
        return resultado;
    }

    // ------------------------------------------------------------------
    // Salida
    // ------------------------------------------------------------------

    private static void imprimirTabla(List<Resultado> resultados) {
        System.out.println();
        System.out.println("| Estaciones | Orden | Altura ABB | Altura AVL | Búsqueda prom. ABB (ns) "
                + "| Búsqueda prom. AVL (ns) | Búsqueda total ABB (ms) | Búsqueda total AVL (ms) "
                + "| Construcción ABB (ms) | Construcción AVL (ms) |");
        System.out.println("|---:|---|---:|---:|---:|---:|---:|---:|---:|---:|");
        for (Resultado r : resultados) {
            System.out.println(String.format(Locale.ROOT,
                    "| %d | %s | %d | %d | %.1f | %.1f | %.3f | %.3f | %.3f | %.3f |",
                    r.cantidad, r.orden.etiqueta, r.alturaAbb, r.alturaAvl,
                    r.busquedaPromedioAbbNs, r.busquedaPromedioAvlNs,
                    r.busquedaTotalAbbMs, r.busquedaTotalAvlMs,
                    r.construccionAbbMs, r.construccionAvlMs));
        }
        System.out.println();
        System.out.println("Alturas contadas en aristas (árbol vacío = -1), igual en ABB y AVL.");
        System.out.println("Búsqueda: todas las claves insertadas, en orden aleatorio fijo, mismo orden en ambos árboles;");
        System.out.println("promedio de " + REPETICIONES_MEDIDAS + " repeticiones. Construcción: una inserción completa, medida aparte.");
    }
}
