package co.edu.uq.cinemauq.core.ports;

import co.edu.uq.cinemauq.core.domain.TarjetaVirtual;

/**
 * Interfaz para procesamiento de pagos.
 * Implementa: RF-007 (Procesamiento de Pagos)
 */

public interface IPagoProcessor {

    boolean procesarPago(TarjetaVirtual tarjeta, double monto);

    void procesarReembolso(TarjetaVirtual tarjeta, double monto);
}
