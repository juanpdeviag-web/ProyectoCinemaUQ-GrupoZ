package co.edu.uq.cinemauq.core.exceptions;

/** Se lanza cuando el cliente intenta redimir más puntos vigentes de los disponibles. */
public class PuntosInsuficientesException extends PuntosException {
    public PuntosInsuficientesException(int disponibles, int requeridos) {
        super("Puntos insuficientes. Disponibles: " + disponibles + ", requeridos: " + requeridos);
    }
}
