package co.edu.uq.cinemauq.core.factory;
import co.edu.uq.cinemauq.core.domain.Funcion;
import java.util.List;
import co.edu.uq.cinemauq.core.exceptions.CompraException;

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

}
