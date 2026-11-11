package co.edu.uq.cinemauq.core.managers;

import co.edu.uq.cinemauq.core.domain.ReservaAsiento;
import co.edu.uq.cinemauq.core.domain.CompraConfirmada;
import co.edu.uq.cinemauq.core.exceptions.CompraException;

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
    private final Map<String, CompraConfirmada> comprasConfirmadas = new java.util.LinkedHashMap<>();

    public ReservasManager() {
        this.reservasPorUsuario = new HashMap<>();
        this.todasLasReservas = new ArrayList<>();
    }

    /**
     * Obtiene la instancia única del gestor (Singleton).
     */
    public static synchronized ReservasManager getInstance() {
        if (instance == null) {
            instance = new ReservasManager();
        }
        return instance;
    }

    /**
     * Agrega una reserva de asiento.
     * @param reserva La reserva a agregar
     */
    public synchronized void agregarReserva(ReservaAsiento reserva) {
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
    public synchronized List<ReservaAsiento> getReservasPorUsuario(String usuarioId) {
        return List.copyOf(reservasPorUsuario.getOrDefault(usuarioId, List.of()));
    }

    /**
     * Obtiene todas las reservas realizadas.
     * @return Lista de todas las reservas
     */
    public synchronized List<ReservaAsiento> getTodasLasReservas() {
        return new ArrayList<>(todasLasReservas);
    }

    /**
     * Obtiene las reservas para una función específica.
     * @param funcionId ID de la función
     * @return Lista de reservas para esa función
     */
    public synchronized List<ReservaAsiento> getReservasPorFuncion(String funcionId) {
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
    public synchronized void limpiarTodo() {
        reservasPorUsuario.clear();
        todasLasReservas.clear();
        comprasConfirmadas.clear();
        System.out.println("Todas las reservas han sido limpiadas");
    }

    public synchronized void registrarCompra(CompraConfirmada registro) {
        String id = registro.compra().id();
        CompraConfirmada existente = comprasConfirmadas.get(id);
        if (existente != null) {
            if (!existente.compra().equals(registro.compra())
                    || !existente.cliente().getId().equals(registro.cliente().getId())
                    || existente.tarjetaPago() != registro.tarjetaPago()
                    || existente.importePagado().compareTo(registro.importePagado()) != 0) {
                throw new CompraException("Ya existe otra operación registrada con el ID: " + id);
            }
            return;
        }
        comprasConfirmadas.put(id, registro);
        registro.reservas().forEach(this::agregarReserva);
        AsientosManager.getInstance().marcarAsientosOcupados(registro.compra().funcion().getId(),
                new java.util.HashSet<>(registro.compra().asientos()));
    }

    public synchronized CompraConfirmada buscarCompra(String id) {
        return comprasConfirmadas.get(id);
    }

    public synchronized List<CompraConfirmada> listarComprasPorUsuario(String usuarioId) {
        return comprasConfirmadas.values().stream()
                .filter(c -> c.cliente().getId().equals(usuarioId)).toList();
    }

    public synchronized List<CompraConfirmada> listarCompras() {
        return List.copyOf(comprasConfirmadas.values());
    }

    public synchronized void actualizarCompra(CompraConfirmada compra) {
        CompraConfirmada anterior = comprasConfirmadas.get(compra.compra().id());
        if (anterior == null || anterior.tarjetaPago() != compra.tarjetaPago()
                || anterior.cliente() != compra.cliente()
                || anterior.importePagado().compareTo(compra.importePagado()) != 0) {
            throw new CompraException("No se puede actualizar una compra distinta de la registrada");
        }
        comprasConfirmadas.put(compra.compra().id(), compra);
    }

    /** Pago y cancelación comparten el mismo límite transaccional del registro y los asientos. */
    public synchronized <T> T ejecutarTransaccion(java.util.function.Supplier<T> operacion) {
        Map<String, CompraConfirmada> comprasAntes = new java.util.LinkedHashMap<>(comprasConfirmadas);
        Map<String, List<ReservaAsiento>> reservasAntes = new HashMap<>();
        reservasPorUsuario.forEach((id, reservas) -> reservasAntes.put(id, new ArrayList<>(reservas)));
        List<ReservaAsiento> todasAntes = new ArrayList<>(todasLasReservas);
        try {
            return AsientosManager.getInstance().ejecutarTransaccion(operacion);
        } catch (RuntimeException | Error fallo) {
            comprasConfirmadas.clear();
            comprasConfirmadas.putAll(comprasAntes);
            reservasPorUsuario.clear();
            reservasPorUsuario.putAll(reservasAntes);
            todasLasReservas.clear();
            todasLasReservas.addAll(todasAntes);
            throw fallo;
        }
    }
}
