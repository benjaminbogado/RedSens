package redsens.guardias;

import java.util.List;

/** Identificadores de técnicos: el orden empieza en el primer fin de semana. */
public record CalendarioGuardias(List<Integer> asignaciones, int suplente) {
    public CalendarioGuardias {
        asignaciones = List.copyOf(asignaciones);
    }
}
