package co.edu.uq.cinemauq.core.repository;

import co.edu.uq.cinemauq.core.domain.Usuario;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MemoryUserRepository implements UserRepository{
    private final List<Usuario> usuarios = new ArrayList<>();

    @Override
    public void guardar(Usuario usuario) {
        usuarios.add(Objects.requireNonNull(usuario, "El usuario no puede ser null"));
    }

    @Override
    public List<Usuario> listar() {
        return List.copyOf(usuarios);
    }
}
