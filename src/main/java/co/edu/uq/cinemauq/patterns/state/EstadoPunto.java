package co.edu.uq.cinemauq.patterns.state;

import co.edu.uq.cinemauq.core.domain.PuntoCinemaUQ;

public interface EstadoPunto {
    default EstadoPunto actualizarEstado(PuntoCinemaUQ lote) {
        return this;
    }

    boolean estaDisponible();

    EstadoPunto redimir(PuntoCinemaUQ lote, int cantidad);
}
