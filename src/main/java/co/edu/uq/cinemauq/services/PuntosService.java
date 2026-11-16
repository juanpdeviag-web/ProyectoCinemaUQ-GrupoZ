package co.edu.uq.cinemauq.services;

import co.edu.uq.cinemauq.core.domain.Cliente;
import co.edu.uq.cinemauq.core.domain.PuntoCinemaUQ;
import co.edu.uq.cinemauq.core.exceptions.UsuarioException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class PuntosService {
    /** Devuelve los puntos generados; un importe menor a $100 no crea lote. */
    public int acumularPuntos(Cliente cliente, BigDecimal montoCompra) {
        Objects.requireNonNull(cliente, "El cliente es obligatorio");
        if (montoCompra == null || montoCompra.signum() < 0) {
            throw new UsuarioException("El monto de compra debe ser no negativo");
        }
        int cantidad = montoCompra.divideToIntegralValue(BigDecimal.valueOf(100)).intValueExact();
        if (cantidad > 0) {
            cliente.agregarPuntos(new PuntoCinemaUQ(cantidad, LocalDate.now()));
        }
        return cantidad;
    }

    public boolean verificarDisponibilidad(Cliente cliente, int puntosRequeridos) {
        Objects.requireNonNull(cliente, "El cliente es obligatorio");
        return cliente.verificarPuntosAcumulados(puntosRequeridos);
    }

    /** Consume puntos y devuelve su equivalencia monetaria: 100 puntos = $5. */
    public BigDecimal redimirPuntos(Cliente cliente, int cantidad) {
        Objects.requireNonNull(cliente, "El cliente es obligatorio");
        if (cantidad <= 0) {
            throw new UsuarioException("La cantidad a redimir debe ser positiva");
        }
        List<PuntoCinemaUQ> disponibles = cliente.getPuntosAcumulados().stream()
                .filter(PuntoCinemaUQ::estaDisponible)
                .sorted(Comparator.comparing(PuntoCinemaUQ::getFechaVencimiento))
                .toList();
        long saldo = disponibles.stream().mapToLong(PuntoCinemaUQ::getCantidadRestante).sum();
        if (saldo < cantidad) {
            throw new UsuarioException("Puntos disponibles insuficientes para la redención");
        }
        int pendientes = cantidad;
        for (PuntoCinemaUQ lote : disponibles) {
            int consumo = Math.min(pendientes, lote.getCantidadRestante());
            lote.redimir(consumo);
            pendientes -= consumo;
            if (pendientes == 0) {
                break;
            }
        }
        return BigDecimal.valueOf(cantidad).multiply(new BigDecimal("0.05"));
    }
}
