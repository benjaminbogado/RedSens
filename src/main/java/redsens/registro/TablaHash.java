package redsens.registro;

import redsens.modelo.Estacion;

/**
 * Registro propio con direccionamiento abierto y eliminación perezosa.
 * Esta primera entrega implementa LINEAL con capacidad fija. La ampliación
 * automática y CUADRATICA corresponden a la segunda parte de M1.
 *
 * <p>Segunda parte (integrante 4). Dos modos:
 * <ul>
 *   <li>Capacidad fija: {@code new TablaHash(capacidad, estrategia)}. Si no hay
 *       casilla disponible, insertar lanza IllegalStateException.</li>
 *   <li>Ampliable: {@code new TablaHash(capacidad, estrategia, umbral)} o
 *       {@link #ampliable}. Antes de insertar, si (activas + borradas + 1)
 *       superaría umbral · capacidad, la tabla crece al primer primo mayor o
 *       igual al doble y reinserta las activas, descartando las marcas.</li>
 * </ul>
 */
public class TablaHash {
    /** null en el arreglo significa que la casilla nunca estuvo ocupada. */
    private static final class Casilla {
        // Una casilla existente con estacion == null es una marca de borrado.
        private Estacion estacion;

        private Casilla(Estacion estacion) {
            this.estacion = estacion;
        }
    }

    private Casilla[] casillas;
    private final EstrategiaExploracion estrategia;
    /** Umbral de ocupación; 0 indica capacidad fija (sin ampliación). */
    private final double umbralCarga;
    private int tamanio;
    private int borradas;
    private int rehashings;
    private int limpiezas;
    private int sondeosUltimaOperacion;
    private int colisionesUltimaOperacion;
    private long sondeosTotales;
    private long colisionesTotales;

    public TablaHash(int capacidad) {
        this(capacidad, EstrategiaExploracion.LINEAL);
    }

    /** Tabla de capacidad fija. */
    public TablaHash(int capacidad, EstrategiaExploracion estrategia) {
        if (capacidad <= 0) {
            throw new IllegalArgumentException("La capacidad debe ser positiva");
        }
        if (estrategia == null) {
            throw new IllegalArgumentException("La estrategia no puede ser nula");
        }
        this.casillas = new Casilla[capacidad];
        this.estrategia = estrategia;
        this.umbralCarga = 0;
    }

    /**
     * Tabla ampliable con un umbral explícito, entre 0 y 1 (exclusivos).
     * La capacidad inicial se lleva al primer primo mayor o igual, porque la
     * garantía de la exploración cuadrática necesita una capacidad prima.
     */
    public TablaHash(int capacidad, EstrategiaExploracion estrategia, double umbralCarga) {
        if (capacidad <= 0) {
            throw new IllegalArgumentException("La capacidad debe ser positiva");
        }
        if (estrategia == null) {
            throw new IllegalArgumentException("La estrategia no puede ser nula");
        }
        if (!(umbralCarga > 0 && umbralCarga < 1)) {
            throw new IllegalArgumentException("El umbral de carga debe estar entre 0 y 1");
        }
        this.casillas = new Casilla[Primos.siguientePrimo(capacidad)];
        this.estrategia = estrategia;
        this.umbralCarga = umbralCarga;
    }

    /** Tabla ampliable con el umbral por defecto de la estrategia (ver umbralPorDefecto). */
    public static TablaHash ampliable(int capacidad, EstrategiaExploracion estrategia) {
        return new TablaHash(capacidad, estrategia, umbralPorDefecto(estrategia));
    }

    /**
     * Umbral de ocupación por defecto de cada estrategia.
     * <ul>
     *   <li>LINEAL 0,75: siempre encuentra lugar si queda alguno, el umbral solo
     *       controla el costo. Con λ = 0,75 una búsqueda no exitosa cuesta en
     *       teoría ½(1 + 1/(1−λ)²) = 8,5 sondeos; con λ = 0,9 ya son 50,5.</li>
     *   <li>CUADRATICA 0,5: con m primo, las ⌈m/2⌉ primeras posiciones de la
     *       secuencia son distintas; por encima de 0,5 se pierde la garantía de
     *       encontrar una casilla libre.</li>
     * </ul>
     */
    public static double umbralPorDefecto(EstrategiaExploracion estrategia) {
        if (estrategia == null) {
            throw new IllegalArgumentException("La estrategia no puede ser nula");
        }
        return estrategia == EstrategiaExploracion.CUADRATICA ? 0.5 : 0.75;
    }

    /** Rechaza duplicados; una tabla llena no pierde ni reemplaza estaciones. */
    public void insertar(Estacion estacion) {
        iniciarOperacion();
        if (estacion == null) {
            throw new IllegalArgumentException("La estación no puede ser nula");
        }
        if (esAmpliable() && tamanio + borradas + 1 > umbralCarga * casillas.length
                && !contieneSinMedir(estacion.getCodigo())) {
            ampliar();
        }
        int inicio = hash(estacion.getCodigo());
        int primeraBorrada = -1;
        for (int intento = 0; intento < casillas.length; intento++) {
            int posicion = estrategia.posicion(inicio, intento, casillas.length);
            Casilla casilla = sondear(posicion);
            if (casilla == null) {
                ocupar(primeraBorrada >= 0 ? primeraBorrada : posicion, estacion);
                return;
            }
            if (casilla.estacion == null) {
                if (primeraBorrada < 0) {
                    primeraBorrada = posicion;
                }
            } else if (casilla.estacion.getCodigo().equals(estacion.getCodigo())) {
                throw new IllegalArgumentException("El código ya existe: " + estacion.getCodigo());
            } else {
                registrarColision();
            }
        }
        if (primeraBorrada >= 0) {
            ocupar(primeraBorrada, estacion);
            return;
        }
        throw new IllegalStateException("No hay una casilla disponible; falta ampliar la tabla");
    }

    /** Devuelve la misma instancia almacenada, o null si el código no existe. */
    public Estacion buscar(String codigo) {
        iniciarOperacion();
        validarCodigo(codigo);
        int posicion = localizar(codigo);
        return posicion < 0 ? null : casillas[posicion].estacion;
    }

    /** Mantiene la marca de borrado para no cortar las cadenas de búsqueda. */
    public void eliminar(String codigo) {
        iniciarOperacion();
        validarCodigo(codigo);
        int posicion = localizar(codigo);
        if (posicion < 0) {
            throw new IllegalArgumentException("El código no existe: " + codigo);
        }
        casillas[posicion].estacion = null;
        tamanio--;
        borradas++;
    }

    private int localizar(String codigo) {
        int inicio = hash(codigo);
        for (int intento = 0; intento < casillas.length; intento++) {
            int posicion = estrategia.posicion(inicio, intento, casillas.length);
            Casilla casilla = sondear(posicion);
            if (casilla == null) {
                return -1;
            }
            if (casilla.estacion != null) {
                if (casilla.estacion.getCodigo().equals(codigo)) {
                    return posicion;
                }
                registrarColision();
            }
        }
        return -1;
    }

    /**
     * Hace lugar cuando la ocupación superaría el umbral. Si casi todo lo
     * ocupado son marcas de borrado (las activas + 1 no llegan a la mitad del
     * umbral), reconstruye con la misma capacidad para limpiarlas; si no,
     * crece al primer primo mayor o igual al doble. Las reinserciones no se
     * suman a los contadores de sondeos y colisiones.
     */
    private void ampliar() {
        int nueva;
        if (tamanio + 1 <= umbralCarga * casillas.length / 2) {
            nueva = casillas.length;
            limpiezas++;
        } else {
            nueva = Primos.siguientePrimo(2 * casillas.length);
            while (tamanio + 1 > umbralCarga * nueva) {
                nueva = Primos.siguientePrimo(2 * nueva);
            }
            rehashings++;
        }
        Casilla[] anteriores = casillas;
        casillas = new Casilla[nueva];
        borradas = 0;
        for (Casilla casilla : anteriores) {
            if (casilla != null && casilla.estacion != null) {
                colocarSinMedir(casilla.estacion);
            }
        }
    }

    /** Reinserción interna del rehashing: no cuenta sondeos ni colisiones. */
    private void colocarSinMedir(Estacion estacion) {
        int inicio = hash(estacion.getCodigo());
        for (int intento = 0; intento < casillas.length; intento++) {
            int posicion = estrategia.posicion(inicio, intento, casillas.length);
            if (casillas[posicion] == null) {
                casillas[posicion] = new Casilla(estacion);
                return;
            }
        }
        throw new IllegalStateException("No se pudo reubicar " + estacion.getCodigo());
    }

    /** Evita ampliar la tabla por una inserción que va a ser rechazada como duplicado. */
    private boolean contieneSinMedir(String codigo) {
        int inicio = hash(codigo);
        for (int intento = 0; intento < casillas.length; intento++) {
            Casilla casilla = casillas[estrategia.posicion(inicio, intento, casillas.length)];
            if (casilla == null) {
                return false;
            }
            if (casilla.estacion != null && casilla.estacion.getCodigo().equals(codigo)) {
                return true;
            }
        }
        return false;
    }

    /** Hash polinómico de toda la cadena; se reduce en cada paso. */
    private int hash(String codigo) {
        int resultado = 0;
        for (int i = 0; i < codigo.length(); i++) {
            resultado = (int) ((31L * resultado + codigo.charAt(i)) % casillas.length);
        }
        return resultado;
    }

    private static void validarCodigo(String codigo) {
        if (codigo == null || !codigo.matches("CBA-[0-9]{4}")) {
            throw new IllegalArgumentException("El código debe tener el formato CBA-NNNN");
        }
    }

    private void ocupar(int posicion, Estacion estacion) {
        if (casillas[posicion] != null) {
            borradas--; // se reutiliza una marca de borrado
        }
        casillas[posicion] = new Casilla(estacion);
        tamanio++;
    }

    private Casilla sondear(int posicion) {
        sondeosUltimaOperacion++;
        sondeosTotales++;
        return casillas[posicion];
    }

    private void registrarColision() {
        colisionesUltimaOperacion++;
        colisionesTotales++;
    }

    private void iniciarOperacion() {
        sondeosUltimaOperacion = 0;
        colisionesUltimaOperacion = 0;
    }

    public int tamanio() {
        return tamanio;
    }

    public boolean estaVacia() {
        return tamanio == 0;
    }

    public int capacidad() {
        return casillas.length;
    }

    /** Ocupación de estaciones activas; no incluye marcas de borrado. */
    public double factorCarga() {
        return (double) tamanio / casillas.length;
    }

    /** (Activas + marcas de borrado) / capacidad: es lo que decide la ampliación. */
    public double factorOcupacion() {
        return (double) (tamanio + borradas) / casillas.length;
    }

    public int cantidadBorradas() {
        return borradas;
    }

    public boolean esAmpliable() {
        return umbralCarga > 0;
    }

    /** Umbral de ocupación, o 0 si la tabla es de capacidad fija. */
    public double getUmbralCarga() {
        return umbralCarga;
    }

    public EstrategiaExploracion getEstrategia() {
        return estrategia;
    }

    /** Veces que la tabla creció al siguiente primo del doble. No se reinicia con reiniciarContadores. */
    public int getRehashings() {
        return rehashings;
    }

    /** Reconstrucciones con la misma capacidad para descartar marcas de borrado. */
    public int getLimpiezas() {
        return limpiezas;
    }

    public int getSondeosUltimaOperacion() {
        return sondeosUltimaOperacion;
    }

    public int getColisionesUltimaOperacion() {
        return colisionesUltimaOperacion;
    }

    public long getSondeosTotales() {
        return sondeosTotales;
    }

    public long getColisionesTotales() {
        return colisionesTotales;
    }

    /** Reinicia las mediciones sin modificar estaciones ni capacidad. */
    public void reiniciarContadores() {
        iniciarOperacion();
        sondeosTotales = 0;
        colisionesTotales = 0;
    }
}
