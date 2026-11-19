package redsens.red;

/**
 * Arista de la lista de adyacencia: desde una localidad se llega a
 * {@code destino} recorriendo {@code km} kilómetros.
 */
public record Ruta(int destino, int km) {
    public Ruta {
        if (km <= 0) {
            throw new IllegalArgumentException("La distancia debe ser positiva");
        }
    }
}
