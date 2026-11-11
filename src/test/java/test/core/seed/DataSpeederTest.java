package test.core.seed;

import co.edu.uq.cinemauq.core.seed.DataSeeder;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DataSpeederTest {
    @Test
    void CargarPeliculasDePrueba() {
        var peliculas = DataSeeder.cargarPeliculas();

        assertNotNull(peliculas);
        assertEquals(5, peliculas.size());
        assertEquals("Interestelar", peliculas.get(0).getTitulo());
    }

    @Test
    void CargarSalasDePrueba() {
        var salas = DataSeeder.cargarSalas();

        assertNotNull(salas);
        assertEquals(4, salas.size());
    }

    @Test
    void CargarTodosLosDatosSemilla() {
        var datos = DataSeeder.cargarDatos();

        assertEquals(5, datos.peliculas().size());
        assertEquals(4, datos.salas().size());
    }
}
