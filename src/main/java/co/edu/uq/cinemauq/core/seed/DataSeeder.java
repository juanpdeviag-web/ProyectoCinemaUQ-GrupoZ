package co.edu.uq.cinemauq.core.seed;

import co.edu.uq.cinemauq.core.domain.Pelicula;
import co.edu.uq.cinemauq.core.domain.Sala;

import java.util.List;

/**
 * Clase utilitaria para carga de datos semilla.
 * Implementa: RF-002 (Consulta de Cartelera), RF-003 (Gestión de Salas)
 * Regla de Negocio: RN-006 (Protección de datos semilla)
 */

public final class DataSeeder {

    private DataSeeder() {
        // Clase utilitaria: no debe ser instanciada.
    }


     // Carga películas de prueba.
    public static List<Pelicula> cargarPeliculas() {
        return List.of(
                new Pelicula("Interestelar", "Ciencia ficción", 169),
                new Pelicula("El origen", "Ciencia ficción", 148),
                new Pelicula("Toy Story", "Animación", 81),
                new Pelicula("El padrino", "Drama", 175),
                new Pelicula("Avengers: Endgame", "Acción", 181)
        );
    }


     // Carga salas de prueba.

    public static List<Sala> cargarSalas() {
        return List.of(
                new Sala("Sala 1", 10, 12),
                new Sala("Sala 2", 8, 10),
                new Sala("Sala 3", 12, 15),
                new Sala("Sala VIP", 6, 8)
        );
    }


      //Carga semilla.

    public static DatosSemilla cargarDatos() {
        return new DatosSemilla(cargarPeliculas(), cargarSalas());
    }


     // Agrupa los datos semilla necesarios para las pruebas.

    public record DatosSemilla(
            List<Pelicula> peliculas,
            List<Sala> salas
    ) {
        public DatosSemilla {
            peliculas = List.copyOf(peliculas);
            salas = List.copyOf(salas);
        }
    }
}

