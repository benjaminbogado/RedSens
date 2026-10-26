package redsens.indice;

import java.util.ArrayList;
import java.util.List;

import redsens.modelo.Estacion;

/**
 * Árbol binario de búsqueda (ABB) de estaciones, ordenado por el número de
 * estación (por ejemplo, CBA-0347 tiene número 347).
 *
 * <p>Contrato compartido con {@link ArbolAVL}:
 * <ul>
 *   <li>No se admiten números duplicados ni estaciones nulas: {@link #insertar(Estacion)} lanza excepción.</li>
 *   <li>{@link #buscar(int)} devuelve {@code null} si la estación no existe.</li>
 *   <li>{@link #eliminar(int)} lanza excepción si la estación no existe.</li>
 *   <li>{@link #altura()} cuenta aristas: el árbol vacío tiene altura -1 y una hoja, 0.</li>
 *   <li>Los listados nunca devuelven {@code null}: si no hay datos, devuelven una lista vacía.</li>
 * </ul>
 *
 * <p>Las operaciones son recursivas. Al no estar balanceado, si se insertan
 * datos ya ordenados el árbol degenera en una lista y su altura es n.
 */
public class ArbolBinarioBusqueda {

    /** Nodo interno del árbol. Guarda una estación y sus dos hijos. */
    private static class Nodo {
        private Estacion estacion;
        private Nodo izquierdo;
        private Nodo derecho;

        Nodo(Estacion estacion) {
            this.estacion = estacion;
        }

        int clave() {
            return estacion.getNumero();
        }

        boolean esHoja() {
            return izquierdo == null && derecho == null;
        }
    }

    private Nodo raiz;
    private int tamanio;

    /** Crea un árbol vacío. */
    public ArbolBinarioBusqueda() {
        this.raiz = null;
        this.tamanio = 0;
    }

    // ------------------------------------------------------------------
    // Inserción
    // ------------------------------------------------------------------

    /**
     * Inserta una estación en el árbol según su número.
     *
     * @param estacion estación a insertar, no puede ser {@code null}
     * @throws IllegalArgumentException si la estación es {@code null} o si ya
     *                                  existe una estación con el mismo número
     */
    public void insertar(Estacion estacion) {
        if (estacion == null) {
            throw new IllegalArgumentException("La estación a insertar no puede ser null");
        }
        raiz = insertar(raiz, estacion);
        tamanio++;
    }

    private Nodo insertar(Nodo actual, Estacion estacion) {
        if (actual == null) {
            return new Nodo(estacion);
        }
        int numero = estacion.getNumero();
        if (numero < actual.clave()) {
            actual.izquierdo = insertar(actual.izquierdo, estacion);
        } else if (numero > actual.clave()) {
            actual.derecho = insertar(actual.derecho, estacion);
        } else {
            throw new IllegalArgumentException(
                    "Ya existe una estación con el número " + numero);
        }
        return actual;
    }

    // ------------------------------------------------------------------
    // Búsqueda
    // ------------------------------------------------------------------

    /**
     * Busca una estación por su número.
     *
     * @param numero número de la estación
     * @return la estación encontrada, o {@code null} si no existe
     */
    public Estacion buscar(int numero) {
        Nodo nodo = buscar(raiz, numero);
        return nodo == null ? null : nodo.estacion;
    }

    private Nodo buscar(Nodo actual, int numero) {
        if (actual == null) {
            return null;
        }
        if (numero < actual.clave()) {
            return buscar(actual.izquierdo, numero);
        }
        if (numero > actual.clave()) {
            return buscar(actual.derecho, numero);
        }
        return actual;
    }

    /**
     * Indica si existe una estación con el número dado.
     *
     * @param numero número de la estación
     * @return {@code true} si existe
     */
    public boolean contiene(int numero) {
        return buscar(raiz, numero) != null;
    }

    // ------------------------------------------------------------------
    // Eliminación
    // ------------------------------------------------------------------

    /**
     * Elimina la estación con el número dado. Contempla los tres casos:
     * nodo hoja, nodo con un solo hijo y nodo con dos hijos (en este último
     * se reemplaza por su sucesor inorden, el mínimo del subárbol derecho).
     *
     * @param numero número de la estación a eliminar
     * @throws IllegalArgumentException si no existe una estación con ese número
     */
    public void eliminar(int numero) {
        raiz = eliminar(raiz, numero);
        tamanio--;
    }

    private Nodo eliminar(Nodo actual, int numero) {
        if (actual == null) {
            throw new IllegalArgumentException("No existe una estación con el número " + numero);
        }
        if (numero < actual.clave()) {
            actual.izquierdo = eliminar(actual.izquierdo, numero);
            return actual;
        }
        if (numero > actual.clave()) {
            actual.derecho = eliminar(actual.derecho, numero);
            return actual;
        }

        // Caso 1: nodo hoja -> se quita directamente.
        if (actual.esHoja()) {
            return null;
        }
        // Caso 2: nodo con un solo hijo -> el hijo ocupa su lugar.
        if (actual.izquierdo == null) {
            return actual.derecho;
        }
        if (actual.derecho == null) {
            return actual.izquierdo;
        }
        // Caso 3: nodo con dos hijos -> se copia el sucesor y se lo borra del subárbol derecho.
        Nodo sucesor = minimo(actual.derecho);
        actual.estacion = sucesor.estacion;
        actual.derecho = eliminar(actual.derecho, sucesor.clave());
        return actual;
    }

    private Nodo minimo(Nodo actual) {
        return actual.izquierdo == null ? actual : minimo(actual.izquierdo);
    }

    // ------------------------------------------------------------------
    // Recorridos
    // ------------------------------------------------------------------

    /**
     * Recorrido inorden (izquierdo, raíz, derecho). Devuelve las estaciones
     * ordenadas de menor a mayor número.
     *
     * @return lista de estaciones en inorden
     */
    public List<Estacion> inorden() {
        List<Estacion> resultado = new ArrayList<>();
        inorden(raiz, resultado);
        return resultado;
    }

    private void inorden(Nodo actual, List<Estacion> resultado) {
        if (actual == null) {
            return;
        }
        inorden(actual.izquierdo, resultado);
        resultado.add(actual.estacion);
        inorden(actual.derecho, resultado);
    }

    /**
     * Recorrido preorden (raíz, izquierdo, derecho).
     *
     * @return lista de estaciones en preorden
     */
    public List<Estacion> preorden() {
        List<Estacion> resultado = new ArrayList<>();
        preorden(raiz, resultado);
        return resultado;
    }

    private void preorden(Nodo actual, List<Estacion> resultado) {
        if (actual == null) {
            return;
        }
        resultado.add(actual.estacion);
        preorden(actual.izquierdo, resultado);
        preorden(actual.derecho, resultado);
    }

    /**
     * Recorrido postorden (izquierdo, derecho, raíz).
     *
     * @return lista de estaciones en postorden
     */
    public List<Estacion> postorden() {
        List<Estacion> resultado = new ArrayList<>();
        postorden(raiz, resultado);
        return resultado;
    }

    private void postorden(Nodo actual, List<Estacion> resultado) {
        if (actual == null) {
            return;
        }
        postorden(actual.izquierdo, resultado);
        postorden(actual.derecho, resultado);
        resultado.add(actual.estacion);
    }

    // ------------------------------------------------------------------
    // Estadísticas (recursivas)
    // ------------------------------------------------------------------

    /**
     * Calcula la altura del árbol, contada en aristas (igual que el AVL):
     * el árbol vacío tiene altura -1 y un árbol con solo la raíz, altura 0.
     *
     * @return altura del árbol
     */
    public int altura() {
        return altura(raiz);
    }

    private int altura(Nodo actual) {
        if (actual == null) {
            return -1;
        }
        return 1 + Math.max(altura(actual.izquierdo), altura(actual.derecho));
    }

    /**
     * Cuenta recursivamente la cantidad de nodos del árbol.
     *
     * @return cantidad de nodos
     */
    public int cantidadNodos() {
        return cantidadNodos(raiz);
    }

    private int cantidadNodos(Nodo actual) {
        if (actual == null) {
            return 0;
        }
        return 1 + cantidadNodos(actual.izquierdo) + cantidadNodos(actual.derecho);
    }

    /**
     * Cuenta recursivamente la cantidad de hojas (nodos sin hijos).
     *
     * @return cantidad de hojas
     */
    public int cantidadHojas() {
        return cantidadHojas(raiz);
    }

    private int cantidadHojas(Nodo actual) {
        if (actual == null) {
            return 0;
        }
        if (actual.esHoja()) {
            return 1;
        }
        return cantidadHojas(actual.izquierdo) + cantidadHojas(actual.derecho);
    }

    // ------------------------------------------------------------------
    // Consultas específicas
    // ------------------------------------------------------------------

    /**
     * Lista las estaciones cuyo número está en el rango [desde, hasta]
     * (ambos inclusive), ordenadas por número. Poda las ramas que quedan
     * fuera del rango.
     *
     * @param desde límite inferior del rango
     * @param hasta límite superior del rango
     * @return estaciones dentro del rango, ordenadas por número
     * @throws IllegalArgumentException si {@code desde > hasta}
     */
    public List<Estacion> listarPorRango(int desde, int hasta) {
        if (desde > hasta) {
            throw new IllegalArgumentException("El límite inicial no puede ser mayor que el final");
        }
        List<Estacion> resultado = new ArrayList<>();
        listarPorRango(raiz, desde, hasta, resultado);
        return resultado;
    }

    private void listarPorRango(Nodo actual, int desde, int hasta, List<Estacion> resultado) {
        if (actual == null) {
            return;
        }
        int clave = actual.clave();
        if (clave > desde) {
            listarPorRango(actual.izquierdo, desde, hasta, resultado);
        }
        if (clave >= desde && clave <= hasta) {
            resultado.add(actual.estacion);
        }
        if (clave < hasta) {
            listarPorRango(actual.derecho, desde, hasta, resultado);
        }
    }

    /**
     * Lista las estaciones críticas (batería menor a 20, según
     * {@link Estacion#esCritica()}), ordenadas por número.
     *
     * @return estaciones críticas ordenadas por número
     */
    public List<Estacion> listarCriticas() {
        List<Estacion> resultado = new ArrayList<>();
        listarCriticas(raiz, resultado);
        return resultado;
    }

    private void listarCriticas(Nodo actual, List<Estacion> resultado) {
        if (actual == null) {
            return;
        }
        listarCriticas(actual.izquierdo, resultado);
        if (actual.estacion.esCritica()) {
            resultado.add(actual.estacion);
        }
        listarCriticas(actual.derecho, resultado);
    }

    // ------------------------------------------------------------------
    // Utilidades
    // ------------------------------------------------------------------

    /**
     * Devuelve la cantidad de estaciones almacenadas (contador mantenido en
     * inserciones y eliminaciones; debe coincidir con {@link #cantidadNodos()}).
     *
     * @return cantidad de estaciones
     */
    public int tamanio() {
        return tamanio;
    }

    /**
     * Indica si el árbol está vacío.
     *
     * @return {@code true} si no tiene estaciones
     */
    public boolean estaVacio() {
        return raiz == null;
    }
}
