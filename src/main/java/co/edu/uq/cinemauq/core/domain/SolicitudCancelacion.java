package co.edu.uq.cinemauq.core.domain;

import co.edu.uq.cinemauq.core.exceptions.CompraException;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Representa una solicitud de cancelación de una compra.
 * Implementa: RF-0XX (Solicitud de Cancelación)
 * Regla de Negocio: RN-009 (Inmutabilidad de solicitudes de cancelación)
 */
public record SolicitudCancelacion(
    String id,
    String idCompra,
    LocalDateTime fechaSolicitud,
    String motivo,
    boolean canceladaPorCine
) {
    public SolicitudCancelacion {
        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString();
        }
        if (idCompra == null || idCompra.isBlank()) {
            throw new CompraException("El ID de la compra no puede ser nulo o vacío");
        }
        if (fechaSolicitud == null) {
            fechaSolicitud = LocalDateTime.now();
        }
        if (motivo == null || motivo.isBlank()) {
            throw new CompraException("El motivo de la cancelación no puede ser nulo o vacío");
        }
    }

    public static SolicitudCancelacion crear(String idCompra, String motivo, boolean canceladaPorCine) {
        return new SolicitudCancelacion(
            UUID.randomUUID().toString(),
            idCompra,
            LocalDateTime.now(),
            motivo,
            canceladaPorCine
        );
    }

    public static SolicitudCancelacion crearPorCliente(String idCompra, String motivo) {
        return crear(idCompra, motivo, false);
    }

    public static SolicitudCancelacion crearPorCine(String idCompra, String motivo) {
        return crear(idCompra, motivo, true);
    }
}
