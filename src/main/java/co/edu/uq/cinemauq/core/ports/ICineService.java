package co.edu.uq.cinemauq.core.ports;

import co.edu.uq.cinemauq.core.domain.Cliente;
import co.edu.uq.cinemauq.core.domain.Usuario;

import java.util.List;

public interface ICineService {
    void registrarUsuario(Usuario usuario);

    List<Usuario> listUsuarios();

    List<Cliente> listClientes();
}