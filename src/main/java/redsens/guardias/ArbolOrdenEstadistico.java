package redsens.guardias;

/**
 * ABB propio de claves 1..N, construido equilibrado por puntos medios.
 * Solo admite consultas y bajas: la altura nunca supera la inicial O(log N).
 * No necesita rotaciones porque no se insertan claves después de construirlo.
 */
public final class ArbolOrdenEstadistico {
    private static final class Nodo {
        private int tecnico;
        private int cantidad;
        private Nodo izquierdo;
        private Nodo derecho;
        private Nodo(int tecnico) { this.tecnico = tecnico; }
    }

    private Nodo raiz;

    public ArbolOrdenEstadistico(int n) {
        if (n < 1) { throw new IllegalArgumentException("N debe ser positivo"); }
        raiz = construir(1, n);
    }

    private Nodo construir(int desde, int hasta) {
        if (desde > hasta) { return null; } // Caso base: intervalo vacío.
        int medio = desde + (hasta - desde) / 2;
        Nodo nodo = new Nodo(medio);
        nodo.izquierdo = construir(desde, medio - 1);
        nodo.derecho = construir(medio + 1, hasta);
        actualizar(nodo);
        return nodo;
    }

    public int tamanio() { return cantidad(raiz); }
    private static int cantidad(Nodo n) { return n == null ? 0 : n.cantidad; }
    private static void actualizar(Nodo n) {
        n.cantidad = 1 + cantidad(n.izquierdo) + cantidad(n.derecho);
    }

    /** Rango basado en cero dentro de las claves que siguen activas. */
    public int seleccionar(int k) {
        validarRango(k);
        Nodo nodo = raiz;
        while (nodo != null) {
            int izquierda = cantidad(nodo.izquierdo);
            if (k < izquierda) { nodo = nodo.izquierdo; }
            else if (k > izquierda) { k -= izquierda + 1; nodo = nodo.derecho; }
            else { return nodo.tecnico; }
        }
        throw new IllegalStateException("Metadatos inconsistentes");
    }

    public int eliminarK(int k) {
        int eliminado = seleccionar(k); // También valida antes de modificar.
        raiz = eliminar(raiz, k);
        return eliminado;
    }

    private Nodo eliminar(Nodo nodo, int k) {
        int izquierda = cantidad(nodo.izquierdo);
        if (k < izquierda) { nodo.izquierdo = eliminar(nodo.izquierdo, k); }
        else if (k > izquierda) { nodo.derecho = eliminar(nodo.derecho, k - izquierda - 1); }
        else {
            if (nodo.izquierdo == null) { return nodo.derecho; }
            if (nodo.derecho == null) { return nodo.izquierdo; }
            Nodo sucesor = nodo.derecho;
            while (sucesor.izquierdo != null) { sucesor = sucesor.izquierdo; }
            nodo.tecnico = sucesor.tecnico;
            nodo.derecho = eliminar(nodo.derecho, 0);
        }
        actualizar(nodo);
        return nodo;
    }

    private void validarRango(int k) {
        if (k < 0 || k >= tamanio()) {
            throw new IllegalArgumentException("Rango k fuera de los técnicos activos");
        }
    }

    /** Validador de orden y metadatos usado por las pruebas del paquete. */
    boolean estructuraValida() { return comprobar(raiz, 0L, (long) Integer.MAX_VALUE + 1) >= 0; }
    private int comprobar(Nodo nodo, long menor, long mayor) {
        if (nodo == null) { return 0; }
        if (nodo.tecnico <= menor || nodo.tecnico >= mayor) { return -1; }
        int izq = comprobar(nodo.izquierdo, menor, nodo.tecnico);
        int der = comprobar(nodo.derecho, nodo.tecnico, mayor);
        return izq < 0 || der < 0 || nodo.cantidad != 1 + izq + der ? -1 : nodo.cantidad;
    }
}
