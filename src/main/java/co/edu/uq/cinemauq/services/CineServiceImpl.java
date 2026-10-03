package co.edu.uq.cinemauq.services;

import co.edu.uq.cinemauq.core.domain.Cliente;
import co.edu.uq.cinemauq.core.ports.ICineService;
import co.edu.uq.cinemauq.core.domain.Usuario;
import co.edu.uq.cinemauq.core.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CineServiceImpl implements ICineService {
    // Instancia única del Singleton
    private static CineServiceImpl instancia;
    private UserRepository repository;

    private List<Usuario> usuarios;

    // Constructor privado para evitar instanciación externa
    private CineServiceImpl() {
        this.usuarios = new ArrayList<>();
    }

    // Punto de acceso global al Singleton
    public static synchronized CineServiceImpl getInstancia() {
        if (instancia == null) {
            instancia = new CineServiceImpl();
        }
        return instancia;
    }

    // Público: lo usan los tests y permite inyectar la abstracción
    public CineServiceImpl(UserRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    @Override
    public void registrarUsuario(Usuario usuario) {
        repository.guardar(usuario);
    }

    @Override
    public List<Usuario> listUsuarios() {
        return repository.listar();
    }

    @Override
    public List<Cliente> listClientes() {
        return repository.listar().stream()
                .filter(u -> u instanceof Cliente)
                .map(u -> (Cliente) u)
                .toList();
    }
}