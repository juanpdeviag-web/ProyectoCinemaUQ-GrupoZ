package co.edu.uq.cinemauq.core.factory;

import co.edu.uq.cinemauq.core.domain.Funcion;

import java.util.List;

/**
 * Representa una compra de boletos.
 * Implementa: RF-005 (Compra de Boletos)
 * Regla de Negocio: RN-003 (Inmutabilidad de compras - record inmutable)
 */

public record Compra(Funcion funcion, List<String> asientos, List<String> snacks) {
    public Compra {
        asientos = List.copyOf(asientos);
        snacks = List.copyOf(snacks);
    }
}
