package co.edu.uq.cinemauq.patterns.observer;

import co.edu.uq.cinemauq.services.AdministracionService;
import co.edu.uq.cinemauq.core.domain.Usuario;
import co.edu.uq.cinemauq.core.domain.Administrador;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class EstadisticasManager implements EstadisticasSubject {
    private static EstadisticasManager instance;
    private final List<EstadisticasObserver> observers;
    private AdministracionService servicio;
    private LocalDate desde;
    private LocalDate hasta;
    private String clienteSeleccionado;
    private Map<String, Double> ultimosIndicadores;

    private EstadisticasManager() {
        this.observers = new CopyOnWriteArrayList<>();
        this.desde = LocalDate.now().withDayOfMonth(1);
        this.hasta = LocalDate.now().plusMonths(1);
        this.clienteSeleccionado = null;
        this.ultimosIndicadores = new LinkedHashMap<>();
    }

    public static synchronized EstadisticasManager getInstance() {
        if (instance == null) {
            instance = new EstadisticasManager();
        }
        return instance;
    }

    public void setServicio(AdministracionService servicio) {
        this.servicio = servicio;
    }

    public void setRangoFechas(LocalDate desde, LocalDate hasta) {
        this.desde = desde;
        this.hasta = hasta;
    }

    public void setClienteSeleccionado(String clienteId) {
        this.clienteSeleccionado = clienteId;
    }

    @Override
    public void registrarObserver(EstadisticasObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void eliminarObserver(EstadisticasObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notificarObservers(EstadisticasEvent evento) {
        for (EstadisticasObserver observer : observers) {
            try {
                observer.actualizar(evento);
            } catch (Exception e) {
                System.err.println("Error notificando observador: " + e.getMessage());
            }
        }
    }

    public void notificarVenta() {
        if (servicio != null) {
            ultimosIndicadores = servicio.indicadores(desde, hasta);
            EstadisticasEvent evento = new EstadisticasEvent(desde, hasta, ultimosIndicadores, "VENTA");
            notificarObservers(evento);
        }
    }

    public void notificarCancelacion() {
        if (servicio != null) {
            ultimosIndicadores = servicio.indicadores(desde, hasta);
            EstadisticasEvent evento = new EstadisticasEvent(desde, hasta, ultimosIndicadores, "CANCELACION");
            notificarObservers(evento);
        }
    }

    public void notificarRecarga() {
        if (servicio != null) {
            ultimosIndicadores = servicio.indicadores(desde, hasta);
            EstadisticasEvent evento = new EstadisticasEvent(desde, hasta, ultimosIndicadores, "RECARGA");
            notificarObservers(evento);
        }
    }

    public void notificarFuncionCreada() {
        if (servicio != null) {
            ultimosIndicadores = servicio.indicadores(desde, hasta);
            EstadisticasEvent evento = new EstadisticasEvent(desde, hasta, ultimosIndicadores, "FUNCION_CREADA");
            notificarObservers(evento);
        }
    }

    public void notificarFuncionCancelada() {
        if (servicio != null) {
            ultimosIndicadores = servicio.indicadores(desde, hasta);
            EstadisticasEvent evento = new EstadisticasEvent(desde, hasta, ultimosIndicadores, "FUNCION_CANCELADA");
            notificarObservers(evento);
        }
    }

    public void notificarDatosIniciales() {
        if (servicio != null) {
            ultimosIndicadores = servicio.indicadores(desde, hasta);
            EstadisticasEvent evento = new EstadisticasEvent(desde, hasta, ultimosIndicadores, "DATOS_INICIALES");
            notificarObservers(evento);
        }
    }

    public void actualizarEstadisticas() {
        if (servicio != null) {
            ultimosIndicadores = servicio.indicadores(desde, hasta);
            if (clienteSeleccionado != null) {
                ultimosIndicadores = filtrarPorCliente(ultimosIndicadores, clienteSeleccionado);
            }
            EstadisticasEvent evento = new EstadisticasEvent(desde, hasta, ultimosIndicadores, "ACTUALIZACION");
            notificarObservers(evento);
        }
    }

    private Map<String, Double> filtrarPorCliente(Map<String, Double> indicadores, String clienteId) {
        Map<String, Double> filtrados = new LinkedHashMap<>(indicadores);
        var compras = servicio.compras().stream()
            .filter(c -> c.cliente().getId().equals(clienteId))
            .filter(c -> {
                var fecha = c.compra().fechaCompra().toLocalDate();
                return !fecha.isBefore(desde) && !fecha.isAfter(hasta);
            })
            .toList();
        
        double ventasCobradas = compras.stream()
            .filter(c -> !c.compra().estaCancelada() && c.reembolso() == null)
            .mapToDouble(c -> c.importePagado().doubleValue())
            .sum();
        
        double reembolsos = compras.stream()
            .filter(c -> c.reembolso() != null)
            .mapToDouble(c -> c.reembolso().montoReembolsado())
            .sum();
        
        double entradas = compras.stream()
            .filter(c -> !c.compra().estaCancelada())
            .mapToDouble(c -> c.compra().asientos().size())
            .sum();
        
        double confiteria = compras.stream()
            .mapToDouble(c -> c.detallePago().confiteria().doubleValue())
            .sum();
        double recargas = servicio.clientes("").stream()
            .filter(c -> c.getId().equals(clienteId))
            .findFirst()
            .map(cliente -> cliente.getTarjeta().obtenerHistorial().stream()
                .filter(t -> t.tipo() == co.edu.uq.cinemauq.core.domain.TipoTransaccion.RECARGA)
                .filter(t -> {
                    var fecha = t.fecha().toLocalDate();
                    return !fecha.isBefore(desde) && !fecha.isAfter(hasta);
                })
                .mapToDouble(co.edu.uq.cinemauq.core.domain.Transaccion::monto)
                .sum())
            .orElse(0.0);
        final long[] disponibles = {0}, vencidos = {0}, redimidos = {0};
        servicio.clientes("").stream()
            .filter(c -> c.getId().equals(clienteId))
            .findFirst()
            .ifPresent(cliente -> {
                for (var p : cliente.getPuntosAcumulados()) {
                    String estado = p.getEstado().getClass().getSimpleName();
                    redimidos[0] += (long) p.getCantidad() - p.getCantidadRestante();
                    if (estado.equals("PuntoVencido")) vencidos[0] += p.getCantidadRestante();
                    else if (estado.equals("PuntoDisponible")) disponibles[0] += p.getCantidadRestante();
                }
            });
        filtrados.put("Compras", (double) compras.size());
        filtrados.put("Ventas cobradas", ventasCobradas);
        filtrados.put("Reembolsos", reembolsos);
        filtrados.put("Entradas", entradas);
        filtrados.put("Confitería (subtotal)", confiteria);
        filtrados.put("Recargas", recargas);
        filtrados.put("Puntos disponibles", (double) disponibles[0]);
        filtrados.put("Puntos vencidos", (double) vencidos[0]);
        filtrados.put("Puntos redimidos", (double) redimidos[0]);
        
        return filtrados;
    }
    public Map<String, Double> getUltimosIndicadores() {
        return new LinkedHashMap<>(ultimosIndicadores);
    }
    public LocalDate[] getRangoFechas() {
        return new LocalDate[]{desde, hasta};
    }
    public AdministracionService getServicio() {
        return servicio;
    }
    public String getClienteSeleccionado() {
        return clienteSeleccionado;
    }
}
