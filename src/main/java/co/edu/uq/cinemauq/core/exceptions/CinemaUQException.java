package co.edu.uq.cinemauq.core.exceptions;

/**
 * Las excepciones de dominio y de negocio heredan de esta clase
 */
public class CinemaUQException extends RuntimeException {
    public CinemaUQException(String message) {
        super(message);
    }

    public CinemaUQException(String message, Throwable cause) {
        super(message, cause);
    }
}

