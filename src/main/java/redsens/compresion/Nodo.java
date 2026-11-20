package redsens.compresion;

/** Nodo de un árbol Huffman: hoja o unión de dos subárboles. */
public class Nodo {
    private final Integer simbolo;
    private final long frecuencia;
    private final int simboloMinimo;
    private final Nodo izquierdo;
    private final Nodo derecho;

    public Nodo(Integer simbolo, long frecuencia) {
        this(simbolo, frecuencia, null, null);
    }

    public Nodo(long frecuencia, Nodo izquierdo, Nodo derecho) {
        this(null, frecuencia, izquierdo, derecho);
    }

    private Nodo(Integer simbolo, long frecuencia, Nodo izquierdo, Nodo derecho) {
        this.simbolo = simbolo;
        this.frecuencia = frecuencia;
        this.simboloMinimo = simbolo != null
                ? simbolo
                : Math.min(izquierdo.getSimboloMinimo(), derecho.getSimboloMinimo());
        this.izquierdo = izquierdo;
        this.derecho = derecho;
    }

    public boolean esHoja() {
        return simbolo != null;
    }

    public Integer getSimbolo() {
        return simbolo;
    }

    public long getFrecuencia() {
        return frecuencia;
    }

    public int getSimboloMinimo() {
        return simboloMinimo;
    }

    public Nodo getIzquierdo() {
        return izquierdo;
    }

    public Nodo getDerecho() {
        return derecho;
    }
}
