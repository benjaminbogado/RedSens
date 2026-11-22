package redsens.guardias;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/** Solución simple O(N*M), con lista enlazada e iterador circular. */
public final class JosephusLista {
    private JosephusLista() { }

    public static CalendarioGuardias resolver(int n, int m) {
        validar(n, m);
        LinkedList<Integer> ronda = new LinkedList<>();
        for (int tecnico = 1; tecnico <= n; tecnico++) {
            ronda.add(tecnico);
        }
        List<Integer> asignaciones = new ArrayList<>(n - 1);
        Iterator<Integer> cursor = ronda.iterator();
        while (ronda.size() > 1) {
            // M son pasadas: se omiten M técnicos y sale el siguiente.
            for (int pasada = 0; pasada < m; pasada++) {
                if (!cursor.hasNext()) { cursor = ronda.iterator(); }
                cursor.next();
            }
            if (!cursor.hasNext()) { cursor = ronda.iterator(); }
            asignaciones.add(cursor.next());
            cursor.remove();
        }
        return new CalendarioGuardias(asignaciones, ronda.getFirst());
    }

    static void validar(int n, int m) {
        if (n < 1 || m < 1) {
            throw new IllegalArgumentException("N y M deben ser positivos");
        }
    }
}
