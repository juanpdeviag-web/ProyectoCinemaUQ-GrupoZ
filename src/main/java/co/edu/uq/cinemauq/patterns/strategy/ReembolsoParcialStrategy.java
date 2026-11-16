package co.edu.uq.cinemauq.patterns.strategy;

import java.time.LocalDateTime;
import java.time.Duration;

/**
 * Estrategia de reembolso parcial (80%).
 * Aplicable cuando faltan entre 2 y 24 horas para la función.
 * Regla de Negocio: RN-007 (Reembolso parcial 80% entre 2-24h)
 */
public class ReembolsoParcialStrategy implements PoliticaReembolso {
    
    private static final int HORAS_MINIMAS = 2;
    private static final int HORAS_MAXIMAS = 24;
    private static final double PORCENTAJE_REEMBOLSO = 0.8; // 80%
    
    @Override
    public double calcularPorcentaje(LocalDateTime fechaFuncion, LocalDateTime fechaCancelacion) {
        return PORCENTAJE_REEMBOLSO;
    }
    
    @Override
    public String getDescripcion() {
        return "Reembolso parcial (" + (PORCENTAJE_REEMBOLSO * 100) + "%) - Entre " + HORAS_MINIMAS + " y " + HORAS_MAXIMAS + " horas antes de la función";
    }
    
    /**
     * Verifica si esta estrategia aplica según el tiempo restante.
     * @param fechaFuncion Fecha de la función
     * @param fechaCancelacion Fecha de cancelación
     * @return true si aplica reembolso parcial
     */
    public boolean aplica(LocalDateTime fechaFuncion, LocalDateTime fechaCancelacion) {
        Duration duracion = Duration.between(fechaCancelacion, fechaFuncion);
        long horas = duracion.toHours();
        return horas >= HORAS_MINIMAS && horas < HORAS_MAXIMAS;
    }
}
