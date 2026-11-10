package co.edu.uq.cinemauq.core.domain;

import co.edu.uq.cinemauq.core.exceptions.CompraException;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Representa un reembolso realizado a una compra cancelada.
 * Implementa: RF-0XX (Reembolso de Compras)
 * Regla de Negocio: RN-010 (Inmutabilidad de reembolsos)
 */
public record Reembolso(
    String id,
    String idCompra,
    double montoReembolsado,
    double porcentajeAplicado,
    LocalDateTime fechaReembolso,
    String politicaAplicada,
    boolean procesado
) {
    public Reembolso {
        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString();
        }
        if (idCompra == null || idCompra.isBlank()) {
            throw new CompraException("El ID de la compra no puede ser nulo o vacío");
        }
        if (montoReembolsado < 0) {
            throw new CompraException("El monto reembolsado no puede ser negativo");
        }
        if (porcentajeAplicado < 0 || porcentajeAplicado > 1) {
            throw new CompraException("El porcentaje aplicado debe estar entre 0 y 1");
        }
        if (fechaReembolso == null) {
            fechaReembolso = LocalDateTime.now();
        }
        if (politicaAplicada == null || politicaAplicada.isBlank()) {
            throw new CompraException("La política aplicada no puede ser nula o vacía");
        }
    }

    public static Reembolso crear(
        String idCompra,
        double montoOriginal,
        double porcentajeAplicado,
        String politicaAplicada
    ) {
        double montoReembolsado = montoOriginal * porcentajeAplicado;
        return new Reembolso(
            UUID.randomUUID().toString(),
            idCompra,
            montoReembolsado,
            porcentajeAplicado,
            LocalDateTime.now(),
            politicaAplicada,
            true
        );
    }

    public Reembolso marcarComoProcesado() {
        return new Reembolso(
            id,
            idCompra,
            montoReembolsado,
            porcentajeAplicado,
            fechaReembolso,
            politicaAplicada,
            true
        );
    }

    public Reembolso marcarComoNoProcesado() {
        return new Reembolso(
            id,
            idCompra,
            montoReembolsado,
            porcentajeAplicado,
            fechaReembolso,
            politicaAplicada,
            false
        );
    }
}
