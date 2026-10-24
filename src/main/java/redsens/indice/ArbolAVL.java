package redsens.indice;

import redsens.modelo.Estacion;

import java.util.ArrayList;
import java.util.List;

/** Estructura base del árbol AVL para indexar estaciones. */
public class ArbolAVL {
    private static class Nodo {
        private final Estacion estacion;
        private Nodo izquierdo;
        private Nodo derecho;
        private int altura;

        private Nodo(Estacion estacion) {
            this.estacion = estacion;
            this.altura = 0;
        }
    }

    private Nodo raiz;

    /** Construye un árbol sin nodos. */
    public ArbolAVL() {
        this.raiz = null;
    }

    /** Devuelve la altura de la raíz; para un árbol vacío devuelve -1. */
    public int altura() {
        return altura(raiz);
    }

    /** Cuenta los nodos del árbol. */
    public int cantidadNodos() {
        return cantidadNodos(raiz);
    }

    private int cantidadNodos(Nodo nodo) {
        if (nodo == null) {
            return 0;
        }
        return 1 + cantidadNodos(nodo.izquierdo) + cantidadNodos(nodo.derecho);
    }

    /** Cuenta los nodos que no tienen hijos. */
    public int cantidadHojas() {
        return cantidadHojas(raiz);
    }

    private int cantidadHojas(Nodo nodo) {
        if (nodo == null) {
            return 0;
        }
        if (nodo.izquierdo == null && nodo.derecho == null) {
            return 1;
        }
        return cantidadHojas(nodo.izquierdo) + cantidadHojas(nodo.derecho);
    }

    /** Devuelve las estaciones en orden ascendente por número. */
    public List<Estacion> inorden() {
        List<Estacion> estaciones = new ArrayList<>();
        inorden(raiz, estaciones);
        return estaciones;
    }

    private void inorden(Nodo nodo, List<Estacion> estaciones) {
        if (nodo != null) {
            inorden(nodo.izquierdo, estaciones);
            estaciones.add(nodo.estacion);
            inorden(nodo.derecho, estaciones);
        }
    }

    /** Devuelve las estaciones en recorrido raíz, izquierda, derecha. */
    public List<Estacion> preorden() {
        List<Estacion> estaciones = new ArrayList<>();
        preorden(raiz, estaciones);
        return estaciones;
    }

    private void preorden(Nodo nodo, List<Estacion> estaciones) {
        if (nodo != null) {
            estaciones.add(nodo.estacion);
            preorden(nodo.izquierdo, estaciones);
            preorden(nodo.derecho, estaciones);
        }
    }

    /** Devuelve las estaciones en recorrido izquierda, derecha, raíz. */
    public List<Estacion> postorden() {
        List<Estacion> estaciones = new ArrayList<>();
        postorden(raiz, estaciones);
        return estaciones;
    }

    private void postorden(Nodo nodo, List<Estacion> estaciones) {
        if (nodo != null) {
            postorden(nodo.izquierdo, estaciones);
            postorden(nodo.derecho, estaciones);
            estaciones.add(nodo.estacion);
        }
    }

    /** Devuelve en orden las estaciones cuyo número está dentro del rango inclusivo. */
    public List<Estacion> listarPorRango(int desde, int hasta) {
        if (desde > hasta) {
            throw new IllegalArgumentException("El límite inicial no puede ser mayor que el final");
        }
        List<Estacion> estaciones = new ArrayList<>();
        listarPorRango(raiz, desde, hasta, estaciones);
        return estaciones;
    }

    private void listarPorRango(Nodo nodo, int desde, int hasta, List<Estacion> estaciones) {
        if (nodo == null) {
            return;
        }

        int numero = nodo.estacion.getNumero();
        if (numero > desde) {
            listarPorRango(nodo.izquierdo, desde, hasta, estaciones);
        }
        if (numero >= desde && numero <= hasta) {
            estaciones.add(nodo.estacion);
        }
        if (numero < hasta) {
            listarPorRango(nodo.derecho, desde, hasta, estaciones);
        }
    }

    /** Devuelve las estaciones críticas en orden ascendente por número. */
    public List<Estacion> listarCriticas() {
        List<Estacion> estaciones = new ArrayList<>();
        listarCriticas(raiz, estaciones);
        return estaciones;
    }

    private void listarCriticas(Nodo nodo, List<Estacion> estaciones) {
        if (nodo != null) {
            listarCriticas(nodo.izquierdo, estaciones);
            if (nodo.estacion.esCritica()) {
                estaciones.add(nodo.estacion);
            }
            listarCriticas(nodo.derecho, estaciones);
        }
    }

    /** Busca una estación por su número, o devuelve null si no existe. */
    public Estacion buscar(int numero) {
        Nodo actual = raiz;
        while (actual != null) {
            int numeroActual = actual.estacion.getNumero();
            if (numero < numeroActual) {
                actual = actual.izquierdo;
            } else if (numero > numeroActual) {
                actual = actual.derecho;
            } else {
                return actual.estacion;
            }
        }
        return null;
    }

    /** Elimina la estación indicada, o falla si el número no existe. */
    public void eliminar(int numero) {
        raiz = eliminar(raiz, numero);
    }

    /** Inserta una estación usando su número como clave única. */
    public void insertar(Estacion estacion) {
        if (estacion == null) {
            throw new IllegalArgumentException("La estación no puede ser nula");
        }
        raiz = insertar(raiz, estacion);
    }

    private Nodo insertar(Nodo nodo, Estacion estacion) {
        if (nodo == null) {
            return new Nodo(estacion);
        }

        int numero = estacion.getNumero();
        int numeroNodo = nodo.estacion.getNumero();
        if (numero < numeroNodo) {
            nodo.izquierdo = insertar(nodo.izquierdo, estacion);
        } else if (numero > numeroNodo) {
            nodo.derecho = insertar(nodo.derecho, estacion);
        } else {
            throw new IllegalArgumentException("Ya existe una estación con el número " + numero);
        }

        return reequilibrar(nodo);
    }

    private Nodo eliminar(Nodo nodo, int numero) {
        if (nodo == null) {
            throw new IllegalArgumentException("No existe una estación con el número " + numero);
        }

        int numeroNodo = nodo.estacion.getNumero();
        if (numero < numeroNodo) {
            nodo.izquierdo = eliminar(nodo.izquierdo, numero);
        } else if (numero > numeroNodo) {
            nodo.derecho = eliminar(nodo.derecho, numero);
        } else if (nodo.izquierdo == null) {
            return nodo.derecho;
        } else if (nodo.derecho == null) {
            return nodo.izquierdo;
        } else {
            Nodo sucesor = minimo(nodo.derecho);
            Nodo nuevoSubarbolDerecho = eliminar(nodo.derecho, sucesor.estacion.getNumero());
            sucesor.izquierdo = nodo.izquierdo;
            sucesor.derecho = nuevoSubarbolDerecho;
            nodo = sucesor;
        }

        return reequilibrar(nodo);
    }

    private Nodo minimo(Nodo nodo) {
        Nodo actual = nodo;
        while (actual.izquierdo != null) {
            actual = actual.izquierdo;
        }
        return actual;
    }

    private Nodo reequilibrar(Nodo nodo) {
        actualizarAltura(nodo);
        int equilibrio = factorEquilibrio(nodo);

        // El factor se define como altura derecha menos altura izquierda.
        if (equilibrio > 1) {
            if (factorEquilibrio(nodo.derecho) < 0) {
                nodo.derecho = rotarDerecha(nodo.derecho);
            }
            return rotarIzquierda(nodo);
        }
        if (equilibrio < -1) {
            if (factorEquilibrio(nodo.izquierdo) > 0) {
                nodo.izquierdo = rotarIzquierda(nodo.izquierdo);
            }
            return rotarDerecha(nodo);
        }
        return nodo;
    }

    private int altura(Nodo nodo) {
        return nodo == null ? -1 : nodo.altura;
    }

    private void actualizarAltura(Nodo nodo) {
        nodo.altura = 1 + Math.max(altura(nodo.izquierdo), altura(nodo.derecho));
    }

    private int factorEquilibrio(Nodo nodo) {
        return altura(nodo.derecho) - altura(nodo.izquierdo);
    }

    private Nodo rotarIzquierda(Nodo pivote) {
        Nodo nuevaRaiz = pivote.derecho;
        Nodo subarbol = nuevaRaiz.izquierdo;

        nuevaRaiz.izquierdo = pivote;
        pivote.derecho = subarbol;

        actualizarAltura(pivote);
        actualizarAltura(nuevaRaiz);
        return nuevaRaiz;
    }

    private Nodo rotarDerecha(Nodo pivote) {
        Nodo nuevaRaiz = pivote.izquierdo;
        Nodo subarbol = nuevaRaiz.derecho;

        nuevaRaiz.derecho = pivote;
        pivote.izquierdo = subarbol;

        actualizarAltura(pivote);
        actualizarAltura(nuevaRaiz);
        return nuevaRaiz;
    }
}
