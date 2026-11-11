package co.edu.uq.cinemauq.core.managers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Gestor centralizado de asientos ocupados por función.
 * Mantiene un registro de qué asientos están ocupados para cada función.
 */
public class AsientosManager {

    private static AsientosManager instance;
    private Map<String, Set<String>> asientosOcupadosPorFuncion;

    private AsientosManager() {
        this.asientosOcupadosPorFuncion = new HashMap<>();
    }

    /**
     * Obtiene la instancia única del gestor (Singleton).
     */
    public static synchronized AsientosManager getInstance() {
        if (instance == null) {
            instance = new AsientosManager();
        }
        return instance;
    }

    /**
     * Obtiene los asientos ocupados para una función específica.
     * @param funcionId Identificador único de la función
     * @return Set con los IDs de asientos ocupados
     */
    public synchronized Set<String> getAsientosOcupados(String funcionId) {
        return Set.copyOf(asientosOcupadosPorFuncion.getOrDefault(funcionId, Set.of()));
    }

    /**
     * Marca asientos como ocupados para una función específica.
     * @param funcionId Identificador único de la función
     * @param asientos Set con los IDs de asientos a marcar como ocupados
     */
    public synchronized void marcarAsientosOcupados(String funcionId, Set<String> asientos) {
        asientosOcupadosPorFuncion.computeIfAbsent(funcionId, k -> new HashSet<>()).addAll(asientos);
        System.out.println("Asientos marcados como ocupados para función " + funcionId + ": " + asientos);
    }

    /**
     * Marca un asiento como ocupado para una función específica.
     * @param funcionId Identificador único de la función
     * @param asientoId ID del asiento a marcar como ocupado
     */
    public synchronized void marcarAsientoOcupado(String funcionId, String asientoId) {
        asientosOcupadosPorFuncion.computeIfAbsent(funcionId, k -> new HashSet<>()).add(asientoId);
        System.out.println("Asiento " + asientoId + " marcado como ocupado para función " + funcionId);
    }

    /**
     * Libera asientos para una función específica (útil para pruebas o cancelaciones).
     * @param funcionId Identificador único de la función
     * @param asientos Set con los IDs de asientos a liberar
     */
    public synchronized void liberarAsientos(String funcionId, Set<String> asientos) {
        Set<String> ocupados = asientosOcupadosPorFuncion.get(funcionId);
        if (ocupados != null) {
            ocupados.removeAll(asientos);
            System.out.println("Asientos liberados para función " + funcionId + ": " + asientos);
        }
    }

    /**
     * Verifica si un asiento está ocupado para una función específica.
     * @param funcionId Identificador único de la función
     * @param asientoId ID del asiento a verificar
     * @return true si el asiento está ocupado, false en caso contrario
     */
    public synchronized boolean isAsientoOcupado(String funcionId, String asientoId) {
        Set<String> ocupados = asientosOcupadosPorFuncion.get(funcionId);
        return ocupados != null && ocupados.contains(asientoId);
    }

    /**
     * Limpia todos los asientos ocupados para una función específica.
     * @param funcionId Identificador único de la función
     */
    public synchronized void limpiarFuncion(String funcionId) {
        asientosOcupadosPorFuncion.remove(funcionId);
        System.out.println("Asientos limpiados para función " + funcionId);
    }

    /**
     * Limpia todos los asientos ocupados (útil para reiniciar la aplicación).
     */
    public synchronized void limpiarTodo() {
        asientosOcupadosPorFuncion.clear();
        System.out.println("Todos los asientos ocupados han sido limpiados");
    }

    public synchronized <T> T ejecutarTransaccion(java.util.function.Supplier<T> operacion) {
        Map<String, Set<String>> respaldo = new HashMap<>();
        asientosOcupadosPorFuncion.forEach((id, asientos) -> respaldo.put(id, new HashSet<>(asientos)));
        try {
            return operacion.get();
        } catch (RuntimeException | Error fallo) {
            asientosOcupadosPorFuncion.clear();
            asientosOcupadosPorFuncion.putAll(respaldo);
            throw fallo;
        }
    }
}
