package co.edu.uq.cinemauq.core.domain;
import co.edu.uq.cinemauq.core.exceptions.DatosInvalidosException;
import java.util.Objects;

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



    public void validar() {
        if (id == null || id.trim().isEmpty()) {
            throw new DatosInvalidosException("El ID del usuario no puede estar vacío (RF-001)");
        }
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new DatosInvalidosException("El nombre del usuario no puede estar vacío (RF-001)");
        }
        if (correo == null || !correo.contains("@")) {
            throw new DatosInvalidosException("El correo del usuario es inválido (RF-001)");
        }
    }

    public void setId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new DatosInvalidosException("El ID del usuario no puede estar vacío (RF-001)");
        }
        this.id = id;
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new DatosInvalidosException("El nombre del usuario no puede estar vacío (RF-001)");
        }
        this.nombre = nombre;
    }

    public void setCorreo(String correo) {
        if (correo == null || !correo.contains("@")) {
            throw new DatosInvalidosException("El correo del usuario es inválido (RF-001)");
        }
        this.correo = correo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(id, usuario.id) && Objects.equals(correo, usuario.correo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, correo);
    }

}