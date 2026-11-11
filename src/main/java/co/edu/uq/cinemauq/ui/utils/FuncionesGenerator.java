package co.edu.uq.cinemauq.ui.utils;

import co.edu.uq.cinemauq.core.domain.Funcion;
import co.edu.uq.cinemauq.core.domain.Sala;
import co.edu.uq.cinemauq.services.CatalogoCinema;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Adaptador de las vistas al catálogo compartido; conserva los IDs y la ocupación real. */
public final class FuncionesGenerator {
    private FuncionesGenerator() { }
    public static List<Funcion> generarFuncionesPrueba() {
        return CatalogoCinema.getInstance().funcionesDisponibles();
    }
    /** Ya no existe un caché local: navegar nunca borra los asientos vendidos. */
    @Deprecated
    public static void limpiarCache() { }
    public static String formatearFechaHora(LocalDateTime fechaHora) {
        return fechaHora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm"));
    }
    public static String generarIdAsiento(int fila, int columna) {
        return String.format("%c%d", (char) ('A' + fila), columna + 1);
    }
    public static List<String> generarIdsAsientos(Sala sala) {
        List<String> asientos = new ArrayList<>();
        for (int f=0; f<sala.getCapacidadFilas(); f++)
            for (int c=0; c<sala.getCapacidadColumnas(); c++) asientos.add(generarIdAsiento(f,c));
        return asientos;
    }
}