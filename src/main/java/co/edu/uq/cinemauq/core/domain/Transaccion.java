package co.edu.uq.cinemauq.core.domain;

import java.time.LocalDateTime;

public record Transaccion(
    String id,
    TipoTransaccion tipo,
    double monto,
    double saldoAnterior,
    double saldoPosterior,
    LocalDateTime fecha,
    String descripcion
) {
    public Transaccion {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("ID de transacción no puede ser nulo o vacío");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("Tipo de transacción no puede ser nulo");
        }
        if (monto <= 0) {
            throw new IllegalArgumentException("Monto debe ser positivo");
        }
        if (saldoAnterior < 0) {
            throw new IllegalArgumentException("Saldo anterior no puede ser negativo");
        }
        if (saldoPosterior < 0) {
            throw new IllegalArgumentException("Saldo posterior no puede ser negativo");
        }
        if (fecha == null) {
            throw new IllegalArgumentException("Fecha no puede ser nula");
        }
        if (descripcion == null || descripcion.isEmpty()) {
            throw new IllegalArgumentException("Descripción no puede ser nula o vacía");
        }
    }
}
