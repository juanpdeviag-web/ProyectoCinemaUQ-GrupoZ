package co.edu.uq.cinemauq.core.factory;

import co.edu.uq.cinemauq.core.domain.Funcion;
import java.util.ArrayList;
import java.util.List;
import co.edu.uq.cinemauq.core.exceptions.AsientoNoDisponibleException;
import co.edu.uq.cinemauq.core.exceptions.CompraException;

/**
 * Builder para la construcción de objetos Compra.
 * Implementa: RF-005 (Compra de Boletos)
 * Patrón Creacional: Builder
 * Regla de Negocio: RN-003 (Inmutabilidad de compras)
 */

public class CompraBuilder {
    private Funcion funcion;
    private List<String> asientos = new ArrayList<>();
    private List<String> snacks = new ArrayList<>();

    public CompraBuilder paraFuncion(Funcion funcion) {
        this.funcion = funcion;
        return this;
    }

    public CompraBuilder agregarAsiento(String asiento) {
        this.asientos.add(asiento);
        return this;
    }

    public CompraBuilder agregarSnack(String snack) {
        this.snacks.add(snack);
        return this;
    }

     public Compra build() {
        if (funcion == null || asientos.isEmpty()) {
            throw new IllegalStateException("Una compra requiere al menos una función y un asiento.");
        }
        else  {
            return new Compra(funcion, asientos, snacks);
        }
     }


    public CompraBuilder agregarAsientoValidado(String asiento) {
        if (asiento == null || asiento.trim().isEmpty()) {
            throw new CompraException("El código del asiento no puede estar vacío (RF-005)");
        }
        if (this.asientos.contains(asiento)) {
            throw new AsientoNoDisponibleException(asiento);
        }
        this.asientos.add(asiento);
        return this;
    }

    public void validarEstado() {
        if (funcion == null) {
            throw new CompraException("La compra debe tener una función asignada (RF-005).");
        }
        if (asientos == null || asientos.isEmpty()) {
            throw new CompraException("La compra requiere al menos un asiento seleccionado (RF-005).");
        }
        if (snacks == null) {
            throw new CompraException("La lista de snacks no puede ser nula.");
        }
    }

    public Compra buildValidado() {
        validarEstado();
        Compra compra = new Compra(funcion, asientos, snacks);
        compra.validar();
        return compra;
    }

}