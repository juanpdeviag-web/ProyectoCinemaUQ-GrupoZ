package test.services;

import co.edu.uq.cinemauq.core.domain.Cliente;
import co.edu.uq.cinemauq.core.domain.PuntoCinemaUQ;
import co.edu.uq.cinemauq.core.exceptions.UsuarioException;
import co.edu.uq.cinemauq.patterns.state.PuntoDisponible;
import co.edu.uq.cinemauq.patterns.state.PuntoRedimido;
import co.edu.uq.cinemauq.patterns.state.PuntoVencido;
import co.edu.uq.cinemauq.services.PuntosService;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PuntosServiceTest {
    private final PuntosService service = new PuntosService();

    private Cliente cliente() {
        return new Cliente("C001", "Ana", "ana@correo.com");
    }

    @Test
    void acumulaSoloCentenasCompletasEnLotesIndependientes() {
        Cliente cliente = cliente();
        assertEquals(0, service.acumularPuntos(cliente, new BigDecimal("99.99")));
        assertTrue(cliente.getPuntosAcumulados().isEmpty());
        assertEquals(2, service.acumularPuntos(cliente, new BigDecimal("250.99")));
        assertEquals(1, service.acumularPuntos(cliente, new BigDecimal("100")));
        assertEquals(2, cliente.getPuntosAcumulados().size());
        assertEquals(3, cliente.getPuntos());
        PuntoCinemaUQ lote = cliente.getPuntosAcumulados().getFirst();
        assertEquals(lote.getFechaObtencion().plusYears(1), lote.getFechaVencimiento());
        assertThrows(UnsupportedOperationException.class, () -> cliente.getPuntosAcumulados().clear());
    }

    @Test
    void permiteRedencionParcialYBloqueaReutilizacionTrasRedencionTotal() {
        PuntoCinemaUQ lote = new PuntoCinemaUQ(100, LocalDate.now());
        lote.redimir(40);
        assertEquals(100, lote.getCantidad());
        assertEquals(60, lote.getCantidadRestante());
        assertInstanceOf(PuntoDisponible.class, lote.getEstado());
        lote.redimir(60);
        assertEquals(0, lote.getCantidadRestante());
        assertInstanceOf(PuntoRedimido.class, lote.getEstado());
        assertThrows(UsuarioException.class, () -> lote.redimir(1));
    }

    @Test
    void venceSoloDespuesDeLaFechaDeVencimiento() {
        LocalDate hoy = LocalDate.now();
        PuntoCinemaUQ vigente = new PuntoCinemaUQ(10, hoy.minusYears(1));
        // Esta igualdad también verifica que la fecha de hoy pertenece al periodo vigente.
        assertEquals(hoy, vigente.getFechaVencimiento());
        assertTrue(vigente.estaDisponible());
        vigente.redimir(1);
        PuntoCinemaUQ vencido = new PuntoCinemaUQ(10, hoy.minusYears(1).minusDays(1));
        assertInstanceOf(PuntoVencido.class, vencido.getEstado());
        assertFalse(vencido.estaDisponible());
        assertThrows(UsuarioException.class, () -> vencido.redimir(1));
        assertEquals(10, vencido.getCantidadRestante());
    }

    @Test
    void consumePrimeroElLoteConVencimientoMasProximo() {
        Cliente cliente = cliente();
        PuntoCinemaUQ reciente = new PuntoCinemaUQ(100, LocalDate.now());
        PuntoCinemaUQ antiguo = new PuntoCinemaUQ(60, LocalDate.now().minusMonths(6));
        cliente.agregarPuntos(reciente);
        cliente.agregarPuntos(antiguo);
        assertEquals(new BigDecimal("5.00"), service.redimirPuntos(cliente, 100));
        assertInstanceOf(PuntoRedimido.class, antiguo.getEstado());
        assertEquals(60, reciente.getCantidadRestante());
        assertInstanceOf(PuntoDisponible.class, reciente.getEstado());
    }

    @Test
    void saldoInsuficienteNoConsumeNingunLoteYExcluyeVencidos() {
        Cliente cliente = cliente();
        PuntoCinemaUQ disponible = new PuntoCinemaUQ(20, LocalDate.now());
        PuntoCinemaUQ vencido = new PuntoCinemaUQ(100, LocalDate.now().minusYears(2));
        cliente.agregarPuntos(disponible);
        cliente.agregarPuntos(vencido);
        assertFalse(service.verificarDisponibilidad(cliente, 21));
        assertThrows(UsuarioException.class, () -> service.redimirPuntos(cliente, 21));
        assertEquals(20, disponible.getCantidadRestante());
        assertEquals(100, vencido.getCantidadRestante());
        assertEquals(20, cliente.getPuntos());
    }

    @Test
    void validaCantidadesSinModificarElSaldo() {
        Cliente cliente = cliente();
        cliente.agregarPuntos(10);
        cliente.agregarPuntos(0);
        assertThrows(UsuarioException.class, () -> cliente.agregarPuntos(-1));
        assertThrows(UsuarioException.class, () -> service.acumularPuntos(cliente, new BigDecimal("-1")));
        assertThrows(UsuarioException.class, () -> service.verificarDisponibilidad(cliente, -1));
        assertThrows(UsuarioException.class, () -> service.redimirPuntos(cliente, 0));
        PuntoCinemaUQ lote = cliente.getPuntosAcumulados().getFirst();
        assertThrows(UsuarioException.class, () -> lote.redimir(11));
        assertThrows(UsuarioException.class, () -> lote.redimir(-1));
        assertEquals(10, cliente.getPuntos());
        assertEquals(new BigDecimal("0.05"), service.redimirPuntos(cliente, 1));
    }
}
