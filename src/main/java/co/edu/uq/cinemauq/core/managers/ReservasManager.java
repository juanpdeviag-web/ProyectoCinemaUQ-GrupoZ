package co.edu.uq.cinemauq.core.managers;

import co.edu.uq.cinemauq.core.domain.ReservaAsiento;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gestor centralizado de reservas de asientos.
 * Mantiene un registro de todas las reservas realizadas por usuario.
 */
public class ReservasManager {

    private static ReservasManager instance;
    private Map<String, List<ReservaAsiento>> reservasPorUsuario;
    private List<ReservaAsiento> todasLasReservas;

    private ReservasManager() {
        this.reservasPorUsuario = new HashMap<>();
        this.todasLasReservas = new ArrayList<>();
    }

    /**
     * Obtiene la instancia única del gestor (Singleton).
     */
    public static ReservasManager getInstance() {
        if (instance == null) {
            instance = new ReservasManager();
        }
        return instance;
    }

    /**
     * Agrega una reserva de asiento.
     * @param reserva La reserva a agregar
     */
    public void agregarReserva(ReservaAsiento reserva) {
        String usuarioId = reserva.getUsuarioId();
        reservasPorUsuario.computeIfAbsent(usuarioId, k -> new ArrayList<>()).add(reserva);
        todasLasReservas.add(reserva);
        System.out.println("✅ Reserva agregada: " + reserva);
    }

    /**
     * Obtiene todas las reservas de un usuario específico.
     * @param usuarioId ID del usuario
     * @return Lista de reservas del usuario
     */
    public List<ReservaAsiento> getReservasPorUsuario(String usuarioId) {
        return reservasPorUsuario.getOrDefault(usuarioId, new ArrayList<>());
    }

    /**
     * Obtiene todas las reservas realizadas.
     * @return Lista de todas las reservas
     */
    public List<ReservaAsiento> getTodasLasReservas() {
        return new ArrayList<>(todasLasReservas);
    }

    /**
     * Obtiene las reservas para una función específica.
     * @param funcionId ID de la función
     * @return Lista de reservas para esa función
     */
    public List<ReservaAsiento> getReservasPorFuncion(String funcionId) {
        List<ReservaAsiento> reservas = new ArrayList<>();
        for (ReservaAsiento reserva : todasLasReservas) {
            if (reserva.getFuncionId().equals(funcionId)) {
                reservas.add(reserva);
            }
        }
        return reservas;
    }

    /**
     * Limpia todas las reservas (útil para reiniciar la aplicación).
     */
    public void limpiarTodo() {
        reservasPorUsuario.clear();
        todasLasReservas.clear();
        System.out.println("Todas las reservas han sido limpiadas");
    }
}
