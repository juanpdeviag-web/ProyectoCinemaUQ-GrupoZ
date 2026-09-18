package co.edu.uq.cinemauq.services;

import co.edu.uq.cinemauq.core.ports.ICineService;
import co.edu.uq.cinemauq.core.domain.Usuario;
import java.util.ArrayList;
import java.util.List;

public class CineServiceImpl implements ICineService {
    // Instancia única del Singleton
    private static CineServiceImpl instancia;

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

    @Override
    public void registrarUsuario(Usuario usuario) {
        this.usuarios.add(usuario);
    }
}