package co.edu.uq.cinemauq.core.factory;

import co.edu.uq.cinemauq.core.domain.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class UsuarioFactory {
    public static Usuario crearUsuario(String tipo, String id, String nombre, String correo) {
        if (tipo.equalsIgnoreCase("CLIENTE")) {
            return new Cliente(id, nombre, correo);
        } else if (tipo.equalsIgnoreCase("ADMINISTRADOR")) {
            return new Administrador(id, nombre, correo);
        }
        throw new IllegalArgumentException("Tipo de usuario no soportado: " + tipo);
    }

    private final Map<String, Function<DatosUsuario, Usuario>> creadores = new HashMap<>();

    public UsuarioFactory registrarTipo(String tipo, Function<DatosUsuario, Usuario> creador) {
        String clave = normalizar(tipo);
        creadores.put(clave, Objects.requireNonNull(creador, "El creador no puede ser null"));
        return this;
    }

    private static String normalizar(String tipo) {
        return Objects.requireNonNull(tipo, "El tipo de usuario no puede ser null")
                .trim().toUpperCase();
    }

    public record DatosUsuario(String id, String nombre, String correo) { }

    public Usuario crearUsuarioRegistrado(
            String tipo,
            String id,
            String nombre,
            String correo) {

        String clave = normalizar(tipo);

        Function<DatosUsuario, Usuario> creador = creadores.get(clave);

        if (creador == null) {
            throw new IllegalArgumentException(
                    "Tipo de usuario no soportado: " + tipo
            );
        }

        return creador.apply(new DatosUsuario(id, nombre, correo));
    }
}