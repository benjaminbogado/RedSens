package redsens.registro;

import java.util.Locale;
import java.util.Random;
import redsens.modelo.Estacion;
import redsens.modelo.TipoEstacion;

/** Casos de M1 lineal ejecutables sin dependencias externas. */
public final class PruebaTablaHash {
    private static int verificaciones;

    public static void main(String[] args) {
        probarColisionesYBorrado();
        probarVueltaAlInicioYTablaLlena();
        probarEntradasInvalidas();
        probarOperacionesAleatorias();
        probarDiezMilEstaciones();
        System.out.println("Tabla hash lineal: " + verificaciones + " verificaciones OK");
    }

    private static void probarColisionesYBorrado() {
        TablaHash tabla = new TablaHash(5, EstrategiaExploracion.LINEAL);
        verificar(tabla.estaVacia() && tabla.tamanio() == 0, "Tabla vacía");
        verificar(tabla.buscar(codigo(0)) == null, "Ausente en tabla vacía");
        verificar(tabla.getSondeosUltimaOperacion() == 1, "Se sondea la casilla vacía");
        // Estas tres cadenas tienen hash inicial 0 módulo 5.
        tabla.reiniciarContadores();
        Estacion primera = estacion(0);
        Estacion tercera = estacion(14);
        tabla.insertar(primera);
        tabla.insertar(estacion(5));
        tabla.insertar(tercera);
        verificar(tabla.getSondeosUltimaOperacion() == 3, "Tres sondeos al insertar");
        verificar(tabla.getColisionesUltimaOperacion() == 2, "Dos colisiones al insertar");
        verificar(tabla.getSondeosTotales() == 6 && tabla.getColisionesTotales() == 3,
                "Contadores acumulados de inserción");
        verificar(tabla.factorCarga() == 0.6, "Factor de carga activo");
        tabla.eliminar(codigo(0));
        verificar(tabla.buscar(codigo(14)) == tercera, "La marca no corta la búsqueda");
        verificar(tabla.getSondeosUltimaOperacion() == 3, "La marca también es un sondeo");
        verificar(tabla.getColisionesUltimaOperacion() == 1, "La marca no es colisión");
        esperar(IllegalArgumentException.class, () -> tabla.insertar(estacion(14)));
        verificar(tabla.tamanio() == 2, "No duplica una clave detrás de un borrado");
        tabla.insertar(primera);
        verificar(tabla.buscar(codigo(0)) == primera, "Reutiliza la primera marca");
        verificar(tabla.getSondeosUltimaOperacion() == 1, "Reutilización en posición inicial");
        tabla.reiniciarContadores();
        verificar(tabla.getSondeosTotales() == 0 && tabla.getColisionesTotales() == 0
                && tabla.getSondeosUltimaOperacion() == 0, "Reinicio de mediciones");
        verificar(tabla.tamanio() == 3, "El reinicio conserva los datos");
    }

    private static void probarVueltaAlInicioYTablaLlena() {
        TablaHash tabla = new TablaHash(5);
        // Hash 4: la cadena de exploración cruza del final al comienzo.
        tabla.insertar(estacion(4));
        Estacion segunda = estacion(9);
        tabla.insertar(segunda);
        verificar(tabla.buscar(codigo(9)) == segunda, "Exploración circular");
        tabla.insertar(estacion(18));
        tabla.insertar(estacion(27));
        tabla.insertar(estacion(36));
        esperar(IllegalStateException.class, () -> tabla.insertar(estacion(45)));
        verificar(tabla.tamanio() == 5, "Fallo por capacidad conserva el tamaño");
        verificar(tabla.getSondeosUltimaOperacion() == 5, "El ciclo completo está acotado");
        verificar(tabla.buscar(codigo(45)) == null, "Ausente sin casillas nunca ocupadas");
        verificar(tabla.getSondeosUltimaOperacion() == 5, "Búsqueda acotada en tabla llena");
        esperar(IllegalArgumentException.class, () -> tabla.insertar(estacion(9)));
        tabla.eliminar(codigo(18));
        tabla.insertar(estacion(45));
        verificar(tabla.tamanio() == 5 && tabla.buscar(codigo(45)) != null,
                "Reutiliza un borrado sin casillas nuevas");
        for (int numero : new int[] {4, 9, 27, 36, 45}) {
            tabla.eliminar(codigo(numero));
        }
        verificar(tabla.estaVacia(), "Todas las casillas son marcas de borrado");
        verificar(tabla.buscar(codigo(99)) == null, "Termina con todas las casillas borradas");
        tabla.insertar(estacion(99));
        verificar(tabla.tamanio() == 1, "Inserta con todas las casillas borradas");
        TablaHash unica = new TablaHash(1);
        unica.insertar(estacion(0));
        unica.eliminar(codigo(0));
        unica.insertar(estacion(1));
        verificar(unica.buscar(codigo(1)) != null, "Capacidad mínima de una casilla");
    }

    private static void probarEntradasInvalidas() {
        esperar(IllegalArgumentException.class, () -> new TablaHash(0));
        esperar(IllegalArgumentException.class, () -> new TablaHash(-1));
        esperar(IllegalArgumentException.class, () -> new TablaHash(17, null));
        TablaHash tabla = new TablaHash(17);
        esperar(IllegalArgumentException.class, () -> tabla.insertar(null));
        for (String invalido : new String[] {null, "", "CBA-12", "CBA-10000", "ABC-0000"}) {
            esperar(IllegalArgumentException.class, () -> tabla.buscar(invalido));
            esperar(IllegalArgumentException.class, () -> tabla.eliminar(invalido));
        }
        esperar(IllegalArgumentException.class, () -> tabla.eliminar(codigo(0)));
        verificar(tabla.estaVacia(), "Entradas inválidas no alteran la tabla");
    }

    private static void probarOperacionesAleatorias() {
        TablaHash tabla = new TablaHash(211);
        Estacion[] referencia = new Estacion[150];
        Random random = new Random(3026L);
        int cantidad = 0;
        for (int i = 0; i < 3000; i++) {
            int numero = random.nextInt(referencia.length);
            int operacion = random.nextInt(3);
            if (operacion == 0) {
                if (referencia[numero] == null) {
                    referencia[numero] = estacion(numero);
                    tabla.insertar(referencia[numero]);
                    cantidad++;
                } else {
                    esperar(IllegalArgumentException.class, () -> tabla.insertar(estacion(numero)));
                }
            } else if (operacion == 1) {
                if (referencia[numero] == null) {
                    esperar(IllegalArgumentException.class, () -> tabla.eliminar(codigo(numero)));
                } else {
                    tabla.eliminar(codigo(numero));
                    referencia[numero] = null;
                    cantidad--;
                }
            } else {
                verificar(tabla.buscar(codigo(numero)) == referencia[numero], "Búsqueda contra referencia");
            }
            verificar(tabla.tamanio() == cantidad, "Cantidad tras operación aleatoria");
        }
        for (int numero = 0; numero < referencia.length; numero++) {
            verificar(tabla.buscar(codigo(numero)) == referencia[numero], "Estado final contra referencia");
        }
    }

    private static void probarDiezMilEstaciones() {
        // Capacidad fija suficiente: esto es una prueba de integridad, no el experimento M1.
        TablaHash tabla = new TablaHash(20011);
        int[] orden = new int[10000];
        Estacion[] referencia = new Estacion[10000];
        for (int i = 0; i < orden.length; i++) {
            orden[i] = i;
            referencia[i] = estacion(i);
        }
        Random random = new Random(3026L);
        for (int i = orden.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int aux = orden[i];
            orden[i] = orden[j];
            orden[j] = aux;
        }
        for (int numero : orden) {
            tabla.insertar(referencia[numero]);
        }
        verificar(tabla.tamanio() == 10000, "Diez mil códigos únicos");
        for (int numero = 0; numero < referencia.length; numero++) {
            verificar(tabla.buscar(codigo(numero)) == referencia[numero], "Integridad de diez mil estaciones");
        }
    }

    private static String codigo(int numero) {
        return String.format(Locale.ROOT, "CBA-%04d", numero);
    }

    private static Estacion estacion(int numero) {
        return new Estacion(codigo(numero), numero, "Estación " + numero, "San Francisco",
                TipoEstacion.METEOROLOGICA, numero % 101, 0);
    }

    private static void verificar(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new AssertionError(mensaje);
        }
        verificaciones++;
    }

    private static void esperar(Class<? extends RuntimeException> tipo, Runnable accion) {
        try {
            accion.run();
        } catch (RuntimeException e) {
            verificar(tipo.isInstance(e), "Tipo de excepción: " + e);
            return;
        }
        throw new AssertionError("Se esperaba " + tipo.getSimpleName());
    }
}
