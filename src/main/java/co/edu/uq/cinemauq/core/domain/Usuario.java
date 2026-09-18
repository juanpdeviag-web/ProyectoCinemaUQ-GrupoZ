package co.edu.uq.cinemauq.core.domain;

public abstract class Usuario {
    protected String id;
    protected String nombre;
    protected String correo;

    public Usuario(String id, String nombre, String correo) {
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
    }

    public abstract String getRol();
}