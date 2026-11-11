package co.edu.uq.cinemauq.core.state;

import co.edu.uq.cinemauq.core.domain.PuntoCinemaUQ;
import co.edu.uq.cinemauq.core.exceptions.PuntosException;

import java.time.LocalDate;

/** Estado de un lote vigente y con saldo disponible. */
public class PuntoDisponible implements EstadoPunto {
    @Override
    public boolean esValido(PuntoCinemaUQ punto) {
        return punto.getCantidad() > 0
                && !LocalDate.now().isAfter(punto.getFechaVencimiento());
    }

    @Override
    public int redimir(PuntoCinemaUQ punto, int cantidadSolicitada) {
        if (cantidadSolicitada <= 0) {
            throw new PuntosException("La cantidad de puntos a redimir debe ser mayor que cero");
        }
        if (!esValido(punto)) {
            throw new PuntosException("El lote de puntos ya no está disponible para redención");
        }
        return Math.min(punto.getCantidad(), cantidadSolicitada);
    }

    @Override
    public String getNombre() {
        return "DISPONIBLE";
    }
}
