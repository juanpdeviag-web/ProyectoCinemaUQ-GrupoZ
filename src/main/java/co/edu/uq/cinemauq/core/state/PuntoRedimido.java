package co.edu.uq.cinemauq.core.state;

import co.edu.uq.cinemauq.core.domain.PuntoCinemaUQ;
import co.edu.uq.cinemauq.core.exceptions.PuntosException;

/** Estado final de un lote que fue consumido completamente. */
public class PuntoRedimido implements EstadoPunto {
    @Override
    public boolean esValido(PuntoCinemaUQ punto) {
        return false;
    }

    @Override
    public int redimir(PuntoCinemaUQ punto, int cantidadSolicitada) {
        throw new PuntosException("Este lote de puntos ya fue redimido completamente");
    }

    @Override
    public String getNombre() {
        return "REDIMIDO";
    }
}
