package co.edu.uq.cinemauq.patterns.state;

import co.edu.uq.cinemauq.core.domain.PuntoCinemaUQ;
import co.edu.uq.cinemauq.core.exceptions.UsuarioException;

public final class PuntoVencido implements EstadoPunto {
    @Override
    public boolean estaDisponible() {
        return false;
    }

    @Override
    public EstadoPunto redimir(PuntoCinemaUQ lote, int cantidad) {
        throw new UsuarioException("No se pueden redimir puntos vencidos");
    }
}
