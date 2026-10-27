package redsens.simulacion;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import redsens.modelo.Estacion;
import redsens.modelo.TipoEstacion;

public class GeneradorDatos {

    public static final long SEMILLA_GRUPO = 3026L;
    public static final int MAX_ESTACIONES = 10000;

    private static final String[] LOCALIDADES = {
        "Córdoba",
        "Villa Carlos Paz",
        "Alta Gracia",
        "Jesús María",
        "Río Tercero",
        "Villa María",
        "San Francisco",
        "Cosquín",
        "La Falda",
        "Río Cuarto",
        "Deán Funes",
        "Villa Dolores"
    };

    /**
     * Genera estaciones usando la semilla del grupo 3.
     */
    public static List<Estacion> generarEstaciones(int cantidad) {
        return generarEstaciones(cantidad, SEMILLA_GRUPO);
    }

    /**
     * La misma cantidad y semilla producen los mismos datos
     * en el mismo orden.
     *
     * Los códigos son únicos dentro de cada lista generada.
     */
    public static List<Estacion> generarEstaciones(
            int cantidad, long semilla) {

        if (cantidad < 0 || cantidad > MAX_ESTACIONES) {
            throw new IllegalArgumentException(
                "La cantidad debe estar entre 0 y 10000"
            );
        }

        Random random = new Random(semilla);
        List<Estacion> estaciones = new ArrayList<>(cantidad);

        // Cada número aparece una sola vez en este arreglo.
        int[] numeros = new int[MAX_ESTACIONES];

        for (int i = 0; i < numeros.length; i++) {
            numeros[i] = i;
        }

        TipoEstacion[] tipos = TipoEstacion.values();

        for (int i = 0; i < cantidad; i++) {

            // Elegimos un número entre los que todavía no usamos.
            // Intercambiamos posiciones: Fisher-Yates parcial.
            int posicion = i + random.nextInt(MAX_ESTACIONES - i);

            int auxiliar = numeros[i];
            numeros[i] = numeros[posicion];
            numeros[posicion] = auxiliar;

            int numero = numeros[i];

            String codigo = String.format(
                Locale.ROOT, "CBA-%04d", numero
            );

            String nombre = "Estación " + codigo;

            String localidad = LOCALIDADES[
                random.nextInt(LOCALIDADES.length)
            ];

            TipoEstacion tipo = tipos[
                random.nextInt(tipos.length)
            ];

            // nextInt(101) genera valores entre 0 y 100 inclusive.
            int bateria = random.nextInt(101);

            // Decisión de simulación: entre 0 y 365 días inclusive.
            int diasSinVisita = random.nextInt(366);

            Estacion estacion = new Estacion(
                codigo,
                numero,
                nombre,
                localidad,
                tipo,
                bateria,
                diasSinVisita
            );

            estaciones.add(estacion);
        }

        return estaciones;
    }
}