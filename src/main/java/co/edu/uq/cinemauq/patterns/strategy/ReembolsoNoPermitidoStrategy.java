package co.edu.uq.cinemauq.patterns.strategy;

import java.time.LocalDateTime;
import java.time.Duration;

/**
 * Estrategia de reembolso no permitido (0%).
 * Aplicable cuando faltan menos de 2 horas para la función.
 * Regla de Negocio: RN-008 (Sin reembolso <2h antes de la función)
 */
public class ReembolsoNoPermitidoStrategy implements PoliticaReembolso {
    
    private static final int HORAS_MINIMAS = 2;
    
    @Override
    public double calcularPorcentaje(LocalDateTime fechaFuncion, LocalDateTime fechaCancelacion) {
        return 0.0; // 0% de reembolso
    }
    
    @Override
    public String getDescripcion() {
        return "Reembolso no permitido (0%) - Menos de " + HORAS_MINIMAS + " horas antes de la función";
    }
    
    /**
     * Verifica si esta estrategia aplica según el tiempo restante.
     * @param fechaFuncion Fecha de la función
     * @param fechaCancelacion Fecha de cancelación
     * @return true si no aplica reembolso
     */
    public boolean aplica(LocalDateTime fechaFuncion, LocalDateTime fechaCancelacion) {
        Duration duracion = Duration.between(fechaCancelacion, fechaFuncion);
        return duracion.toHours() < HORAS_MINIMAS;
    }
}
