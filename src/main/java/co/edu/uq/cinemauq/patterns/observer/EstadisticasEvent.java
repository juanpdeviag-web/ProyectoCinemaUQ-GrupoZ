package co.edu.uq.cinemauq.patterns.observer;

import java.time.LocalDate;
import java.util.Map;

/**
 * Evento que contiene los datos de estadísticas actualizados.
 * Se utiliza para notificar a los observadores sobre cambios en las métricas del sistema.
 */
public record EstadisticasEvent(
    LocalDate desde,
    LocalDate hasta,
    Map<String, Double> indicadores,
    String tipoCambio
) {
    /**
     * Tipos de cambios que pueden ocurrir en las estadísticas.
     */
    public enum TipoCambio {
        VENTA,
        CANCELACION,
        RECARGA,
        FUNCION_CREADA,
        FUNCION_CANCELADA,
        DATOS_INICIALES
    }
}
