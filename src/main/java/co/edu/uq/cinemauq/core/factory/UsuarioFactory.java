package co.edu.uq.cinemauq.core.factory;

import co.edu.uq.cinemauq.core.domain.*;

public class UsuarioFactory {
    public static Usuario crearUsuario(String tipo, String id, String nombre, String correo) {
        if (tipo.equalsIgnoreCase("CLIENTE")) {
            return new Cliente(id, nombre, correo);
        } else if (tipo.equalsIgnoreCase("ADMINISTRADOR")) {
            return new Administrador(id, nombre, correo);
        }
        throw new IllegalArgumentException("Tipo de usuario no soportado: " + tipo);
    }
}