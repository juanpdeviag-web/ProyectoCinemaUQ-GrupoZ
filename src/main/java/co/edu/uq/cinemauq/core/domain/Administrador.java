package co.edu.uq.cinemauq.core.domain;
import co.edu.uq.cinemauq.core.exceptions.UsuarioException;

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


    private boolean permisosActivos = true;

    public void validarPermisos() {
        super.validar();
        if (!permisosActivos || !"ADMINISTRADOR".equals(getRol())) {
            throw new UsuarioException("El usuario no cuenta con permisos administrativos activos");
        }
    }

    public boolean tieneAutorizacion(String operacion) {
        if (operacion == null || operacion.trim().isEmpty()) {
            return false;
        }
        validarPermisos();
        return true;
    }

    public boolean isPermisosActivos() {
        return permisosActivos;
    }

    public void setPermisosActivos(boolean permisosActivos) {
        this.permisosActivos = permisosActivos;
    }

}