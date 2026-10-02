package co.edu.uq.cinemauq.core.factory;

import co.edu.uq.cinemauq.core.domain.Funcion;

import java.util.List;

public record Compra(Funcion funcion, List<String> asientos, List<String> snacks) {
    public Compra {
        asientos = List.copyOf(asientos);
        snacks = List.copyOf(snacks);
    }
}
