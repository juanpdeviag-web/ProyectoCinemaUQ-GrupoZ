package co.edu.uq.cinemauq.core.seed;

import co.edu.uq.cinemauq.core.domain.Administrador;
import co.edu.uq.cinemauq.core.domain.Cliente;
import co.edu.uq.cinemauq.core.domain.Usuario;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utilidad para cargar datos de prueba de usuarios (clientes y administradores).
 */
public class UserSeeder {

    private static Map<String, String> contrasenas = new HashMap<>();

    static {
        // Contraseñas de prueba para cada usuario
        contrasenas.put("CLI001", "juan123");
        contrasenas.put("CLI002", "maria123");
        contrasenas.put("CLI003", "carlos123");
        contrasenas.put("CLI004", "ana123");
        contrasenas.put("CLI005", "pedro123");
        contrasenas.put("ADM001", "admin123");
        contrasenas.put("ADM002", "gerente123");
    }

    /**
     * Carga una lista de usuarios de prueba incluyendo clientes y administradores.
     */
    public static List<Usuario> cargarUsuarios() {
        List<Usuario> usuarios = new ArrayList<>();

        // Clientes de prueba
        Cliente cliente1 = new Cliente("CLI001", "Juan Pérez", "juan.perez@email.com");
        // cliente1.getTarjeta().recargar(50000.0); // $50,000 COP - Saldo inicial eliminado
        usuarios.add(cliente1);

        Cliente cliente2 = new Cliente("CLI002", "María García", "maria.garcia@email.com");
        // cliente2.getTarjeta().recargar(75000.0); // $75,000 COP - Saldo inicial eliminado
        usuarios.add(cliente2);

        Cliente cliente3 = new Cliente("CLI003", "Carlos Rodríguez", "carlos.rodriguez@email.com");
        // cliente3.getTarjeta().recargar(100000.0); // $100,000 COP - Saldo inicial eliminado
        usuarios.add(cliente3);

        Cliente cliente4 = new Cliente("CLI004", "Ana Martínez", "ana.martinez@email.com");
        // cliente4.getTarjeta().recargar(40000.0); // $40,000 COP - Saldo inicial eliminado
        usuarios.add(cliente4);

        Cliente cliente5 = new Cliente("CLI005", "Pedro López", "pedro.lopez@email.com");
        // cliente5.getTarjeta().recargar(60000.0); // $60,000 COP - Saldo inicial eliminado
        usuarios.add(cliente5);

        // Administradores de prueba
        Administrador admin1 = new Administrador("ADM001", "Admin Principal", "admin@cinemauq.edu");
        usuarios.add(admin1);

        Administrador admin2 = new Administrador("ADM002", "Gerente Cine", "gerente@cinemauq.edu");
        usuarios.add(admin2);

        return usuarios;
    }

    /**
     * Obtiene la contraseña de un usuario por su ID.
     */
    public static String getContrasena(String usuarioId) {
        return contrasenas.get(usuarioId);
    }

    /**
     * Valida la contraseña de un usuario.
     */
    public static boolean validarContrasena(String usuarioId, String contrasena) {
        String contrasenaAlmacenada = contrasenas.get(usuarioId);
        return contrasenaAlmacenada != null && contrasenaAlmacenada.equals(contrasena);
    }

    /**
     * Agrega un nuevo usuario con contraseña.
     */
    public static void agregarUsuario(Usuario usuario, String contrasena) {
        contrasenas.put(usuario.getId(), contrasena);
    }

    /**
     * Busca un usuario por su ID.
     */
    public static Usuario buscarUsuarioPorId(String id) {
        return cargarUsuarios().stream()
            .filter(u -> u.getId().equals(id))
            .findFirst()
            .orElse(null);
    }

    /**
     * Busca un usuario por su email.
     */
    public static Usuario buscarUsuarioPorEmail(String email) {
        return cargarUsuarios().stream()
            .filter(u -> u.getCorreo().equals(email))
            .findFirst()
            .orElse(null);
    }

    /**
     * Obtiene todos los clientes.
     */
    public static List<Cliente> obtenerClientes() {
        return cargarUsuarios().stream()
            .filter(u -> u instanceof Cliente)
            .map(u -> (Cliente) u)
            .toList();
    }

    /**
     * Obtiene todos los administradores.
     */
    public static List<Administrador> obtenerAdministradores() {
        return cargarUsuarios().stream()
            .filter(u -> u instanceof Administrador)
            .map(u -> (Administrador) u)
            .toList();
    }
}
