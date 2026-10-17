package co.edu.uq.cinemauq.core.factory;

import co.edu.uq.cinemauq.core.domain.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import co.edu.uq.cinemauq.core.exceptions.DatosInvalidosException;
import co.edu.uq.cinemauq.core.exceptions.UsuarioDuplicadoException;

/**
 * Factory Method para la creación de usuarios.
 * Implementa: RF-001 (Registro de Usuarios)
 * Patrón Creacional: Factory Method
 * Regla de Negocio: RN-005 (Normalización de tipos de usuario)
 */

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



    public static void validarDatosCreacion(String tipo, String id, String nombre, String correo) {
        if (tipo == null || tipo.trim().isEmpty()) {
            throw new DatosInvalidosException("El tipo de usuario no puede estar vacío (RF-001)");
        }
        if (!tipo.equalsIgnoreCase("CLIENTE") && !tipo.equalsIgnoreCase("ADMINISTRADOR")) {
            throw new DatosInvalidosException("Tipo de usuario no válido: " + tipo);
        }
        if (id == null || id.trim().isEmpty()) {
            throw new DatosInvalidosException("El ID del usuario no puede ser nulo o vacío (RF-001)");
        }
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new DatosInvalidosException("El nombre del usuario no puede estar vacío (RF-001)");
        }
        if (correo == null || !correo.contains("@")) {
            throw new DatosInvalidosException("El correo del usuario es inválido (RF-001)");
        }
    }

    public static Usuario crearUsuarioValidado(String tipo, String id, String nombre, String correo, boolean yaExiste) {
        validarDatosCreacion(tipo, id, nombre, correo);
        if (yaExiste) {
            throw new UsuarioDuplicadoException(id);
        }
        Usuario usuario = crearUsuario(tipo, id, nombre, correo);
        usuario.validar();
        return usuario;
    }

}