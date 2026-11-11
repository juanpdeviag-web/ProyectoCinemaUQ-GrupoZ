package co.edu.uq.cinemauq.core.state;

import co.edu.uq.cinemauq.core.domain.PuntoCinemaUQ;
import co.edu.uq.cinemauq.core.exceptions.PuntosException;

/** Estado de un lote cuya fecha de vencimiento ya expiró. */
public class PuntoVencido implements EstadoPunto {
    @Override
    public boolean esValido(PuntoCinemaUQ punto) {
        return false;
    }

    @Override
    public int redimir(PuntoCinemaUQ punto, int cantidadSolicitada) {
        throw new PuntosException("No se pueden redimir puntos vencidos");
    }

    @Override
    public String getNombre() {
        return "VENCIDO";
    }
}
