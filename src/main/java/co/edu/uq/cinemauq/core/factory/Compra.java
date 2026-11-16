package co.edu.uq.cinemauq.core.factory;
import co.edu.uq.cinemauq.core.domain.Funcion;
import co.edu.uq.cinemauq.core.domain.EstadoCompra;
import java.time.LocalDateTime;
import java.util.List;
import co.edu.uq.cinemauq.core.exceptions.CompraException;

/**
 * Representa una compra de boletos.
 * Implementa: RF-005 (Compra de Boletos), RF-0XX (Cancelación de Compras)
 * Regla de Negocio: RN-003 (Inmutabilidad de compras - record inmutable)
 * Regla de Negocio: RN-012 (Estado de compra para control de cancelaciones)
 */

public record Compra(
    String id,
    Funcion funcion,
    List<String> asientos,
    List<String> snacks,
    EstadoCompra estado,
    LocalDateTime fechaCompra
) {
    public Compra {
        if (id == null || id.isBlank()) {
            id = java.util.UUID.randomUUID().toString();
        }
        if (estado == null) {
            estado = EstadoCompra.ACTIVA;
        }
        if (fechaCompra == null) {
            fechaCompra = LocalDateTime.now();
        }
        asientos = List.copyOf(asientos);
        snacks = List.copyOf(snacks);
    }

    public static Compra crear(Funcion funcion, List<String> asientos, List<String> snacks) {
        return new Compra(
            java.util.UUID.randomUUID().toString(),
            funcion,
            asientos,
            snacks,
            EstadoCompra.ACTIVA,
            LocalDateTime.now()
        );
    }

    public Compra cancelar() {
        return new Compra(
            id,
            funcion,
            asientos,
            snacks,
            EstadoCompra.CANCELADA,
            fechaCompra
        );
    }

    public void validar() {
        if (funcion == null) {
            throw new CompraException("La compra debe estar asociada a una función válida (RF-005)");
        }
        if (asientos == null || asientos.isEmpty()) {
            throw new CompraException("La compra debe tener al menos un asiento seleccionado (RF-005)");
        }
        if (snacks == null) {
            throw new CompraException("La lista de snacks no puede ser nula");
        }
    }

    public boolean estaCancelada() {
        return estado == EstadoCompra.CANCELADA;
    }

    public boolean esActiva() {
        return estado == EstadoCompra.ACTIVA;
    }
}
