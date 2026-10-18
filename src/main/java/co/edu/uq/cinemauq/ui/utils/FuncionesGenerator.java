package co.edu.uq.cinemauq.ui.utils;

import co.edu.uq.cinemauq.core.domain.Funcion;
import co.edu.uq.cinemauq.core.domain.Pelicula;
import co.edu.uq.cinemauq.core.domain.Sala;
import co.edu.uq.cinemauq.core.managers.AsientosManager;
import co.edu.uq.cinemauq.core.seed.DataSeeder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Utilidad para generar funciones de prueba combinando películas y salas.
 * Esta clase complementa DataSeeder para la interfaz gráfica.
 */
public class FuncionesGenerator {

    private static final Random random = new Random();
    private static List<Funcion> funcionesCache = null;

    /**
     * Genera funciones de prueba para las próximas 24 horas.
     * Combina las películas y salas de DataSeeder con horarios aleatorios.
     * También genera asientos ocupados iniciales para cada función.
     * Las salas se asignan de manera estática (no aleatoria) para consistencia.
     * Las funciones se cachean para no regenerarlas cada vez (mantiene IDs consistentes).
     */
    public static List<Funcion> generarFuncionesPrueba() {
        // Si ya hay funciones cacheadas, reutilizarlas
        if (funcionesCache != null) {
            return funcionesCache;
        }

        DataSeeder.DatosSemilla datos = DataSeeder.cargarDatos();
        List<Pelicula> peliculas = datos.peliculas();
        List<Sala> salas = datos.salas();

        List<Funcion> funciones = new ArrayList<>();
        LocalDateTime ahora = LocalDateTime.now();

        // Generar 3 funciones por película en horarios diferentes
        int salaIndex = 0;
        for (Pelicula pelicula : peliculas) {
            for (int i = 0; i < 3; i++) {
                // Asignar sala de manera cíclica (no aleatoria) para consistencia
                Sala sala = salas.get(salaIndex % salas.size());
                salaIndex++;

                LocalDateTime horaFuncion = calcularHoraFuncion(ahora, i);

                Funcion funcion = new Funcion(pelicula, sala, horaFuncion);
                funciones.add(funcion);

                // Generar asientos ocupados iniciales (datos quemados) para esta función
                Set<String> asientosOcupados = generarAsientosOcupadosIniciales(sala);
                AsientosManager.getInstance().marcarAsientosOcupados(funcion.getId(), asientosOcupados);
                System.out.println("Asientos ocupados iniciales para función " + funcion.getId() + ": " + asientosOcupados);
            }
        }

        // Cachear las funciones para reutilizarlas
        funcionesCache = funciones;
        return funciones;
    }

    /**
     * Limpia el caché de funciones (útil para reiniciar la aplicación).
     */
    public static void limpiarCache() {
        funcionesCache = null;
        AsientosManager.getInstance().limpiarTodo();
        System.out.println("Caché de funciones limpiada");
    }

    /**
     * Genera asientos ocupados iniciales (datos quemados fijos) para una sala.
     * Usa asientos fijos en lugar de aleatorios para consistencia.
     */
    private static Set<String> generarAsientosOcupadosIniciales(Sala sala) {
        Set<String> ocupados = new HashSet<>();

        // Asientos fijos que siempre estarán ocupados (datos quemados)
        // Fila central (D, E, F, G) - asientos del medio
        ocupados.add("D5");
        ocupados.add("D6");
        ocupados.add("E4");
        ocupados.add("E5");
        ocupados.add("E6");
        ocupados.add("E7");
        ocupados.add("F4");
        ocupados.add("F5");
        ocupados.add("F6");
        ocupados.add("F7");
        ocupados.add("G5");
        ocupados.add("G6");

        // Algunos asientos en filas posteriores
        ocupados.add("H3");
        ocupados.add("H4");
        ocupados.add("H7");
        ocupados.add("H8");
        ocupados.add("I5");
        ocupados.add("I6");

        // Filtrar solo los asientos que existen en esta sala
        List<String> asientosValidos = generarIdsAsientos(sala);
        Set<String> ocupadosFiltrados = new HashSet<>();
        for (String asiento : ocupados) {
            if (asientosValidos.contains(asiento)) {
                ocupadosFiltrados.add(asiento);
            }
        }

        return ocupadosFiltrados;
    }
    
    /**
     * Calcula una hora de función basada en el índice.
     * Genera horarios para hoy y mañana.
     */
    private static LocalDateTime calcularHoraFuncion(LocalDateTime base, int indice) {
        int horaBase = 14; // Empezar a las 2 PM
        int[] intervalos = {0, 3, 6}; // 2 PM, 5 PM, 8 PM
        
        int dia = indice / 3;
        int intervalo = intervalos[indice % 3];
        
        return base.plusDays(dia)
                    .withHour(horaBase + intervalo)
                    .withMinute(0)
                    .withSecond(0)
                    .withNano(0);
    }
    
    /**
     * Formatea la fecha y hora de una función para mostrar en la UI.
     */
    public static String formatearFechaHora(LocalDateTime fechaHora) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm");
        return fechaHora.format(formatter);
    }
    
    /**
     * Genera un identificador único para un asiento basado en fila y columna.
     */
    public static String generarIdAsiento(int fila, int columna) {
        return String.format("%c%d", (char) ('A' + fila), columna + 1);
    }
    
    /**
     * Genera todos los identificadores de asientos para una sala.
     */
    public static List<String> generarIdsAsientos(Sala sala) {
        List<String> asientos = new ArrayList<>();
        int filas = sala.getCapacidadFilas();
        int columnas = sala.getCapacidadColumnas();
        
        for (int f = 0; f < filas; f++) {
            for (int c = 0; c < columnas; c++) {
                asientos.add(generarIdAsiento(f, c));
            }
        }
        
        return asientos;
    }
}
