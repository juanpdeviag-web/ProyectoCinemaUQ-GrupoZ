package co.edu.uq.cinemauq.ui.utils;

import co.edu.uq.cinemauq.core.domain.Funcion;
import co.edu.uq.cinemauq.core.domain.Pelicula;
import co.edu.uq.cinemauq.core.domain.Sala;
import co.edu.uq.cinemauq.core.seed.DataSeeder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Utilidad para generar funciones de prueba combinando películas y salas.
 * Esta clase complementa DataSeeder para la interfaz gráfica.
 */
public class FuncionesGenerator {
    
    private static final Random random = new Random();
    
    /**
     * Genera funciones de prueba para las próximas 24 horas.
     * Combina las películas y salas de DataSeeder con horarios aleatorios.
     */
    public static List<Funcion> generarFuncionesPrueba() {
        DataSeeder.DatosSemilla datos = DataSeeder.cargarDatos();
        List<Pelicula> peliculas = datos.peliculas();
        List<Sala> salas = datos.salas();
        
        List<Funcion> funciones = new ArrayList<>();
        LocalDateTime ahora = LocalDateTime.now();
        
        // Generar 3 funciones por película en horarios diferentes
        for (Pelicula pelicula : peliculas) {
            for (int i = 0; i < 3; i++) {
                Sala sala = salas.get(random.nextInt(salas.size()));
                LocalDateTime horaFuncion = calcularHoraFuncion(ahora, i);
                
                Funcion funcion = new Funcion(pelicula, sala, horaFuncion);
                funciones.add(funcion);
            }
        }
        
        return funciones;
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
