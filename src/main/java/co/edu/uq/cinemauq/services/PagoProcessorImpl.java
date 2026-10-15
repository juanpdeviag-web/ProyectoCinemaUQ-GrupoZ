package co.edu.uq.cinemauq.services;

import co.edu.uq.cinemauq.core.domain.TarjetaVirtual;
import co.edu.uq.cinemauq.core.domain.TipoTransaccion;
import co.edu.uq.cinemauq.core.domain.Transaccion;
import co.edu.uq.cinemauq.core.ports.IPagoProcessor;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class PagoProcessorImpl implements IPagoProcessor {
    private static final int LIMITE_HORAS_REEMBOLSO = 24;

    @Override
    public boolean procesarPago(TarjetaVirtual tarjeta, double monto) {
        if (!validarPago(tarjeta, monto)) {
            return false;
        }
        tarjeta.deducir(monto);
        return true;
    }

    @Override
    public void procesarReembolso(TarjetaVirtual tarjeta, double monto) {
        if (!tarjeta.isActiva()) {
            throw new IllegalStateException("La tarjeta está desactivada");
        }
        if (monto <= 0) {
            throw new IllegalArgumentException("El monto de reembolso debe ser positivo");
        }
        Transaccion ultimaTransaccion = tarjeta.obtenerUltimaTransaccion();
        if (ultimaTransaccion == null) {
            throw new IllegalStateException("No hay transacciones para reembolsar");
        }
        if (ultimaTransaccion.tipo() != TipoTransaccion.COMPRA) {
            throw new IllegalStateException("Solo se pueden reembolsar compras");
        }
        if (ultimaTransaccion.monto() < monto) {
            throw new IllegalArgumentException("El monto de reembolso excede el de la compra");
        }
        long horasDesdeCompra = ChronoUnit.HOURS.between(ultimaTransaccion.fecha(), LocalDateTime.now());
        if (horasDesdeCompra > LIMITE_HORAS_REEMBOLSO) {
            throw new IllegalStateException("El tiempo para reembolso ha expirado");
        }
        double saldoAnterior = tarjeta.getSaldo();
        tarjeta.agregarSaldoSinTransaccion(monto);
        Transaccion reembolso = new Transaccion(
            UUID.randomUUID().toString(),
            TipoTransaccion.REEMBOLSO,
            monto,
            saldoAnterior,
            tarjeta.getSaldo(),
            LocalDateTime.now(),
            TipoTransaccion.REEMBOLSO.getDescripcion()
        );
        tarjeta.registrarTransaccion(reembolso);
    }

    @Override
    public boolean validarPago(TarjetaVirtual tarjeta, double monto) {
        if (tarjeta == null) {
            return false;
        }
        if (!tarjeta.isActiva()) {
            return false;
        }
        if (monto <= 0) {
            return false;
        }
        return tarjeta.validarSaldoSuficiente(monto);
    }
}
