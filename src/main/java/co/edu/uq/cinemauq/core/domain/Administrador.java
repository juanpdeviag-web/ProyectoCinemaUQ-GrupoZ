package co.edu.uq.cinemauq.core.domain;

public class Administrador extends Usuario {
    public Administrador(String id, String nombre, String correo) {
        super(id, nombre, correo);
    }

    @Override
    public String getRol() { return "ADMINISTRADOR"; }
}