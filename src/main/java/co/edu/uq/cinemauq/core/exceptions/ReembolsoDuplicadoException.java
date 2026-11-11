package co.edu.uq.cinemauq.core.exceptions;

/**
 * Excepción lanzada cuando se intenta reembolsar una compra que ya fue reembolsada.
 * Regla de Negocio: RN-013 (Prohibición de reembolso duplicado)
 */
public class ReembolsoDuplicadoException extends CinemaUQException {
    public ReembolsoDuplicadoException(String idCompra) {
        super("La compra con ID " + idCompra + " ya ha sido reembolsada anteriormente (RN-013)");
    }
}
