package co.edu.uq.cinemauq.core.domain;

/**
 * Entidad base para los usuarios del sistema.
 * Implementa: RF-001 (Registro de Usuarios)
 */

public abstract class Usuario {
    protected String id;
    protected String nombre;
    protected String correo;

    public Usuario(String id, String nombre, String correo) {
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
    }

    public String getId() { return id; }

    public String getNombre() { return nombre; }

    public String getCorreo() { return correo; }

    public abstract String getRol();
}