package test.services;

import co.edu.uq.cinemauq.core.domain.Cliente;
import co.edu.uq.cinemauq.core.domain.Usuario;
import co.edu.uq.cinemauq.core.repository.UserRepository;
import co.edu.uq.cinemauq.services.CineServiceImpl;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;


public class CineServiceTest {
    @Test
    void servicioDependeDeLaAbstraccionYRegistrarUsuario() {
        List<Usuario> almacen = new ArrayList<>();

        UserRepository repository = new UserRepository() {
            @Override
            public void guardar(Usuario usuario) {
                almacen.add(usuario);
            }

            @Override
            public List<Usuario> listar() {
                return List.copyOf(almacen);
            }
        };

        CineServiceImpl service = new CineServiceImpl(repository);
        Cliente cliente = new Cliente("C001", "Ana", "ana@correo.com");

        service.registrarUsuario(cliente);

        assertEquals(1, service.listClientes().size());
        assertSame(cliente, service.listUsuarios().get(0));
    }
}
