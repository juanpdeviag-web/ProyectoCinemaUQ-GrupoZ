package co.edu.uq.cinemauq.core.seed;

import co.edu.uq.cinemauq.core.domain.Pelicula;
import co.edu.uq.cinemauq.core.domain.Sala;
import co.edu.uq.cinemauq.core.domain.Cliente;
import co.edu.uq.cinemauq.core.domain.Administrador;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class DataSeeder {

    private DataSeeder() {
    }

    public static List<Pelicula> cargarPeliculas() {
        return List.of(
                new Pelicula("Interestelar", "Ciencia ficción", 169),
                new Pelicula("El origen", "Ciencia ficción", 148),
                new Pelicula("Toy Story", "Animación", 81),
                new Pelicula("El padrino", "Drama", 175),
                new Pelicula("Avengers: Endgame", "Acción", 181),
                new Pelicula("Matrix", "Ciencia ficción", 136),
                new Pelicula("Gladiador", "Acción", 155),
                new Pelicula("El Rey León", "Animación", 118),
                new Pelicula("Titanic", "Drama", 194),
                new Pelicula("Jurassic Park", "Ciencia ficción", 127)
        );
    }
    public static List<Sala> cargarSalas() {
        return List.of(
                new Sala("Sala 1", 10, 12),
                new Sala("Sala 2", 8, 10),
                new Sala("Sala 3", 12, 15),
                new Sala("Sala VIP", 6, 8),
                new Sala("Sala IMAX", 14, 16)
        );
    }
    public static List<Cliente> cargarClientes() {
        return List.of(
                new Cliente("cliente1", "Juan Pérez", "juan.perez@email.com"),
                new Cliente("cliente2", "María García", "maria.garcia@email.com"),
                new Cliente("cliente3", "Carlos Rodríguez", "carlos.rodriguez@email.com"),
                new Cliente("cliente4", "Ana Martínez", "ana.martinez@email.com"),
                new Cliente("cliente5", "Pedro López", "pedro.lopez@email.com"),
                new Cliente("cliente6", "Laura Sánchez", "laura.sanchez@email.com"),
                new Cliente("cliente7", "Diego Torres", "diego.torres@email.com"),
                new Cliente("cliente8", "Sofía Ramírez", "sofia.ramirez@email.com"),
                new Cliente("cliente9", "Miguel Flores", "miguel.flores@email.com"),
                new Cliente("cliente10", "Lucía Morales", "lucia.morales@email.com")
        );
    }
    public static Administrador cargarAdministrador() {
        return new Administrador("admin", "Administrador Principal", "admin@cinemauq.edu.co");
    }
    public static DatosSemilla cargarDatos() {
        return new DatosSemilla(cargarPeliculas(), cargarSalas(), cargarClientes(), cargarAdministrador());
    }
    public record DatosSemilla(
            List<Pelicula> peliculas,
            List<Sala> salas,
            List<Cliente> clientes,
            Administrador administrador
    ) {
        public DatosSemilla {
            peliculas = List.copyOf(peliculas);
            salas = List.copyOf(salas);
            clientes = List.copyOf(clientes);
        }
    }
}

