package redsens.registro;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import redsens.modelo.Estacion;
import redsens.modelo.TipoEstacion;

/**
 * Pruebas manuales de la parte del integrante 4 en M1: exploración cuadrática,
 * factor de carga, rehashing y eliminación perezosa con ambas estrategias.
 * Informa cada verificación con [OK] / [FALLA] y sale con código 1 si algo falla.
 *
 * <p>Uso: {@code java -cp out redsens.registro.PruebaTablaHashCuadratica}
 */
public class PruebaTablaHashCuadratica {

    private static final long SEMILLA = 3026L;       // grupo 3 (enunciado, sección 3)
    private static final int CAPACIDAD_INICIAL = 17; // grupo 3 (enunciado, sección 3)

    private static int ok = 0;
    private static int fallas = 0;

    public static void main(String[] args) {
        probarPrimos();
        probarTeoremaCuadratico();
        probarOperacionesBasicas(EstrategiaExploracion.LINEAL);
        probarOperacionesBasicas(EstrategiaExploracion.CUADRATICA);
        probarSecuenciaDeExploracion();
        probarEliminacionPerezosa(EstrategiaExploracion.LINEAL);
        probarEliminacionPerezosa(EstrategiaExploracion.CUADRATICA);
        probarRehashing(EstrategiaExploracion.LINEAL);
        probarRehashing(EstrategiaExploracion.CUADRATICA);
        probarBorradasDisparanRehashing();
        probarLimpiezaSinCrecer();
        probarDuplicadoNoAmplia();
        probarDiezMil(EstrategiaExploracion.LINEAL);
        probarDiezMil(EstrategiaExploracion.CUADRATICA);
        probarOperacionesAleatorias(EstrategiaExploracion.LINEAL);
        probarOperacionesAleatorias(EstrategiaExploracion.CUADRATICA);
        probarArgumentosInvalidos();

        System.out.println();
        System.out.println("==============================================");
        System.out.printf("Resultado: %d verificaciones OK, %d fallas%n", ok, fallas);
        System.out.println("==============================================");
        if (fallas > 0) {
            System.exit(1);
        }
    }

    // ------------------------------------------------------------------
    // Casos
    // ------------------------------------------------------------------

    private static void probarPrimos() {
        titulo("Primos");
        verificar(Primos.esPrimo(2) && Primos.esPrimo(17) && Primos.esPrimo(10949), "detecta primos");
        verificar(!Primos.esPrimo(1) && !Primos.esPrimo(34) && !Primos.esPrimo(10947), "detecta compuestos");
        verificar(Primos.siguientePrimo(34) == 37, "siguientePrimo(2·17) = 37");
        verificar(Primos.siguientePrimo(74) == 79, "siguientePrimo(2·37) = 79");
        verificar(Primos.siguientePrimo(17) == 17, "siguientePrimo de un primo es él mismo");
    }

    /** Con m primo, las ⌈m/2⌉ primeras posiciones h + i² son distintas. */
    private static void probarTeoremaCuadratico() {
        titulo("Teorema de la exploración cuadrática");
        int[] primos = {17, 37, 79, 163, 331, 673, 1361};
        for (int m : primos) {
            boolean distintas = true;
            int mitad = (m + 1) / 2;
            for (int h = 0; h < m && distintas; h++) {
                boolean[] vista = new boolean[m];
                for (int i = 0; i < mitad; i++) {
                    int p = EstrategiaExploracion.CUADRATICA.posicion(h, i, m);
                    if (vista[p]) {
                        distintas = false;
                        break;
                    }
                    vista[p] = true;
                }
            }
            verificar(distintas, "m = " + m + ": las " + mitad + " primeras posiciones son distintas para todo h");
        }
        // Con m no primo el teorema no vale: m = 16 solo alcanza 4 posiciones distintas desde 0.
        boolean[] vista = new boolean[16];
        int distintas = 0;
        for (int i = 0; i < 16; i++) {
            int p = EstrategiaExploracion.CUADRATICA.posicion(0, i, 16);
            if (!vista[p]) {
                vista[p] = true;
                distintas++;
            }
        }
        verificar(distintas == 4, "m = 16 (no primo): desde 0 solo se alcanzan 4 posiciones (0, 1, 4, 9)");
    }

    private static void probarOperacionesBasicas(EstrategiaExploracion e) {
        titulo("Operaciones básicas · " + e);
        TablaHash t = TablaHash.ampliable(CAPACIDAD_INICIAL, e);
        verificar(t.estaVacia() && t.capacidad() == 17, "tabla nueva vacía con capacidad 17");
        verificar(t.buscar("CBA-0347") == null, "buscar en tabla vacía devuelve null");

        Estacion a = estacion(347, 18);
        t.insertar(a);
        verificar(t.tamanio() == 1, "tamaño 1 tras insertar");
        verificar(t.buscar("CBA-0347") == a, "encuentra exactamente el objeto insertado");
        verificar(lanza(() -> t.insertar(estacion(347, 50))), "rechaza código duplicado");
        verificar(t.tamanio() == 1, "el duplicado no cambia el tamaño");
        t.eliminar("CBA-0347");
        verificar(t.buscar("CBA-0347") == null, "tras eliminar, buscar devuelve null");
        verificar(lanza(() -> t.eliminar("CBA-0347")), "eliminar inexistente lanza excepción");
        verificar(t.tamanio() == 0 && t.cantidadBorradas() == 1, "queda una casilla BORRADA");
    }

    /** Fuerza colisiones con códigos que caen en la misma posición inicial. */
    private static void probarSecuenciaDeExploracion() {
        titulo("Secuencia de exploración con colisiones forzadas (m = 17)");
        List<Estacion> mismas = codigosConMismoHash(4, 17);
        int h = hashInicial(17, mismas.get(0).getCodigo());

        // Capacidad fija: se observa solo la secuencia de exploración.
        TablaHash lineal = new TablaHash(17, EstrategiaExploracion.LINEAL);
        TablaHash cuadratica = new TablaHash(17, EstrategiaExploracion.CUADRATICA);
        for (int i = 0; i < mismas.size(); i++) {
            lineal.insertar(mismas.get(i));
            cuadratica.insertar(mismas.get(i));
            verificar(lineal.getColisionesUltimaOperacion() == i && lineal.getSondeosUltimaOperacion() == i + 1,
                    "lineal, clave " + (i + 1) + ": " + i + " colisiones y " + (i + 1) + " sondeos");
            verificar(cuadratica.getColisionesUltimaOperacion() == i
                            && cuadratica.getSondeosUltimaOperacion() == i + 1,
                    "cuadrática, clave " + (i + 1) + ": " + i + " colisiones y " + (i + 1) + " sondeos");
        }
        verificar(EstrategiaExploracion.LINEAL.posicion(h, 3, 17) == (h + 3) % 17, "lineal: 4.º intento en h+3");
        verificar(EstrategiaExploracion.CUADRATICA.posicion(h, 3, 17) == (h + 9) % 17, "cuadrática: 4.º intento en h+9");
        for (Estacion s : mismas) {
            verificar(lineal.buscar(s.getCodigo()) == s && cuadratica.buscar(s.getCodigo()) == s,
                    "ambas encuentran " + s.getCodigo());
        }
    }

    private static void probarEliminacionPerezosa(EstrategiaExploracion e) {
        titulo("Eliminación perezosa · " + e);
        List<Estacion> mismas = codigosConMismoHash(3, 17);
        TablaHash t = TablaHash.ampliable(17, e);
        for (Estacion s : mismas) {
            t.insertar(s);
        }
        Estacion a = mismas.get(0), b = mismas.get(1), c = mismas.get(2);
        t.eliminar(b.getCodigo());
        verificar(t.buscar(c.getCodigo()) == c, "la clave después de la borrada se sigue encontrando");
        verificar(t.getSondeosUltimaOperacion() == 3, "la búsqueda pasa por la casilla borrada (3 sondeos)");
        verificar(t.getColisionesUltimaOperacion() == 1, "la casilla borrada no cuenta como colisión");
        verificar(t.buscar(b.getCodigo()) == null, "la clave borrada no se encuentra");
        verificar(lanza(() -> t.insertar(c)), "no permite duplicar una clave ubicada después de la borrada");

        t.insertar(b);
        verificar(t.cantidadBorradas() == 0, "al reinsertar se reutiliza la casilla borrada");
        verificar(t.getSondeosUltimaOperacion() == 4, "la inserción recorre hasta la vacía antes de reutilizar (4 sondeos)");
        verificar(t.buscar(a.getCodigo()) == a && t.buscar(b.getCodigo()) == b && t.buscar(c.getCodigo()) == c,
                "las tres claves siguen accesibles");
    }

    private static void probarRehashing(EstrategiaExploracion e) {
        titulo("Rehashing · " + e + " (umbral " + TablaHash.umbralPorDefecto(e) + ")");
        TablaHash t = TablaHash.ampliable(CAPACIDAD_INICIAL, e);
        int limite = (int) Math.floor(TablaHash.umbralPorDefecto(e) * 17); // máximo sin superar el umbral
        List<Estacion> datos = estacionesAleatorias(limite + 1, new Random(SEMILLA));
        for (int i = 0; i < limite; i++) {
            t.insertar(datos.get(i));
        }
        verificar(t.capacidad() == 17 && t.getRehashings() == 0,
                limite + " estaciones entran en 17 sin rehashing");
        t.insertar(datos.get(limite));
        verificar(t.capacidad() == 37 && t.getRehashings() == 1,
                "la estación " + (limite + 1) + " dispara el rehashing a 37 (siguiente primo de 34)");
        boolean todas = true;
        for (Estacion s : datos) {
            todas &= t.buscar(s.getCodigo()) == s;
        }
        verificar(todas, "todas las estaciones se encuentran después del rehashing");
        verificar(t.factorCarga() <= TablaHash.umbralPorDefecto(e), "el factor de carga queda bajo el umbral");
    }

    private static void probarBorradasDisparanRehashing() {
        titulo("Las casillas borradas cuentan para el umbral (cuadrática)");
        TablaHash t = TablaHash.ampliable(17, EstrategiaExploracion.CUADRATICA);
        List<Estacion> datos = estacionesAleatorias(9, new Random(SEMILLA));
        for (int i = 0; i < 8; i++) {
            t.insertar(datos.get(i));
        }
        for (int i = 0; i < 4; i++) {
            t.eliminar(datos.get(i).getCodigo());
        }
        verificar(t.tamanio() == 4 && t.cantidadBorradas() == 4, "4 ocupadas + 4 borradas en m = 17");
        t.insertar(datos.get(8));
        verificar(t.getRehashings() == 1 && t.capacidad() == 37,
                "(4 + 4 + 1)/17 > 0,5: hace rehashing aunque solo haya 5 estaciones");
        verificar(t.cantidadBorradas() == 0 && t.tamanio() == 5, "el rehashing descarta las casillas borradas");
    }

    private static void probarLimpiezaSinCrecer() {
        titulo("Si casi todo son marcas de borrado, se limpian sin crecer (cuadrática)");
        TablaHash t = TablaHash.ampliable(17, EstrategiaExploracion.CUADRATICA);
        List<Estacion> datos = estacionesAleatorias(9, new Random(SEMILLA));
        for (int i = 0; i < 8; i++) {
            t.insertar(datos.get(i));
        }
        for (int i = 0; i < 7; i++) {
            t.eliminar(datos.get(i).getCodigo());
        }
        t.insertar(datos.get(8));
        verificar(t.capacidad() == 17 && t.getLimpiezas() == 1 && t.getRehashings() == 0,
                "1 activa + 7 borradas: reconstruye en 17 en vez de crecer");
        verificar(t.cantidadBorradas() == 0 && t.tamanio() == 2, "quedan 2 activas y ninguna marca");
        verificar(t.buscar(datos.get(7).getCodigo()) == datos.get(7) && t.buscar(datos.get(8).getCodigo()) == datos.get(8),
                "las activas se siguen encontrando");
    }

    private static void probarDuplicadoNoAmplia() {
        titulo("Un duplicado rechazado no amplía la tabla");
        TablaHash t = TablaHash.ampliable(17, EstrategiaExploracion.CUADRATICA);
        List<Estacion> datos = estacionesAleatorias(8, new Random(SEMILLA));
        for (Estacion s : datos) {
            t.insertar(s);
        }
        verificar(lanza(() -> t.insertar(datos.get(0))), "rechaza el duplicado");
        verificar(t.capacidad() == 17 && t.getRehashings() == 0, "la capacidad sigue en 17");
    }

    private static void probarDiezMil(EstrategiaExploracion e) {
        titulo("10.000 estaciones desde capacidad 17 · " + e);
        List<Estacion> datos = estacionesAleatorias(10000, new Random(SEMILLA));
        TablaHash t = TablaHash.ampliable(CAPACIDAD_INICIAL, e);
        double maxOcupacion = 0;
        for (Estacion s : datos) {
            t.insertar(s);
            maxOcupacion = Math.max(maxOcupacion, t.factorOcupacion());
        }
        verificar(t.tamanio() == 10000, "tamaño 10.000");
        verificar(maxOcupacion <= TablaHash.umbralPorDefecto(e), String.format(
                "el factor de ocupación nunca superó %.2f (máximo %.4f)", TablaHash.umbralPorDefecto(e), maxOcupacion));
        verificar(Primos.esPrimo(t.capacidad()), "capacidad final prima: " + t.capacidad());
        boolean todas = true;
        for (Estacion s : datos) {
            todas &= t.buscar(s.getCodigo()) == s;
        }
        verificar(todas, "las 10.000 se encuentran (" + t.getRehashings() + " rehashings)");
    }

    /** Operaciones mezcladas contra un registro de referencia indexado por número. */
    private static void probarOperacionesAleatorias(EstrategiaExploracion e) {
        titulo("50.000 operaciones aleatorias · " + e);
        Random r = new Random(SEMILLA);
        TablaHash t = TablaHash.ampliable(CAPACIDAD_INICIAL, e);
        Estacion[] referencia = new Estacion[10000];
        int tamanioEsperado = 0;
        boolean coincide = true;
        for (int op = 0; op < 50000 && coincide; op++) {
            int n = r.nextInt(2000); // rango acotado para que haya muchas repeticiones y borrados
            String codigo = String.format("CBA-%04d", n);
            switch (r.nextInt(3)) {
                case 0 -> {
                    Estacion s = estacion(n, r.nextInt(101));
                    boolean lanzo = lanza(() -> t.insertar(s));
                    coincide = lanzo == (referencia[n] != null);
                    if (!lanzo) {
                        referencia[n] = s;
                        tamanioEsperado++;
                    }
                }
                case 1 -> coincide = t.buscar(codigo) == referencia[n];
                default -> {
                    boolean lanzo = lanza(() -> t.eliminar(codigo));
                    coincide = lanzo == (referencia[n] == null);
                    if (!lanzo) {
                        referencia[n] = null;
                        tamanioEsperado--;
                    }
                }
            }
            coincide &= t.tamanio() == tamanioEsperado && t.factorOcupacion() <= TablaHash.umbralPorDefecto(e);
        }
        verificar(coincide, "coincide con la referencia en todas las operaciones y respeta el umbral");
    }

    private static void probarArgumentosInvalidos() {
        titulo("Argumentos inválidos");
        verificar(lanza(() -> TablaHash.ampliable(17, null)), "estrategia nula");
        verificar(lanza(() -> new TablaHash(0, EstrategiaExploracion.LINEAL, 0.5)), "capacidad 0");
        verificar(lanza(() -> new TablaHash(17, EstrategiaExploracion.LINEAL, 1.0)), "umbral 1");
        verificar(lanza(() -> new TablaHash(17, EstrategiaExploracion.LINEAL, 0.0)), "umbral 0");
        verificar(TablaHash.ampliable(18, EstrategiaExploracion.CUADRATICA).capacidad() == 19,
                "en una tabla ampliable la capacidad no prima se redondea al siguiente primo");
        verificar(new TablaHash(18, EstrategiaExploracion.CUADRATICA).capacidad() == 18,
                "la tabla de capacidad fija conserva la capacidad pedida");
        verificar(!new TablaHash(17).esAmpliable() && TablaHash.ampliable(17, EstrategiaExploracion.LINEAL).esAmpliable(),
                "los constructores de capacidad fija no amplían");
        TablaHash t = TablaHash.ampliable(17, EstrategiaExploracion.CUADRATICA);
        verificar(lanza(() -> t.insertar(null)), "insertar null");
        verificar(lanza(() -> t.buscar(null)), "buscar null");
    }

    // ------------------------------------------------------------------
    // Auxiliares
    // ------------------------------------------------------------------

    private static Estacion estacion(int numero, int bateria) {
        return new Estacion(String.format("CBA-%04d", numero), numero, "Estación " + numero,
                "Córdoba", TipoEstacion.PLUVIOMETRICA, bateria, 0);
    }

    private static List<Estacion> estacionesAleatorias(int cantidad, Random r) {
        List<Integer> numeros = new ArrayList<>();
        for (int n = 0; n < 10000; n++) {
            numeros.add(n);
        }
        Collections.shuffle(numeros, r);
        List<Estacion> lista = new ArrayList<>();
        for (int i = 0; i < cantidad; i++) {
            lista.add(estacion(numeros.get(i), r.nextInt(101)));
        }
        return lista;
    }

    /** Devuelve {@code cantidad} estaciones cuyo hash inicial coincide en una tabla de capacidad m. */
    private static List<Estacion> codigosConMismoHash(int cantidad, int m) {
        List<Estacion> lista = new ArrayList<>();
        int objetivo = hashInicial(m, "CBA-0000");
        for (int n = 0; n < 10000 && lista.size() < cantidad; n++) {
            Estacion s = estacion(n, 50);
            if (hashInicial(m, s.getCodigo()) == objetivo) {
                lista.add(s);
            }
        }
        return lista;
    }

    /** Misma fórmula que TablaHash.hash (privada): Horner en base 31, reduciendo en cada paso. */
    private static int hashInicial(int capacidad, String codigo) {
        long h = 0;
        for (int i = 0; i < codigo.length(); i++) {
            h = (31L * h + codigo.charAt(i)) % capacidad;
        }
        return (int) h;
    }

    private static boolean lanza(Runnable accion) {
        try {
            accion.run();
            return false;
        } catch (IllegalArgumentException ex) {
            return true;
        }
    }

    private static void titulo(String texto) {
        System.out.println();
        System.out.println("== " + texto + " ==");
    }

    private static void verificar(boolean condicion, String descripcion) {
        if (condicion) {
            ok++;
            System.out.println("[OK]    " + descripcion);
        } else {
            fallas++;
            System.out.println("[FALLA] " + descripcion);
        }
    }
}
