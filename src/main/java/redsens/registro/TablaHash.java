package redsens.registro;

import redsens.modelo.Estacion;

/**
 * Registro propio con direccionamiento abierto y eliminación perezosa.
 * Esta primera entrega implementa LINEAL con capacidad fija. La ampliación
 * automática y CUADRATICA corresponden a la segunda parte de M1.
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

    private final Casilla[] casillas;
    private final EstrategiaExploracion estrategia;
    private int tamanio;
    private int sondeosUltimaOperacion;
    private int colisionesUltimaOperacion;
    private long sondeosTotales;
    private long colisionesTotales;

    public TablaHash(int capacidad) {
        this(capacidad, EstrategiaExploracion.LINEAL);
    }

    public TablaHash(int capacidad, EstrategiaExploracion estrategia) {
        if (capacidad <= 0) {
            throw new IllegalArgumentException("La capacidad debe ser positiva");
        }
        if (estrategia == null) {
            throw new IllegalArgumentException("La estrategia no puede ser nula");
        }
        this.casillas = new Casilla[capacidad];
        this.estrategia = estrategia;
    }

    /** Rechaza duplicados; una tabla llena no pierde ni reemplaza estaciones. */
    public void insertar(Estacion estacion) {
        iniciarOperacion();
        if (estacion == null) {
            throw new IllegalArgumentException("La estación no puede ser nula");
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
