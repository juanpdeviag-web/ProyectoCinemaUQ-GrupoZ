package co.edu.uq.cinemauq.services;

import co.edu.uq.cinemauq.core.domain.Cliente;
import co.edu.uq.cinemauq.core.domain.PuntoCinemaUQ;
import co.edu.uq.cinemauq.core.exceptions.PuntosException;
import co.edu.uq.cinemauq.core.exceptions.PuntosInsuficientesException;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de dominio para el programa de puntos CinemaUQ.
 *
 * Reglas documentadas del integrante 1:
 * - Acumulación: 1 punto por cada $100 de compra.
 * - Vigencia: cada lote vence un año después de su obtención.
 * - Redención: 100 puntos equivalen a $5.
 * - Consumo: se redimen primero los puntos más próximos a vencer.
 */
public class PuntosService {
    public static final double VALOR_COMPRA_POR_PUNTO = 100.0;
    public static final int PUNTOS_BASE_REDENCION = 100;
    public static final double VALOR_BASE_REDENCION = 5.0;

    /**
     * Acumula puntos sobre el valor efectivamente pagado de la compra.
     * Regla: 1 punto por cada $100 de compra pagados. Retorna la cantidad generada.
     */
    public int acumularPuntos(Cliente cliente, double valorCompra) {
        validarCliente(cliente);
        if (valorCompra < 0) {
            throw new PuntosException("El valor de la compra no puede ser negativo");
        }

        int puntosGanados = (int) Math.floor(valorCompra / VALOR_COMPRA_POR_PUNTO);
        if (puntosGanados > 0) {
            cliente.agregarLotePuntos(new PuntoCinemaUQ(puntosGanados, LocalDate.now()));
        }
        return puntosGanados;
    }

    /**
     * Comprueba si el cliente dispone de la cantidad solicitada en puntos vigentes.
     */
    public boolean verificarDisponibilidad(Cliente cliente, int puntosRequeridos) {
        validarCliente(cliente);
        if (puntosRequeridos < 0) {
            throw new PuntosException("Los puntos requeridos no pueden ser negativos");
        }
        actualizarEstados(cliente);
        return obtenerPuntosDisponibles(cliente) >= puntosRequeridos;
    }

    /**
     * Redime aplicando FEFO/FIFO por vencimiento: primero el lote con fecha de
     * vencimiento más cercana. Retorna el valor monetario equivalente.
     */
    public double redimirPuntos(Cliente cliente, int puntosARedimir) {
        validarCliente(cliente);
        if (puntosARedimir <= 0) {
            throw new PuntosException("La cantidad de puntos a redimir debe ser mayor que cero");
        }

        actualizarEstados(cliente);
        int disponibles = obtenerPuntosDisponibles(cliente);
        if (disponibles < puntosARedimir) {
            throw new PuntosInsuficientesException(disponibles, puntosARedimir);
        }

        List<PuntoCinemaUQ> lotesOrdenados = cliente.getPuntosAcumulados().stream()
                .filter(PuntoCinemaUQ::esValido)
                .sorted(Comparator.comparing(PuntoCinemaUQ::getFechaVencimiento)
                        .thenComparing(PuntoCinemaUQ::getFechaObtencion))
                .toList();

        int restantes = puntosARedimir;
        for (PuntoCinemaUQ lote : lotesOrdenados) {
            if (restantes == 0) {
                break;
            }
            int consumidos = lote.redimir(restantes);
            restantes -= consumidos;
        }

        return calcularValorRedencion(puntosARedimir);
    }

    public int obtenerPuntosDisponibles(Cliente cliente) {
        validarCliente(cliente);
        actualizarEstados(cliente);
        return cliente.getPuntosAcumulados().stream()
                .filter(PuntoCinemaUQ::esValido)
                .mapToInt(PuntoCinemaUQ::getCantidad)
                .sum();
    }

    public int obtenerPuntosVencidos(Cliente cliente) {
        validarCliente(cliente);
        actualizarEstados(cliente);
        return cliente.getPuntosAcumulados().stream()
                .filter(p -> "VENCIDO".equals(p.getNombreEstado()))
                .mapToInt(PuntoCinemaUQ::getCantidad)
                .sum();
    }

    public double calcularValorRedencion(int puntos) {
        if (puntos < 0) {
            throw new PuntosException("Los puntos no pueden ser negativos");
        }
        return (puntos / (double) PUNTOS_BASE_REDENCION) * VALOR_BASE_REDENCION;
    }

    public Optional<LocalDate> obtenerProximoVencimiento(Cliente cliente) {
        validarCliente(cliente);
        actualizarEstados(cliente);
        return cliente.getPuntosAcumulados().stream()
                .filter(PuntoCinemaUQ::esValido)
                .map(PuntoCinemaUQ::getFechaVencimiento)
                .min(LocalDate::compareTo);
    }

    public void actualizarEstados(Cliente cliente) {
        validarCliente(cliente);
        cliente.getPuntosAcumulados().forEach(PuntoCinemaUQ::actualizarEstado);
    }

    private void validarCliente(Cliente cliente) {
        if (cliente == null) {
            throw new PuntosException("El cliente es obligatorio para gestionar puntos");
        }
    }
}
