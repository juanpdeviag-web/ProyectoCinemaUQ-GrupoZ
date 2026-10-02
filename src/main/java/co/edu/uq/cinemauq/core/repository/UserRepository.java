package co.edu.uq.cinemauq.core.repository;

import co.edu.uq.cinemauq.core.domain.Usuario;

import java.util.List;

public interface UserRepository {
    void guardar(Usuario usuario);
    List<Usuario> listar();
}
