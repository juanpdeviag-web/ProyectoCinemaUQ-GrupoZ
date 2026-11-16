package co.edu.uq.cinemauq.patterns.state;

import co.edu.uq.cinemauq.core.domain.PuntoCinemaUQ;
import co.edu.uq.cinemauq.core.exceptions.UsuarioException;
import java.time.LocalDate;

public final class PuntoDisponible implements EstadoPunto {
    @Override
    public EstadoPunto actualizarEstado(PuntoCinemaUQ lote) {
        return LocalDate.now().isAfter(lote.getFechaVencimiento())
                ? new PuntoVencido() : this;
    }

    @Override
    public boolean estaDisponible() {
        return true;
    }

    @Override
    public EstadoPunto redimir(PuntoCinemaUQ lote, int cantidad) {
        EstadoPunto vigente = actualizarEstado(lote);
        if (vigente != this) {
            return vigente.redimir(lote, cantidad);
        }
        if (cantidad <= 0 || cantidad > lote.getCantidadRestante()) {
            throw new UsuarioException("La cantidad a redimir debe ser positiva y no superar el saldo del lote");
        }
        return cantidad == lote.getCantidadRestante() ? new PuntoRedimido() : this;
    }
}
