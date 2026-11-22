package redsens.guardias;

import java.util.Arrays;
import java.util.Locale;

/** Experimento obligatorio; construcción incluida, impresión fuera del tiempo. */
public final class ExperimentoJosephus {
    private static final int[] CANTIDADES = {1000, 10000, 100000};
    private static final int[] PASADAS = {3, 1000};
    private static volatile int control;
    private ExperimentoJosephus() { }

    public static void main(String[] args) {
        System.out.println("M5 Josephus | Java " + System.getProperty("java.version")
                + " | " + System.getProperty("os.name") + " " + System.getProperty("os.arch"));
        System.out.println("3 rondas de calentamiento, mediana de 5; incluye construcción y calendario.");
        System.out.println("N;M;lista_ms;arbol_ms;lista/arbol;suplente");
        for (int n : CANTIDADES) {
            for (int m : PASADAS) {
                for (int i = 0; i < 3; i++) {
                    consumir(JosephusLista.resolver(n, m));
                    consumir(JosephusArbol.resolver(n, m));
                }
                long[] lista = new long[5];
                long[] arbol = new long[5];
                CalendarioGuardias a = null, b = null;
                for (int i = 0; i < 5; i++) {
                    // Alternar el orden reduce el sesgo de medir siempre primero la lista.
                    if (i % 2 == 0) {
                        long inicio = System.nanoTime(); a = JosephusLista.resolver(n, m);
                        lista[i] = System.nanoTime() - inicio;
                        inicio = System.nanoTime(); b = JosephusArbol.resolver(n, m);
                        arbol[i] = System.nanoTime() - inicio;
                    } else {
                        long inicio = System.nanoTime(); b = JosephusArbol.resolver(n, m);
                        arbol[i] = System.nanoTime() - inicio;
                        inicio = System.nanoTime(); a = JosephusLista.resolver(n, m);
                        lista[i] = System.nanoTime() - inicio;
                    }
                    if (!a.equals(b)) { throw new AssertionError("Calendarios distintos"); }
                    consumir(a); consumir(b);
                }
                Arrays.sort(lista); Arrays.sort(arbol);
                System.out.printf(Locale.ROOT, "%d;%d;%.3f;%.3f;%.2f;%d%n",
                        n, m, lista[2] / 1e6, arbol[2] / 1e6,
                        (double) lista[2] / arbol[2], a.suplente());
            }
        }
    }

    private static void consumir(CalendarioGuardias c) {
        control = c.suplente() + c.asignaciones().size();
    }
}
