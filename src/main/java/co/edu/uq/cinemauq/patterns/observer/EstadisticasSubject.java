package co.edu.uq.cinemauq.patterns.observer;

import java.util.Map;

/**
 * Interfaz Subject para el patrón Observer.
 * Permite registrar observadores y notificar cambios en las estadísticas.
 */
public interface EstadisticasSubject {
    /**
     * Registra un observador para recibir actualizaciones de estadísticas.
     * @param observer El observador a registrar.
     */
    void registrarObserver(EstadisticasObserver observer);
    
    /**
     * Elimina un observador de la lista de notificaciones.
     * @param observer El observador a eliminar.
     */
    void eliminarObserver(EstadisticasObserver observer);
    
    /**
     * Notifica a todos los observadores registrados sobre cambios en las estadísticas.
     * @param evento El evento de estadísticas con los datos actualizados.
     */
    void notificarObservers(EstadisticasEvent evento);
}
