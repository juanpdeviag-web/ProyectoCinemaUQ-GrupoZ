package co.edu.uq.cinemauq.core.exceptions;

public class UsuarioDuplicadoException extends UsuarioException {
    public UsuarioDuplicadoException(String idUsuario) {
        super("Ya existe un usuario registrado con el ID: " + idUsuario);
    }
}
