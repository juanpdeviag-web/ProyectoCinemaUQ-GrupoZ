package co.edu.uq.cinemauq.services;

import co.edu.uq.cinemauq.core.domain.*;
import co.edu.uq.cinemauq.core.exceptions.CompraException;
import co.edu.uq.cinemauq.core.exceptions.ReembolsoDuplicadoException;
import co.edu.uq.cinemauq.patterns.strategy.*;
import co.edu.uq.cinemauq.core.repository.UserRepository;
import co.edu.uq.cinemauq.core.ports.IPagoProcessor;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.*;

/**
 * Servicio para gestionar cancelaciones y reembolsos de compras.
 * Implementa: RF-0XX (Cancelación y Reembolso)
 * Patrón de Comportamiento: Strategy
 * Regla de Negocio: RN-014 (Políticas de reembolso según tiempo)
 */
public class CancelacionService {
    
    private final UserRepository userRepository;
    private final IPagoProcessor pagoProcessor;
    private final Map<String, co.edu.uq.cinemauq.core.factory.Compra> compras;
    private final Set<String> comprasReembolsadas;
    private final List<Reembolso> historialReembolsos;
    private final List<SolicitudCancelacion> solicitudesCancelacion;
    
    private final PoliticaReembolso reembolsoCompletoStrategy;
    private final PoliticaReembolso reembolsoParcialStrategy;
    private final PoliticaReembolso reembolsoNoPermitidoStrategy;
    
    public CancelacionService(UserRepository userRepository, IPagoProcessor pagoProcessor) {
        this.userRepository = userRepository;
        this.pagoProcessor = pagoProcessor;
        this.compras = new HashMap<>();
        this.comprasReembolsadas = new HashSet<>();
        this.historialReembolsos = new ArrayList<>();
        this.solicitudesCancelacion = new ArrayList<>();
        
        this.reembolsoCompletoStrategy = new ReembolsoCompletoStrategy();
        this.reembolsoParcialStrategy = new ReembolsoParcialStrategy();
        this.reembolsoNoPermitidoStrategy = new ReembolsoNoPermitidoStrategy();
    }
    
    /**
     * Registra una compra en el sistema para seguimiento de cancelaciones.
     */
    public void registrarCompra(co.edu.uq.cinemauq.core.factory.Compra compra) {
        compras.put(compra.id(), compra);
    }
    
    /**
     * Solicita la cancelación de una compra.
     * @param idCompra ID de la compra a cancelar
     * @param motivo Motivo de la cancelación
     * @param canceladaPorCine true si la cancelación es por el cine, false si es por el cliente
     * @return Reembolso generado
     */
    public Reembolso solicitarCancelacion(String idCompra, String motivo, boolean canceladaPorCine) {
        co.edu.uq.cinemauq.core.factory.Compra compra = compras.get(idCompra);
        if (compra == null) {
            throw new CompraException("Compra no encontrada con ID: " + idCompra);
        }
        
        if (compra.estaCancelada()) {
            throw new CompraException("La compra ya está cancelada");
        }
        
        if (comprasReembolsadas.contains(idCompra)) {
            throw new ReembolsoDuplicadoException(idCompra);
        }
        
        SolicitudCancelacion solicitud = SolicitudCancelacion.crear(idCompra, motivo, canceladaPorCine);
        solicitudesCancelacion.add(solicitud);
        
        LocalDateTime fechaFuncion = compra.funcion().getFechaHora();
        LocalDateTime fechaCancelacion = solicitud.fechaSolicitud();
        
        PoliticaReembolso politica = seleccionarPolitica(fechaFuncion, fechaCancelacion, canceladaPorCine);
        double porcentaje = politica.calcularPorcentaje(fechaFuncion, fechaCancelacion);
        
        if (porcentaje == 0.0) {
            throw new CompraException("No se permite el reembolso: " + politica.getDescripcion());
        }
        
        double montoOriginal = calcularMontoCompra(compra);
        Reembolso reembolso = Reembolso.crear(idCompra, montoOriginal, porcentaje, politica.getDescripcion());
        
        procesarReembolso(reembolso, compra);
        
        return reembolso;
    }
    
    /**
     * Solicita cancelación por parte del cliente.
     */
    public Reembolso solicitarCancelacionPorCliente(String idCompra, String motivo) {
        return solicitarCancelacion(idCompra, motivo, false);
    }
    
    /**
     * Solicita cancelación por parte del cine.
     */
    public Reembolso solicitarCancelacionPorCine(String idCompra, String motivo) {
        return solicitarCancelacion(idCompra, motivo, true);
    }
    
    /**
     * Valida si una compra puede ser cancelada según el tiempo restante.
     */
    public boolean validarTiempo(String idCompra) {
        co.edu.uq.cinemauq.core.factory.Compra compra = compras.get(idCompra);
        if (compra == null) {
            return false;
        }
        
        LocalDateTime fechaFuncion = compra.funcion().getFechaHora();
        LocalDateTime ahora = LocalDateTime.now();
        
        Duration duracion = Duration.between(ahora, fechaFuncion);
        return duracion.toHours() >= 2;
    }
    
    /**
     * Selecciona la política de reembolso apropiada según el tiempo y quién cancela.
     */
    private PoliticaReembolso seleccionarPolitica(LocalDateTime fechaFuncion, LocalDateTime fechaCancelacion, boolean canceladaPorCine) {
        if (canceladaPorCine) {
            return reembolsoCompletoStrategy;
        }
        
        Duration duracion = Duration.between(fechaCancelacion, fechaFuncion);
        long horas = duracion.toHours();
        
        if (horas >= 24) {
            return reembolsoCompletoStrategy;
        } else if (horas >= 2) {
            return reembolsoParcialStrategy;
        } else {
            return reembolsoNoPermitidoStrategy;
        }
    }
    
    /**
     * Procesa el reembolso actualizando la tarjeta virtual del cliente.
     */
    private void procesarReembolso(Reembolso reembolso, co.edu.uq.cinemauq.core.factory.Compra compra) {
        comprasReembolsadas.add(reembolso.idCompra());
        historialReembolsos.add(reembolso);
        
        co.edu.uq.cinemauq.core.factory.Compra compraCancelada = compra.cancelar();
        compras.put(compra.id(), compraCancelada);
        
        Cliente cliente = buscarClientePorCompra(compra);
        if (cliente != null) {
            TarjetaVirtual tarjeta = cliente.getTarjetaVirtual();
            tarjeta.agregarSaldoSinTransaccion(reembolso.montoReembolsado());
            
            Transaccion transaccion = new Transaccion(
                UUID.randomUUID().toString(),
                TipoTransaccion.REEMBOLSO,
                reembolso.montoReembolsado(),
                tarjeta.getSaldo() - reembolso.montoReembolsado(),
                tarjeta.getSaldo(),
                LocalDateTime.now(),
                "Reembolso compra " + reembolso.idCompra() + " - " + reembolso.politicaAplicada()
            );
            tarjeta.registrarTransaccion(transaccion);
        }
    }
    
    /**
     * Calcula el monto total de una compra (entradas + snacks).
     */
    private double calcularMontoCompra(co.edu.uq.cinemauq.core.factory.Compra compra) {
        double precioEntrada = compra.funcion().getPelicula().getPrecio();
        double totalEntradas = precioEntrada * compra.asientos().size();
        
        double totalSnacks = 0.0;
        for (String snack : compra.snacks()) {
            totalSnacks += 15.0;
        }
        
        return totalEntradas + totalSnacks;
    }
    
    /**
     * Busca el cliente asociado a una compra.
     */
    private Cliente buscarClientePorCompra(co.edu.uq.cinemauq.core.factory.Compra compra) {
        return userRepository.listar().stream()
            .filter(u -> u instanceof Cliente)
            .map(u -> (Cliente) u)
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Retorna el historial de reembolsos.
     */
    public List<Reembolso> getHistorialReembolsos() {
        return Collections.unmodifiableList(historialReembolsos);
    }
    
    /**
     * Retorna las solicitudes de cancelación.
     */
    public List<SolicitudCancelacion> getSolicitudesCancelacion() {
        return Collections.unmodifiableList(solicitudesCancelacion);
    }
    
    /**
     * Retorna las compras registradas.
     */
    public Map<String, co.edu.uq.cinemauq.core.factory.Compra> getCompras() {
        return Collections.unmodifiableMap(compras);
    }
}
