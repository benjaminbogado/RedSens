package redsens.modelo;

/** Datos comunes e inmutables de una estación. */
public final class Estacion {
    private final String codigo;
    private final int numero;
    private final String nombre;
    private final String localidad;
    private final TipoEstacion tipo;
    private final int bateria;
    private final int diasSinVisita;

    public Estacion(String codigo, int numero, String nombre, String localidad,
                    TipoEstacion tipo, int bateria, int diasSinVisita) {
        if (codigo == null || !codigo.matches("CBA-[0-9]{4}")) {
            throw new IllegalArgumentException("El código debe tener el formato CBA-NNNN");
        }
        int numeroDelCodigo = Integer.parseInt(codigo.substring(4));
        if (numero != numeroDelCodigo) {
            throw new IllegalArgumentException("El número debe coincidir con los cuatro dígitos del código");
        }
        if (nombre == null || nombre.isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede ser nulo ni vacío");
        }
        if (localidad == null || localidad.isEmpty()) {
            throw new IllegalArgumentException("La localidad no puede ser nula ni vacía");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de estación no puede ser nulo");
        }
        if (bateria < 0 || bateria > 100) {
            throw new IllegalArgumentException("La batería debe estar entre 0 y 100");
        }
        if (diasSinVisita < 0) {
            throw new IllegalArgumentException("Los días sin visita no pueden ser negativos");
        }

        this.codigo = codigo;
        this.numero = numero;
        this.nombre = nombre;
        this.localidad = localidad;
        this.tipo = tipo;
        this.bateria = bateria;
        this.diasSinVisita = diasSinVisita;
    }

    public String getCodigo() {
        return codigo;
    }

    public int getNumero() {
        return numero;
    }

    public String getNombre() {
        return nombre;
    }

    public String getLocalidad() {
        return localidad;
    }

    public TipoEstacion getTipo() {
        return tipo;
    }

    public int getBateria() {
        return bateria;
    }

    public int getDiasSinVisita() {
        return diasSinVisita;
    }

    /** Una estación es crítica cuando su batería está por debajo del 20 %. */
    public boolean esCritica() {
        return bateria < 20;
    }
}
