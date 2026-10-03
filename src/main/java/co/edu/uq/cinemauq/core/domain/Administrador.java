package co.edu.uq.cinemauq.core.domain;

/**
 * Representa un administrador del sistema.
 * Implementa: RF-001 (Registro de Usuarios), RF-008 (Listado de Usuarios)
 */

public class Administrador extends Usuario {
    public Administrador(String id, String nombre, String correo) {
        super(id, nombre, correo);
    }

    @Override
    public String getRol() { return "ADMINISTRADOR"; }
}