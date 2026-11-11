package test.services;

import co.edu.uq.cinemauq.core.domain.Cliente;
import co.edu.uq.cinemauq.core.domain.PuntoCinemaUQ;
import co.edu.uq.cinemauq.core.exceptions.PuntosInsuficientesException;
import co.edu.uq.cinemauq.services.PuntosService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PuntosServiceTest {
    private PuntosService service;
    private Cliente cliente;

    @BeforeEach
    void setUp() {
        service = new PuntosService();
        cliente = new Cliente("CLI-TEST", "Cliente Prueba", "test@cinemauq.com");
    }

    @Test
    void acumulaUnPuntoPorCadaCienDeCompra() {
        int generados = service.acumularPuntos(cliente, 12_350);

        assertEquals(123, generados);
        assertEquals(123, service.obtenerPuntosDisponibles(cliente));
        assertEquals(LocalDate.now().plusYears(1), cliente.getPuntosAcumulados().get(0).getFechaVencimiento());
    }

    @Test
    void unLoteVencidoCambiaDeEstadoYNoEstaDisponible() {
        PuntoCinemaUQ vencido = new PuntoCinemaUQ(300, LocalDate.now().minusYears(1).minusDays(1));
        cliente.agregarLotePuntos(vencido);

        assertFalse(vencido.esValido());
        assertEquals("VENCIDO", vencido.getNombreEstado());
        assertEquals(0, service.obtenerPuntosDisponibles(cliente));
        assertEquals(300, service.obtenerPuntosVencidos(cliente));
    }

    @Test
    void redimePrimeroElLoteMasProximoAVencer() {
        PuntoCinemaUQ vencePrimero = new PuntoCinemaUQ(
                100,
                LocalDate.now().minusMonths(10),
                LocalDate.now().plusMonths(2));
        PuntoCinemaUQ venceDespues = new PuntoCinemaUQ(
                100,
                LocalDate.now().minusMonths(3),
                LocalDate.now().plusMonths(9));

        cliente.agregarLotePuntos(venceDespues); // Se agrega en orden inverso a propósito
        cliente.agregarLotePuntos(vencePrimero);

        double valor = service.redimirPuntos(cliente, 150);

        assertEquals(0, vencePrimero.getCantidad());
        assertEquals("REDIMIDO", vencePrimero.getNombreEstado());
        assertEquals(50, venceDespues.getCantidad());
        assertEquals("DISPONIBLE", venceDespues.getNombreEstado());
        assertEquals(7.5, valor, 0.0001); // 100 pts = $5 => 150 pts = $7.50
    }

    @Test
    void noPermiteRedimirMasPuntosDeLosDisponibles() {
        cliente.agregarLotePuntos(new PuntoCinemaUQ(80, LocalDate.now()));

        assertThrows(PuntosInsuficientesException.class,
                () -> service.redimirPuntos(cliente, 100));
        assertEquals(80, service.obtenerPuntosDisponibles(cliente));
    }

    @Test
    void equivalenciaEsCienPuntosPorCinco() {
        assertEquals(5.0, service.calcularValorRedencion(100), 0.0001);
        assertEquals(12.5, service.calcularValorRedencion(250), 0.0001);
    }
}
