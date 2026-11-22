package redsens.guardias;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Oráculo independiente pequeño, invariantes del ABB y casos del enunciado. */
public final class PruebaJosephus {
    private static int verificaciones;
    public static void main(String[] args) {
        CalendarioGuardias oficial = new CalendarioGuardias(List.of(2, 4, 1, 5), 3);
        comprobar(JosephusLista.resolver(5, 1).equals(oficial));
        comprobar(JosephusArbol.resolver(5, 1).equals(oficial));
        comprobar(JosephusLista.resolver(1, 3).equals(new CalendarioGuardias(List.of(), 1)));
        comprobar(JosephusArbol.resolver(1, 3).equals(new CalendarioGuardias(List.of(), 1)));
        for (int n = 1; n <= 80; n++) {
            for (int m : new int[] {1, 2, 3, 7, 1000}) {
                CalendarioGuardias esperado = oraculo(n, m);
                comprobar(JosephusLista.resolver(n, m).equals(esperado));
                comprobar(JosephusArbol.resolver(n, m).equals(esperado));
            }
        }
        comprobar(JosephusArbol.resolver(17, Integer.MAX_VALUE).equals(oraculo(17, Integer.MAX_VALUE)));
        CalendarioGuardias grupo = JosephusArbol.resolver(12, 3);
        comprobar(grupo.equals(JosephusLista.resolver(12, 3)));
        comprobar(grupo.equals(new CalendarioGuardias(List.of(4,8,12,5,10,3,11,7,6,9,2),1)));
        ArbolOrdenEstadistico arbol = new ArbolOrdenEstadistico(257);
        List<Integer> referencia = new ArrayList<>();
        for (int i = 1; i <= 257; i++) { referencia.add(i); }
        Random random = new Random(3026L);
        while (!referencia.isEmpty()) {
            comprobar(arbol.estructuraValida());
            comprobar(arbol.tamanio() == referencia.size());
            for (int i = 0; i < referencia.size(); i++) {
                comprobar(arbol.seleccionar(i) == referencia.get(i));
            }
            int k = random.nextInt(referencia.size());
            comprobar(arbol.eliminarK(k) == referencia.remove(k));
        }
        comprobar(arbol.tamanio() == 0 && arbol.estructuraValida());
        rechaza(() -> arbol.seleccionar(0));
        rechaza(() -> arbol.eliminarK(-1));
        rechaza(() -> new ArbolOrdenEstadistico(0));
        for (int[] par : new int[][] {{0,1},{-1,3},{5,0},{5,-1}}) {
            rechaza(() -> JosephusLista.resolver(par[0],par[1]));
            rechaza(() -> JosephusArbol.resolver(par[0],par[1]));
        }
        List<Integer> mutable = new ArrayList<>(List.of(2,4,1,5));
        CalendarioGuardias copia = new CalendarioGuardias(mutable,3);
        mutable.clear(); comprobar(copia.equals(oficial));
        try { copia.asignaciones().add(99); throw new AssertionError("Lista mutable"); }
        catch (UnsupportedOperationException esperado) { verificaciones++; }
        System.out.println("Josephus: " + verificaciones + " verificaciones OK");
        System.out.println("Grupo 3: " + grupo);
    }

    private static CalendarioGuardias oraculo(int n, int m) {
        List<Integer> activos = new ArrayList<>();
        for (int i = 1; i <= n; i++) { activos.add(i); }
        List<Integer> orden = new ArrayList<>();
        long posicion = 0;
        while (activos.size() > 1) {
            posicion = (posicion + m) % activos.size();
            orden.add(activos.remove((int) posicion));
        }
        return new CalendarioGuardias(orden, activos.get(0));
    }

    private static void comprobar(boolean condicion) {
        if (!condicion) { throw new AssertionError("Verificación " + verificaciones); }
        verificaciones++;
    }
    private static void rechaza(Runnable accion) {
        try { accion.run(); throw new AssertionError("Se esperaba IllegalArgumentException"); }
        catch (IllegalArgumentException esperado) { verificaciones++; }
    }
}
