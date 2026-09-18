package co.edu.uq.cinemauq.core.ports;

import co.edu.uq.cinemauq.core.domain.TarjetaVirtual;

public interface IPagoProcessor {

    boolean procesarPago(TarjetaVirtual tarjeta, double monto);

    void procesarReembolso(TarjetaVirtual tarjeta, double monto);
}
