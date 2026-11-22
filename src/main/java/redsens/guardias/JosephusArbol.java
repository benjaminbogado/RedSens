package redsens.guardias;

import java.util.ArrayList;
import java.util.List;

/** Solución O(N log N) con acceso al k-ésimo técnico activo. */
public final class JosephusArbol {
    private JosephusArbol() { }

    public static CalendarioGuardias resolver(int n, int m) {
        JosephusLista.validar(n, m);
        ArbolOrdenEstadistico ronda = new ArbolOrdenEstadistico(n);
        List<Integer> asignaciones = new ArrayList<>(n - 1);
        int indice = 0;
        while (ronda.tamanio() > 1) {
            // long evita desbordar al sumar M. El próximo cuenta desde el sucesor.
            indice = (int) ((indice + (long) m) % ronda.tamanio());
            asignaciones.add(ronda.eliminarK(indice));
        }
        return new CalendarioGuardias(asignaciones, ronda.seleccionar(0));
    }
}
