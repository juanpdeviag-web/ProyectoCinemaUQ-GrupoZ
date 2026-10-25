package co.edu.uq.cinemauq.services;

import co.edu.uq.cinemauq.core.domain.Cliente;
import co.edu.uq.cinemauq.core.domain.TarjetaVirtual;
import co.edu.uq.cinemauq.core.domain.Transaccion;
import co.edu.uq.cinemauq.core.ports.IPagoProcessor;
import co.edu.uq.cinemauq.core.repository.UserRepository;
import java.util.List;

public class TarjetaVirtualService {
    private final UserRepository userRepository;
    private final IPagoProcessor pagoProcessor;

    public TarjetaVirtualService(UserRepository userRepository, IPagoProcessor pagoProcessor) {
        this.userRepository = userRepository;
        this.pagoProcessor = pagoProcessor;
    }

    public void recargarTarjeta(String idUsuario, double monto) {
        Cliente cliente = buscarCliente(idUsuario);
        TarjetaVirtual tarjeta = cliente.getTarjetaVirtual();
        if (tarjeta == null) {
            throw new IllegalStateException("El cliente no tiene tarjeta virtual asignada");
        }
        tarjeta.recargar(monto);
    }

    public double consultarSaldo(String idUsuario) {
        Cliente cliente = buscarCliente(idUsuario);
        TarjetaVirtual tarjeta = cliente.getTarjetaVirtual();
        if (tarjeta == null) {
            throw new IllegalStateException("El cliente no tiene tarjeta virtual asignada");
        }
        return tarjeta.getSaldo();
    }

    public List<Transaccion> consultarHistorial(String idUsuario) {
        Cliente cliente = buscarCliente(idUsuario);
        TarjetaVirtual tarjeta = cliente.getTarjetaVirtual();
        if (tarjeta == null) {
            throw new IllegalStateException("El cliente no tiene tarjeta virtual asignada");
        }
        return tarjeta.obtenerHistorial();
    }

    public String generarReporteTransacciones(String idUsuario) {
        Cliente cliente = buscarCliente(idUsuario);
        TarjetaVirtual tarjeta = cliente.getTarjetaVirtual();
        if (tarjeta == null) {
            throw new IllegalStateException("El cliente no tiene tarjeta virtual asignada");
        }
        return tarjeta.obtenerResumenTransacciones();
    }

    public void bloquearTarjeta(String idUsuario) {
        Cliente cliente = buscarCliente(idUsuario);
        TarjetaVirtual tarjeta = cliente.getTarjetaVirtual();
        if (tarjeta == null) {
            throw new IllegalStateException("El cliente no tiene tarjeta virtual asignada");
        }
        tarjeta.desactivar();
    }

    public void desbloquearTarjeta(String idUsuario) {
        Cliente cliente = buscarCliente(idUsuario);
        TarjetaVirtual tarjeta = cliente.getTarjetaVirtual();
        if (tarjeta == null) {
            throw new IllegalStateException("El cliente no tiene tarjeta virtual asignada");
        }
        tarjeta.activar();
    }

    public boolean procesarPago(String idUsuario, double monto) {
        Cliente cliente = buscarCliente(idUsuario);
        TarjetaVirtual tarjeta = cliente.getTarjetaVirtual();
        if (tarjeta == null) {
            throw new IllegalStateException("El cliente no tiene tarjeta virtual asignada");
        }
        return pagoProcessor.procesarPago(tarjeta, monto);
    }

    public void procesarReembolso(String idUsuario, double monto) {
        Cliente cliente = buscarCliente(idUsuario);
        TarjetaVirtual tarjeta = cliente.getTarjetaVirtual();
        if (tarjeta == null) {
            throw new IllegalStateException("El cliente no tiene tarjeta virtual asignada");
        }
        pagoProcessor.procesarReembolso(tarjeta, monto);
    }

    private Cliente buscarCliente(String idUsuario) {
        return userRepository.listar().stream()
            .filter(u -> u instanceof Cliente)
            .map(u -> (Cliente) u)
            .filter(c -> c.getId().equals(idUsuario))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + idUsuario));
    }
}
