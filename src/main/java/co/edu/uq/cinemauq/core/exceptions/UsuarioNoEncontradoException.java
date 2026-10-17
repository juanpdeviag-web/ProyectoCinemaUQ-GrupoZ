package co.edu.uq.cinemauq.core.exceptions;

public class UsuarioNoEncontradoException extends UsuarioException {
    public UsuarioNoEncontradoException(String criterio) {
        super("No se encontró el usuario: " + criterio);
    }
}
