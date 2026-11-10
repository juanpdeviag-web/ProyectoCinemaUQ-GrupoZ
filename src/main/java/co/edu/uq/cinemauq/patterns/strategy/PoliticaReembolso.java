package co.edu.uq.cinemauq.patterns.strategy;

import java.time.LocalDateTime;

/**
 * Interfaz para el patrón Strategy de políticas de reembolso.
 * Implementa: RF-0XX (Cancelación y Reembolso)
 * Patrón de Comportamiento: Strategy
 * Problema: Diferentes políticas de reembolso según el tiempo restante y quién cancela
 * Justificación: Strategy permite cambiar el algoritmo de cálculo de reembolso dinámicamente
 */
public interface PoliticaReembolso {
    
    /**
     * Calcula el porcentaje de reembolso aplicable según la política.
     * @param fechaFuncion Fecha y hora de la función
     * @param fechaCancelacion Fecha y hora de la solicitud de cancelación
     * @return Porcentaje de reembolso (0.0 a 1.0)
     */
    double calcularPorcentaje(LocalDateTime fechaFuncion, LocalDateTime fechaCancelacion);
    
    /**
     * Retorna la descripción de la política aplicada.
     * @return Descripción legible de la política
     */
    String getDescripcion();
}
