package co.edu.uq.cinemauq.patterns.observer;

/**
 * Interfaz Observer para el patrón Observer.
 * Define el método que los observadores deben implementar para recibir actualizaciones.
 */
public interface EstadisticasObserver {
    /**
     * Método llamado cuando hay cambios en las estadísticas.
     * @param evento El evento de estadísticas con los datos actualizados.
     */
    void actualizar(EstadisticasEvent evento);
}
