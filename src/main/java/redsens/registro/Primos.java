package redsens.registro;

/** Utilidades de números primos para dimensionar la tabla hash. */
public final class Primos {

    private Primos() {
    }

    /** Indica si {@code n} es primo (prueba por divisores impares hasta √n). */
    public static boolean esPrimo(int n) {
        if (n < 2) {
            return false;
        }
        if (n % 2 == 0) {
            return n == 2;
        }
        for (int d = 3; (long) d * d <= n; d += 2) {
            if (n % d == 0) {
                return false;
            }
        }
        return true;
    }

    /** Menor primo mayor o igual que {@code n}. */
    public static int siguientePrimo(int n) {
        int candidato = Math.max(2, n);
        while (!esPrimo(candidato)) {
            candidato++;
        }
        return candidato;
    }
}
