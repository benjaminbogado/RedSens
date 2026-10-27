package redsens.registro;

/** Calcula la posición del intento i, empezando en i = 0. */
public enum EstrategiaExploracion {
    LINEAL {
        @Override
        public int posicion(int inicio, int intento, int capacidad) {
            return (int) (((long) inicio + intento) % capacidad);
        }
    },
    /**
     * (h + i²) % m. Con m primo, las ⌈m/2⌉ primeras posiciones son todas
     * distintas: si a lo sumo la mitad de las casillas está ocupada (contando
     * las marcas de borrado) siempre se alcanza una libre.
     */
    CUADRATICA {
        @Override
        public int posicion(int inicio, int intento, int capacidad) {
            return (int) (((long) inicio + (long) intento * intento) % capacidad);
        }
    };

    /**
     * Punto común para incorporar CUADRATICA sin duplicar las operaciones.
     * La estrategia debe mantener el intento 0 en la posición inicial.
     */
    public abstract int posicion(int inicio, int intento, int capacidad);
}
