package co.edu.uq.cinemauq.core.domain;

import co.edu.uq.cinemauq.core.factory.Compra;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/** Registro de una operación pagada: identidad, titular, tarjeta e importe reales. */
public record CompraConfirmada(Compra compra, Cliente cliente, TarjetaVirtual tarjetaPago,
                               BigDecimal importePagado, List<ReservaAsiento> reservas,
                               SolicitudCancelacion solicitud, Reembolso reembolso, DetallePago detallePago) {
    /** Evidencia inmutable del descuento aplicado al confirmar, no recalculada desde el catálogo. */
    public record DetalleItem(String nombre, int cantidad, BigDecimal precioUnitario, boolean combo) { }
    public record DetallePago(String promociones, int puntos, BigDecimal descuentoPuntos,
                              BigDecimal entradas, BigDecimal confiteria, BigDecimal descuentoPromociones,
                              List<DetalleItem> items) {
        public DetallePago { items=List.copyOf(items); }
        public DetallePago(String promociones,int puntos,BigDecimal descuentoPuntos) {
            this(promociones,puntos,descuentoPuntos,BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO,List.of());
        }
    }
    public CompraConfirmada(Compra compra, Cliente cliente, TarjetaVirtual tarjetaPago,
                            BigDecimal importePagado, List<ReservaAsiento> reservas,
                            SolicitudCancelacion solicitud, Reembolso reembolso) {
        this(compra, cliente, tarjetaPago, importePagado, reservas, solicitud, reembolso,
                new DetallePago("Sin detalle histórico", 0, BigDecimal.ZERO));
    }
    public CompraConfirmada {
        Objects.requireNonNull(compra);
        Objects.requireNonNull(cliente);
        Objects.requireNonNull(tarjetaPago);
        Objects.requireNonNull(importePagado);
        Objects.requireNonNull(detallePago);
        compra.validar();
        reservas = List.copyOf(reservas);
        if (importePagado.signum() < 0 || reservas.size() != compra.asientos().size()
                || reservas.stream().anyMatch(r -> !compra.id().equals(r.getCompraId())
                || !cliente.getId().equals(r.getUsuarioId()) || !compra.funcion().getId().equals(r.getFuncionId()))
                || !new java.util.HashSet<>(compra.asientos()).equals(reservas.stream()
                .map(ReservaAsiento::getAsientoId).collect(java.util.stream.Collectors.toSet()))) {
            throw new IllegalArgumentException("Los datos del pago y sus reservas no corresponden a la misma compra");
        }
        if (reembolso != null && (!compra.id().equals(reembolso.idCompra()) || !compra.estaCancelada())) {
            throw new IllegalArgumentException("El reembolso debe corresponder a la compra cancelada");
        }
    }

    public CompraConfirmada conReembolso(SolicitudCancelacion solicitud, Reembolso reembolso) {
        return new CompraConfirmada(compra.cancelar(), cliente, tarjetaPago, importePagado, reservas, solicitud, reembolso, detallePago);
    }
}
