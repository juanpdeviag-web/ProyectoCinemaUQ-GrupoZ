package co.edu.uq.cinemauq.core.exceptions;

public class AsientoNoDisponibleException extends CompraException {
    public AsientoNoDisponibleException(String asiento) {
        super("El asiento " + asiento + " ya se encuentra ocupado");
    }
}
