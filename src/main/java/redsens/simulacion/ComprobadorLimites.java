package redsens.simulacion;

public class ComprobadorLimites {

    public static void main(String[] args) {

        comprobarCantidad(0);
        comprobarCantidad(1);
        comprobarCantidad(10000);

        comprobarRechazo(-1);
        comprobarRechazo(10001);

        System.out.println("\nTodas las pruebas de límites pasaron.");
    }

    private static void comprobarCantidad(int cantidad) {

        int generadas = GeneradorDatos
            .generarEstaciones(cantidad)
            .size();

        if (generadas != cantidad) {
            throw new IllegalStateException(
                "Cantidad incorrecta para " + cantidad
            );
        }

        System.out.println(
            "Generar " + cantidad + " estaciones: OK"
        );
    }

    private static void comprobarRechazo(int cantidad) {

        try {
            GeneradorDatos.generarEstaciones(cantidad);
        } catch (IllegalArgumentException e) {
            System.out.println(
                "Rechazar cantidad " + cantidad + ": OK"
            );
            return;
        }

        throw new IllegalStateException(
            "Se aceptó una cantidad inválida: " + cantidad
        );
    }
}