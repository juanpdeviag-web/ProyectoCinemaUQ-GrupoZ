package test.services;

import co.edu.uq.cinemauq.core.domain.Cliente;
import co.edu.uq.cinemauq.core.domain.TarjetaVirtual;
import co.edu.uq.cinemauq.core.domain.TipoTransaccion;
import co.edu.uq.cinemauq.core.domain.Transaccion;
import co.edu.uq.cinemauq.core.repository.MemoryUserRepository;
import co.edu.uq.cinemauq.core.repository.UserRepository;
import co.edu.uq.cinemauq.core.ports.IPagoProcessor;
import co.edu.uq.cinemauq.services.CineServiceImpl;
import co.edu.uq.cinemauq.services.PagoProcessorImpl;
import co.edu.uq.cinemauq.services.TarjetaVirtualService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

class TarjetaVirtualServiceTest {
    private UserRepository repository;
    private IPagoProcessor pagoProcessor;
    private TarjetaVirtualService tarjetaVirtualService;
    private CineServiceImpl cineService;
    private Cliente cliente;

    @BeforeEach
    void setUp() {
        repository = new MemoryUserRepository();
        pagoProcessor = new PagoProcessorImpl();
        tarjetaVirtualService = new TarjetaVirtualService(repository, pagoProcessor);
        cineService = new CineServiceImpl(repository, pagoProcessor);
        cliente = new Cliente("cli001", "Juan Perez", "juan@test.com");
        cineService.registrarUsuario(cliente);
    }

    @Test
    void testRecargarTarjeta() {
        tarjetaVirtualService.recargarTarjeta("cli001", 100.0);
        assertEquals(100.0, tarjetaVirtualService.consultarSaldo("cli001"));
    }

    @Test
    void testRecargarTarjetaConMontoNegativoLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> tarjetaVirtualService.recargarTarjeta("cli001", -50.0));
    }

    @Test
    void testConsultarSaldo() {
        tarjetaVirtualService.recargarTarjeta("cli001", 150.0);
        double saldo = tarjetaVirtualService.consultarSaldo("cli001");
        assertEquals(150.0, saldo);
    }

    @Test
    void testConsultarSaldoInicialEnCero() {
        double saldo = tarjetaVirtualService.consultarSaldo("cli001");
        assertEquals(0.0, saldo);
    }

    @Test
    void testConsultarHistorial() {
        tarjetaVirtualService.recargarTarjeta("cli001", 100.0);
        tarjetaVirtualService.recargarTarjeta("cli001", 50.0);
        List<Transaccion> historial = tarjetaVirtualService.consultarHistorial("cli001");
        assertEquals(2, historial.size());
        assertEquals(TipoTransaccion.RECARGA, historial.get(0).tipo());
        assertEquals(TipoTransaccion.RECARGA, historial.get(1).tipo());
    }

    @Test
    void testConsultarHistorialVacio() {
        List<Transaccion> historial = tarjetaVirtualService.consultarHistorial("cli001");
        assertTrue(historial.isEmpty());
    }

    @Test
    void testGenerarReporteTransacciones() {
        tarjetaVirtualService.recargarTarjeta("cli001", 100.0);
        String reporte = tarjetaVirtualService.generarReporteTransacciones("cli001");
        assertTrue(reporte.contains("Total transacciones: 1"));
        assertTrue(reporte.contains("Total recargas: $100.0"));
    }

    @Test
    void testBloquearTarjeta() {
        tarjetaVirtualService.bloquearTarjeta("cli001");
        TarjetaVirtual tarjeta = cliente.getTarjetaVirtual();
        assertFalse(tarjeta.isActiva());
    }

    @Test
    void testDesbloquearTarjeta() {
        tarjetaVirtualService.bloquearTarjeta("cli001");
        tarjetaVirtualService.desbloquearTarjeta("cli001");
        TarjetaVirtual tarjeta = cliente.getTarjetaVirtual();
        assertTrue(tarjeta.isActiva());
    }

    @Test
    void testRecargarTarjetaClienteNoExistenteLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> tarjetaVirtualService.recargarTarjeta("noexiste", 100.0));
    }

    @Test
    void testConsultarSaldoClienteNoExistenteLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> tarjetaVirtualService.consultarSaldo("noexiste"));
    }

    @Test
    void testIntegracionRealizarCompra() {
        tarjetaVirtualService.recargarTarjeta("cli001", 100.0);
        boolean resultado = cineService.realizarCompra("cli001", 50.0);
        assertTrue(resultado);
        assertEquals(50.0, tarjetaVirtualService.consultarSaldo("cli001"));
    }

    @Test
    void testIntegracionRealizarCompraSaldoInsuficiente() {
        tarjetaVirtualService.recargarTarjeta("cli001", 30.0);
        boolean resultado = cineService.realizarCompra("cli001", 50.0);
        assertFalse(resultado);
        assertEquals(30.0, tarjetaVirtualService.consultarSaldo("cli001"));
    }

    @Test
    void testIntegracionReembolsarCompra() {
        tarjetaVirtualService.recargarTarjeta("cli001", 100.0);
        cineService.realizarCompra("cli001", 50.0);
        cineService.reembolsarCompra("cli001", 50.0);
        assertEquals(100.0, tarjetaVirtualService.consultarSaldo("cli001"));
    }

    @Test
    void testIntegracionReembolsarCompraParcial() {
        tarjetaVirtualService.recargarTarjeta("cli001", 100.0);
        cineService.realizarCompra("cli001", 50.0);
        cineService.reembolsarCompra("cli001", 30.0);
        assertEquals(80.0, tarjetaVirtualService.consultarSaldo("cli001"));
    }

    @Test
    void testIntegracionReembolsarCompraExcedeMonto() {
        tarjetaVirtualService.recargarTarjeta("cli001", 100.0);
        cineService.realizarCompra("cli001", 50.0);
        assertThrows(IllegalArgumentException.class, () -> cineService.reembolsarCompra("cli001", 60.0));
    }

    @Test
    void testIntegracionHistorialDespuesDeCompraYReembolso() {
        tarjetaVirtualService.recargarTarjeta("cli001", 100.0);
        cineService.realizarCompra("cli001", 50.0);
        cineService.reembolsarCompra("cli001", 50.0);
        List<Transaccion> historial = tarjetaVirtualService.consultarHistorial("cli001");
        assertEquals(3, historial.size());
        assertEquals(TipoTransaccion.RECARGA, historial.get(0).tipo());
        assertEquals(TipoTransaccion.COMPRA, historial.get(1).tipo());
        assertEquals(TipoTransaccion.REEMBOLSO, historial.get(2).tipo());
    }

    @Test
    void testIntegracionCompraConTarjetaDesactivada() {
        tarjetaVirtualService.recargarTarjeta("cli001", 100.0);
        tarjetaVirtualService.bloquearTarjeta("cli001");
        boolean resultado = cineService.realizarCompra("cli001", 50.0);
        assertFalse(resultado);
    }
}
