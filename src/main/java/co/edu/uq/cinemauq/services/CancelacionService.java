package co.edu.uq.cinemauq.services;

import co.edu.uq.cinemauq.core.domain.*;
import co.edu.uq.cinemauq.core.exceptions.CompraException;
import co.edu.uq.cinemauq.core.exceptions.ReembolsoDuplicadoException;
import co.edu.uq.cinemauq.core.managers.AsientosManager;
import co.edu.uq.cinemauq.core.managers.ReservasManager;
import co.edu.uq.cinemauq.core.ports.IPagoProcessor;
import co.edu.uq.cinemauq.core.repository.UserRepository;
import co.edu.uq.cinemauq.patterns.strategy.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/** Cancelación monetaria sobre las mismas operaciones confirmadas por PagoCompraService. */
public class CancelacionService {
    private final ReservasManager registro;
    private final Clock reloj;
    private final PoliticaReembolso completa = new ReembolsoCompletoStrategy();
    private final PoliticaReembolso parcial = new ReembolsoParcialStrategy();
    private final PoliticaReembolso noPermitida = new ReembolsoNoPermitidoStrategy();

    public CancelacionService() { this(ReservasManager.getInstance(), Clock.systemDefaultZone()); }

    /** Compatibilidad con la composición anterior; ya no busca al primer cliente del repositorio. */
    public CancelacionService(UserRepository usuarios, IPagoProcessor pagos) { this(); }

    public CancelacionService(ReservasManager registro, Clock reloj) {
        this.registro = Objects.requireNonNull(registro);
        this.reloj = Objects.requireNonNull(reloj);
    }

    public void registrarCompra(CompraConfirmada compra) {
        registro.ejecutarTransaccion(() -> { registro.registrarCompra(compra); return null; });
    }

    public List<CompraConfirmada> listarComprasPorCliente(String clienteId) {
        return registro.listarComprasPorUsuario(clienteId);
    }

    public CompraConfirmada buscarCompra(String id) {
        CompraConfirmada compra = registro.buscarCompra(id);
        if (compra == null) throw new CompraException("Compra no encontrada con ID: " + id);
        return compra;
    }

    public Reembolso solicitarCancelacion(String id, String motivo, boolean porCine) {
        return cancelar(id, motivo, porCine, null);
    }

    public Reembolso solicitarCancelacionPorCliente(String id, String motivo) {
        return cancelar(id, motivo, false, null);
    }

    public Reembolso solicitarCancelacionPorCliente(String id, String motivo, String clienteId) {
        Objects.requireNonNull(clienteId, "El cliente es obligatorio");
        return cancelar(id, motivo, false, clienteId);
    }

    public Reembolso solicitarCancelacionPorCine(String id, String motivo) {
        return cancelar(id, motivo, true, null);
    }

    private Reembolso cancelar(String id, String motivo, boolean porCine, String clienteId) {
        return registro.ejecutarTransaccion(() -> {
            CompraConfirmada compra = buscarCompra(id);
            if (clienteId != null && !compra.cliente().getId().equals(clienteId)) {
                throw new CompraException("La compra no pertenece al cliente autenticado");
            }
            if (compra.reembolso() != null) throw new ReembolsoDuplicadoException(id);
            if (compra.compra().estaCancelada()) throw new CompraException("La compra ya está cancelada");
            LocalDateTime ahora = LocalDateTime.now(reloj);
            PoliticaReembolso politica = seleccionarPolitica(compra.compra().funcion().getFechaHora(), ahora, porCine);
            double porcentaje = politica.calcularPorcentaje(compra.compra().funcion().getFechaHora(), ahora);
            if (porcentaje == 0) throw new CompraException("No se permite el reembolso: " + politica.getDescripcion());
            SolicitudCancelacion solicitud = new SolicitudCancelacion(UUID.randomUUID().toString(), id, ahora, motivo, porCine);
            BigDecimal importe = compra.importePagado().multiply(BigDecimal.valueOf(porcentaje))
                    .setScale(2, RoundingMode.HALF_UP);
            Reembolso reembolso = new Reembolso(UUID.randomUUID().toString(), id, importe.doubleValue(),
                    porcentaje, ahora, politica.getDescripcion(), true);
            TarjetaVirtual tarjeta = compra.tarjetaPago();
            return tarjeta.ejecutarTransaccion(() -> {
                if (!Double.isFinite(tarjeta.getSaldo())) throw new CompraException("El saldo de la tarjeta no es válido");
                if (importe.signum() > 0) {
                    double saldoAnterior = tarjeta.getSaldo();
                    Transaccion movimiento = new Transaccion(UUID.randomUUID().toString(), TipoTransaccion.REEMBOLSO,
                            importe.doubleValue(), saldoAnterior, saldoAnterior + importe.doubleValue(), ahora,
                            "Reembolso compra " + id + " - " + politica.getDescripcion());
                    tarjeta.agregarSaldoSinTransaccion(importe.doubleValue());
                    tarjeta.registrarTransaccion(movimiento);
                }
                AsientosManager.getInstance().liberarAsientos(compra.compra().funcion().getId(),
                        new HashSet<>(compra.compra().asientos()));
                registro.actualizarCompra(compra.conReembolso(solicitud, reembolso));
                return reembolso;
            });
        });
    }

    public boolean validarTiempo(String id) {
        CompraConfirmada compra = registro.buscarCompra(id);
        return compra != null && !compra.compra().estaCancelada() && porcentajeReembolso(id) > 0;
    }

    public double porcentajeReembolso(String id) {
        CompraConfirmada compra = buscarCompra(id);
        if (compra.compra().estaCancelada()) return 0;
        LocalDateTime ahora = LocalDateTime.now(reloj);
        return seleccionarPolitica(compra.compra().funcion().getFechaHora(), ahora, false)
                .calcularPorcentaje(compra.compra().funcion().getFechaHora(), ahora);
    }

    private PoliticaReembolso seleccionarPolitica(LocalDateTime funcion, LocalDateTime cancelacion, boolean porCine) {
        if (porCine) return completa;
        Duration restante = Duration.between(cancelacion, funcion);
        // Exactamente 24 horas pertenece al intervalo de 2 a 24; más de 24 permite 100 %.
        if (restante.compareTo(Duration.ofHours(24)) > 0) return completa;
        if (restante.compareTo(Duration.ofHours(2)) >= 0) return parcial;
        return noPermitida;
    }

    public List<Reembolso> getHistorialReembolsos() {
        return registro.listarCompras().stream().map(CompraConfirmada::reembolso).filter(Objects::nonNull).toList();
    }

    public List<SolicitudCancelacion> getSolicitudesCancelacion() {
        return registro.listarCompras().stream().map(CompraConfirmada::solicitud).filter(Objects::nonNull).toList();
    }

    public Map<String, co.edu.uq.cinemauq.core.factory.Compra> getCompras() {
        Map<String, co.edu.uq.cinemauq.core.factory.Compra> compras = new LinkedHashMap<>();
        registro.listarCompras().forEach(c -> compras.put(c.compra().id(), c.compra()));
        return Collections.unmodifiableMap(compras);
    }
}
