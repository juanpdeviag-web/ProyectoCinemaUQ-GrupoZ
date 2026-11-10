package co.edu.uq.cinemauq.patterns.strategy;

import java.time.LocalDateTime;
import java.time.Duration;

/**
 * Estrategia de reembolso completo (100%).
 * Aplicable cuando faltan más de 24 horas para la función o cuando el cine cancela.
 * Regla de Negocio: RN-006 (Reembolso completo >24h o cancelación por cine)
 */
public class ReembolsoCompletoStrategy implements PoliticaReembolso {
    
    private static final int HORAS_MINIMAS = 24;
    
    @Override
    public double calcularPorcentaje(LocalDateTime fechaFuncion, LocalDateTime fechaCancelacion) {
        return 1.0; // 100% de reembolso
    }
    
    @Override
    public String getDescripcion() {
        return "Reembolso completo (100%) - Más de " + HORAS_MINIMAS + " horas antes de la función o cancelación por el cine";
    }
    
    /**
     * Verifica si esta estrategia aplica según el tiempo restante.
     * @param fechaFuncion Fecha de la función
     * @param fechaCancelacion Fecha de cancelación
     * @return true si aplica reembolso completo
     */
    public boolean aplica(LocalDateTime fechaFuncion, LocalDateTime fechaCancelacion) {
        Duration duracion = Duration.between(fechaCancelacion, fechaFuncion);
        return duracion.toHours() >= HORAS_MINIMAS;
    }
}
