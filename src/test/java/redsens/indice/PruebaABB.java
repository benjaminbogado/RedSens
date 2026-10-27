package redsens.indice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import redsens.modelo.Estacion;
import redsens.modelo.TipoEstacion;

/**
 * Pruebas manuales del ABB de M2. Ejecuta todos los casos, informa cada
 * verificación con [OK] / [FALLA] y termina con código de salida 1 si algo falla.
 */
public class PruebaABB {

    private static final long SEMILLA = 3026L; // semilla asignada al grupo 3 (enunciado, sección 3)

    private static int ok = 0;
    private static int fallas = 0;

    public static void main(String[] args) {
        probarArbolVacio();
        probarEjemploFijo();
        probarEliminacionTresCasos();
        probarDatosOrdenados();
        probarDatosAleatorios();

        System.out.println();
        System.out.println("==============================================");
        System.out.printf("Resultado: %d verificaciones OK, %d fallas%n", ok, fallas);
        System.out.println("==============================================");
        if (fallas > 0) {
            System.exit(1);
        }
    }

    // ------------------------------------------------------------------
    // Casos de prueba
    // ------------------------------------------------------------------

    private static void probarArbolVacio() {
        titulo("1. Árbol vacío");
        ArbolBinarioBusqueda arbol = new ArbolBinarioBusqueda();

        verificar("estaVacio() es true", arbol.estaVacio());
        verificar("altura() = -1", arbol.altura() == -1);
        verificar("cantidadNodos() = 0", arbol.cantidadNodos() == 0);
        verificar("cantidadHojas() = 0", arbol.cantidadHojas() == 0);
        verificar("buscar(347) devuelve null", arbol.buscar(347) == null);
        verificar("eliminar(347) lanza IllegalArgumentException",
                lanzaIllegalArgument(() -> arbol.eliminar(347)));
        verificar("inorden() vacío", arbol.inorden().isEmpty());
        verificar("listarPorRango(0, 9999) vacío", arbol.listarPorRango(0, 9999).isEmpty());
        verificar("listarCriticas() vacío", arbol.listarCriticas().isEmpty());
    }

    /**
     * Árbol de referencia:
     * <pre>
     *            50
     *          /    \
     *        30      70
     *       /  \    /  \
     *     20   40  60   80
     * </pre>
     * Críticas (batería &lt; 20): 20, 60 y 80.
     */
    private static void probarEjemploFijo() {
        titulo("2. Ejemplo fijo (50, 30, 70, 20, 40, 60, 80)");
        ArbolBinarioBusqueda arbol = arbolDeReferencia();

        mostrar("inorden  ", arbol.inorden());
        mostrar("preorden ", arbol.preorden());
        mostrar("postorden", arbol.postorden());

        verificar("inorden correcto",
                numeros(arbol.inorden()).equals(Arrays.asList(20, 30, 40, 50, 60, 70, 80)));
        verificar("preorden correcto",
                numeros(arbol.preorden()).equals(Arrays.asList(50, 30, 20, 40, 70, 60, 80)));
        verificar("postorden correcto",
                numeros(arbol.postorden()).equals(Arrays.asList(20, 40, 30, 60, 80, 70, 50)));

        verificar("altura() = 2", arbol.altura() == 2);
        verificar("cantidadNodos() = 7", arbol.cantidadNodos() == 7);
        verificar("cantidadHojas() = 4", arbol.cantidadHojas() == 4);

        Estacion encontrada = arbol.buscar(40);
        verificar("buscar(40) encuentra la estación 40",
                encontrada != null && encontrada.getNumero() == 40);
        verificar("buscar(45) devuelve null", arbol.buscar(45) == null);

        verificar("listarPorRango(25, 65) = [30, 40, 50, 60]",
                numeros(arbol.listarPorRango(25, 65)).equals(Arrays.asList(30, 40, 50, 60)));
        verificar("listarPorRango(65, 25) lanza IllegalArgumentException",
                lanzaIllegalArgument(() -> arbol.listarPorRango(65, 25)));
        verificar("listarPorRango(20, 20) = [20] (límites inclusivos)",
                numeros(arbol.listarPorRango(20, 20)).equals(Arrays.asList(20)));
        verificar("listarPorRango(81, 99) vacío", arbol.listarPorRango(81, 99).isEmpty());

        mostrar("críticas ", arbol.listarCriticas());
        verificar("listarCriticas() = [20, 60, 80]",
                numeros(arbol.listarCriticas()).equals(Arrays.asList(20, 60, 80)));

        verificar("insertar duplicado (40) lanza IllegalArgumentException",
                lanzaIllegalArgument(() -> arbol.insertar(crearEstacion(40, 90))));
        verificar("insertar null lanza IllegalArgumentException",
                lanzaIllegalArgument(() -> arbol.insertar(null)));
        verificar("tras los rechazos sigue con 7 nodos", arbol.cantidadNodos() == 7);
    }

    private static void probarEliminacionTresCasos() {
        titulo("3. Eliminación: hoja, un hijo y dos hijos");
        ArbolBinarioBusqueda arbol = arbolDeReferencia();

        // Caso 1: hoja.
        verificar("eliminar(20) [hoja] sin errores", eliminarSinError(arbol, 20));
        verificar("20 ya no está", arbol.buscar(20) == null);
        verificar("inorden tras eliminar hoja",
                numeros(arbol.inorden()).equals(Arrays.asList(30, 40, 50, 60, 70, 80)));

        // Caso 2: 30 quedó con un solo hijo (40).
        verificar("eliminar(30) [un hijo] sin errores", eliminarSinError(arbol, 30));
        verificar("preorden tras eliminar nodo con un hijo",
                numeros(arbol.preorden()).equals(Arrays.asList(50, 40, 70, 60, 80)));

        // Caso 3: la raíz 50 tiene dos hijos; la reemplaza su sucesor (60).
        verificar("eliminar(50) [dos hijos, raíz] sin errores", eliminarSinError(arbol, 50));
        verificar("preorden tras eliminar nodo con dos hijos (sucesor 60 sube)",
                numeros(arbol.preorden()).equals(Arrays.asList(60, 40, 70, 80)));
        verificar("inorden sigue ordenado",
                numeros(arbol.inorden()).equals(Arrays.asList(40, 60, 70, 80)));

        verificar("eliminar(50) otra vez lanza IllegalArgumentException",
                lanzaIllegalArgument(() -> arbol.eliminar(50)));
        verificar("cantidadNodos() = 4", arbol.cantidadNodos() == 4);
        verificar("tamanio() coincide con cantidadNodos()", arbol.tamanio() == arbol.cantidadNodos());

        // Vaciar el árbol completo.
        for (int n : new int[] {40, 60, 70, 80}) {
            arbol.eliminar(n);
        }
        verificar("tras eliminar todo, el árbol queda vacío",
                arbol.estaVacio() && arbol.altura() == -1 && arbol.cantidadNodos() == 0);
    }

    private static void probarDatosOrdenados() {
        int cantidad = 500;
        titulo("4. Datos ordenados (1.." + cantidad + ") — caso degenerado");
        ArbolBinarioBusqueda arbol = new ArbolBinarioBusqueda();
        for (int n = 1; n <= cantidad; n++) {
            arbol.insertar(crearEstacion(n, n % 100));
        }

        System.out.printf("  altura = %d, nodos = %d, hojas = %d%n",
                arbol.altura(), arbol.cantidadNodos(), arbol.cantidadHojas());
        verificar("altura() = " + (cantidad - 1) + " (el ABB degenera en lista)",
                arbol.altura() == cantidad - 1);
        verificar("cantidadNodos() = " + cantidad, arbol.cantidadNodos() == cantidad);
        verificar("cantidadHojas() = 1", arbol.cantidadHojas() == 1);
        verificar("inorden ordenado", estaOrdenado(arbol.inorden()));
        verificar("buscar(" + cantidad + ") encuentra el último",
                arbol.buscar(cantidad) != null);
        verificar("listarPorRango(100, 199) devuelve 100 estaciones",
                arbol.listarPorRango(100, 199).size() == 100);
        // Baterías n % 100: son críticas las que terminan en 00..19 -> 20 por cada centena.
        verificar("listarCriticas() devuelve 100 estaciones ordenadas",
                arbol.listarCriticas().size() == 100 && estaOrdenado(arbol.listarCriticas()));
    }

    private static void probarDatosAleatorios() {
        int cantidad = 1000;
        titulo("5. Datos aleatorios (" + cantidad + " estaciones, semilla " + SEMILLA + ")");
        Random random = new Random(SEMILLA);

        // Números únicos entre 1 y 9999 (formato CBA-NNNN) y baterías 0..100.
        List<Integer> disponibles = new ArrayList<>();
        for (int n = 1; n <= 9999; n++) {
            disponibles.add(n);
        }
        Collections.shuffle(disponibles, random);

        // Referencia sin TreeMap (prohibido en M2): un arreglo indexado por número.
        // Recorrerlo por índice da las estaciones ya ordenadas.
        ArbolBinarioBusqueda arbol = new ArbolBinarioBusqueda();
        Estacion[] referencia = new Estacion[10000];
        for (int i = 0; i < cantidad; i++) {
            Estacion e = crearEstacion(disponibles.get(i), random.nextInt(101));
            arbol.insertar(e);
            referencia[e.getNumero()] = e;
        }

        System.out.printf("  altura = %d, nodos = %d, hojas = %d%n",
                arbol.altura(), arbol.cantidadNodos(), arbol.cantidadHojas());
        verificar("cantidadNodos() = " + cantidad, arbol.cantidadNodos() == cantidad);
        verificar("inorden coincide con la referencia ordenada",
                arbol.inorden().equals(enRango(referencia, 0, 9999)));
        verificar("preorden y postorden tienen " + cantidad + " elementos",
                arbol.preorden().size() == cantidad && arbol.postorden().size() == cantidad);
        // Un ABB aleatorio de 1000 nodos ronda altura ~25; muy lejos de 1000 (degenerado).
        verificar("altura razonable para datos aleatorios (< 60)", arbol.altura() < 60);
        verificar("todas las estaciones insertadas se encuentran",
                enRango(referencia, 0, 9999).stream().allMatch(e -> arbol.buscar(e.getNumero()) == e));
        verificar("listarPorRango(2000, 4000) coincide con la referencia",
                arbol.listarPorRango(2000, 4000).equals(enRango(referencia, 2000, 4000)));
        verificar("listarCriticas() coincide con la referencia",
                arbol.listarCriticas().equals(criticas(referencia)));

        // Eliminar la mitad en orden aleatorio.
        List<Integer> aEliminar = numeros(enRango(referencia, 0, 9999));
        Collections.shuffle(aEliminar, random);
        aEliminar = aEliminar.subList(0, cantidad / 2);
        boolean todasEliminadas = true;
        for (int n : aEliminar) {
            todasEliminadas &= eliminarSinError(arbol, n);
            referencia[n] = null;
        }
        int restantes = enRango(referencia, 0, 9999).size();
        verificar("se eliminaron " + aEliminar.size() + " estaciones aleatorias", todasEliminadas);
        verificar("cantidadNodos() = " + restantes, arbol.cantidadNodos() == restantes);
        verificar("tamanio() coincide con cantidadNodos()", arbol.tamanio() == arbol.cantidadNodos());
        verificar("inorden tras eliminar coincide con la referencia",
                arbol.inorden().equals(enRango(referencia, 0, 9999)));
        verificar("las eliminadas ya no se encuentran",
                aEliminar.stream().allMatch(n -> arbol.buscar(n) == null));
        verificar("listarCriticas() tras eliminar coincide con la referencia",
                arbol.listarCriticas().equals(criticas(referencia)));
    }

    // ------------------------------------------------------------------
    // Utilidades
    // ------------------------------------------------------------------

    /**
     * Único punto donde se construye una Estacion. Si el constructor del
     * modelo cambia, solo hay que ajustar este método.
     */
    private static Estacion crearEstacion(int numero, int bateria) {
        String codigo = String.format("CBA-%04d", numero);
        return new Estacion(codigo, numero, "Estación " + numero, "Córdoba",
                TipoEstacion.METEOROLOGICA, bateria, 0);
    }

    /** Baterías elegidas para que 20, 60 y 80 sean críticas. */
    private static ArbolBinarioBusqueda arbolDeReferencia() {
        ArbolBinarioBusqueda arbol = new ArbolBinarioBusqueda();
        int[][] datos = {{50, 85}, {30, 40}, {70, 55}, {20, 5}, {40, 20}, {60, 19}, {80, 0}};
        for (int[] d : datos) {
            arbol.insertar(crearEstacion(d[0], d[1]));
        }
        return arbol;
    }

    /** Estaciones presentes en la referencia con número en [desde, hasta], en orden. */
    private static List<Estacion> enRango(Estacion[] referencia, int desde, int hasta) {
        List<Estacion> resultado = new ArrayList<>();
        for (int n = desde; n <= hasta; n++) {
            if (referencia[n] != null) {
                resultado.add(referencia[n]);
            }
        }
        return resultado;
    }

    private static List<Estacion> criticas(Estacion[] referencia) {
        List<Estacion> resultado = new ArrayList<>();
        for (Estacion e : enRango(referencia, 0, 9999)) {
            if (e.esCritica()) {
                resultado.add(e);
            }
        }
        return resultado;
    }

    private static List<Integer> numeros(List<Estacion> estaciones) {
        List<Integer> resultado = new ArrayList<>();
        for (Estacion e : estaciones) {
            resultado.add(e.getNumero());
        }
        return resultado;
    }

    private static boolean estaOrdenado(List<Estacion> estaciones) {
        for (int i = 1; i < estaciones.size(); i++) {
            if (estaciones.get(i - 1).getNumero() >= estaciones.get(i).getNumero()) {
                return false;
            }
        }
        return true;
    }

    private static boolean eliminarSinError(ArbolBinarioBusqueda arbol, int numero) {
        try {
            arbol.eliminar(numero);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static boolean lanzaIllegalArgument(Runnable accion) {
        try {
            accion.run();
            return false;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    private static void verificar(String descripcion, boolean condicion) {
        if (condicion) {
            ok++;
            System.out.println("  [OK]    " + descripcion);
        } else {
            fallas++;
            System.out.println("  [FALLA] " + descripcion);
        }
    }

    private static void mostrar(String etiqueta, List<Estacion> estaciones) {
        System.out.println("  " + etiqueta + ": " + numeros(estaciones));
    }

    private static void titulo(String texto) {
        System.out.println();
        System.out.println("--- " + texto + " ---");
    }
}
