package test.core.factory;

import co.edu.uq.cinemauq.core.domain.*;
import co.edu.uq.cinemauq.core.factory.Compra;
import co.edu.uq.cinemauq.core.factory.CompraBuilder;
import co.edu.uq.cinemauq.core.factory.UsuarioFactory;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class PatronesCreacionalesTest {
    private UsuarioFactory factory() {
        return new UsuarioFactory()
                .registrarTipo("CLIENTE", d -> new Cliente(d.id(), d.nombre(), d.correo()))
                .registrarTipo("ADMINISTRADOR", d -> new Administrador(d.id(), d.nombre(), d.correo()));
    }

    @Test
    void usuarioFactoryCrearCliente() {
        Usuario usuario = UsuarioFactory.crearUsuario(
                "CLIENTE",
                "C001",
                "Ana",
                "ana@correo.com"
        );

        assertInstanceOf(Cliente.class, usuario);
        assertEquals("CLIENTE", usuario.getRol());
    }

    @Test
    void usuarioFactoryCrearAdministrador() {
        Usuario usuario = UsuarioFactory.crearUsuario(
                "ADMINISTRADOR",
                "A001",
                "Carlos",
                "carlos@correo.com"
        );

        assertInstanceOf(Administrador.class, usuario);
        assertEquals("ADMINISTRADOR", usuario.getRol());
    }

    @Test
    void usuarioFactorySinModificarLaFactory() {

        class Invitado extends Usuario {

            Invitado(String id, String nombre, String correo) {
                super(id, nombre, correo);
            }

            @Override
            public String getRol() {
                return "INVITADO";
            }
        }

        UsuarioFactory factory = new UsuarioFactory();

        Usuario usuario = factory
                .registrarTipo(
                        "INVITADO",
                        d -> new Invitado(
                                d.id(),
                                d.nombre(),
                                d.correo()
                        )
                )
                .crearUsuarioRegistrado(
                        "INVITADO",
                        "I001",
                        "Luis",
                        "luis@correo.com"
                );

        assertInstanceOf(Invitado.class, usuario);
        assertEquals("INVITADO", usuario.getRol());
    }

    @Test
    void usuarioFactoryRechazarTipoNoSoportado() {
        assertThrows(IllegalArgumentException.class, () ->
                factory().crearUsuario("DESCONOCIDO", "X001", "Luis", "luis@correo.com"));
    }

    @Test
    void compraBuilderConstruirCompraValida() {
        Funcion funcion = new Funcion(
                new Pelicula("Interestelar", "Ciencia ficción", 169),
                new Sala("Sala 1", 10, 12), LocalDateTime.now());

        Compra compra = new CompraBuilder()
                .paraFuncion(funcion)
                .agregarAsiento("A1")
                .agregarSnack("Palomitas")
                .build();

        assertSame(funcion, compra.funcion());
        assertEquals(List.of("A1"), compra.asientos());
        assertEquals(List.of("Palomitas"), compra.snacks());
    }

    @Test
    void compraBuilderDebeRechazarCompraSinFuncion() {
        assertThrows(IllegalStateException.class, () -> new CompraBuilder()
                .agregarAsiento("A1").build());
    }

    @Test
    void compraBuilderDebeRechazarCompraSinAsientos() {
        Funcion funcion = new Funcion(
                new Pelicula("Interestelar", "Ciencia ficción", 169),
                new Sala("Sala 1", 10, 12), LocalDateTime.now());

        assertThrows(IllegalStateException.class, () -> new CompraBuilder().paraFuncion(funcion).build());
    }
}
