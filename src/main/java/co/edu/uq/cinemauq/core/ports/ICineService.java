package co.edu.uq.cinemauq.core.ports;

import co.edu.uq.cinemauq.core.domain.Cliente;
import co.edu.uq.cinemauq.core.domain.Usuario;

import java.util.List;

/**
 * Interfaz para servicios del cine.
 * Implementa: RF-001 (Registro de Usuarios), RF-008 (Listado de Usuarios)
 */

public interface ICineService {
    void registrarUsuario(Usuario usuario);

    List<Usuario> listUsuarios();

    List<Cliente> listClientes();
}