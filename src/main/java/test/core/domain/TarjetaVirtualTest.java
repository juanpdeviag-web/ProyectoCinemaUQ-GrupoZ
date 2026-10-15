package test.core.domain;

import co.edu.uq.cinemauq.core.domain.TarjetaVirtual;
import co.edu.uq.cinemauq.core.domain.TipoTransaccion;
import co.edu.uq.cinemauq.core.domain.Transaccion;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

class TarjetaVirtualTest {

    @Test
    void testConstructorInicializaSaldoEnCero() {
        TarjetaVirtual tarjeta = new TarjetaVirtual();
        assertEquals(0.0, tarjeta.getSaldo());
    }

    @Test
    void testConstructorInicializaTarjetaActiva() {
        TarjetaVirtual tarjeta = new TarjetaVirtual();
        assertTrue(tarjeta.isActiva());
    }

    @Test
    void testConstructorGeneraNumeroTarjeta() {
        TarjetaVirtual tarjeta = new TarjetaVirtual();
        assertNotNull(tarjeta.getNumeroTarjeta());
        assertTrue(tarjeta.getNumeroTarjeta().startsWith("TV-"));
    }

    @Test
    void testConstructorInicializaFechaCreacion() {
        TarjetaVirtual tarjeta = new TarjetaVirtual();
        assertNotNull(tarjeta.getFechaCreacion());
        assertTrue(tarjeta.getFechaCreacion().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void testRecargaIncrementaSaldo() {
        TarjetaVirtual tarjeta = new TarjetaVirtual();
        tarjeta.recargar(100.0);
        assertEquals(100.0, tarjeta.getSaldo());
    }

    @Test
    void testRecargaMultiple() {
        TarjetaVirtual tarjeta = new TarjetaVirtual();
        tarjeta.recargar(50.0);
        tarjeta.recargar(30.0);
        assertEquals(80.0, tarjeta.getSaldo());
    }

    @Test
    void testRecargaConMontoNegativoLanzaExcepcion() {
        TarjetaVirtual tarjeta = new TarjetaVirtual();
        assertThrows(IllegalArgumentException.class, () -> tarjeta.recargar(-10.0));
    }

    @Test
    void testRecargaConMontoCeroLanzaExcepcion() {
        TarjetaVirtual tarjeta = new TarjetaVirtual();
        assertThrows(IllegalArgumentException.class, () -> tarjeta.recargar(0.0));
    }

    @Test
    void testDeducirReduceSaldo() {
        TarjetaVirtual tarjeta = new TarjetaVirtual();
        tarjeta.recargar(100.0);
        tarjeta.deducir(30.0);
        assertEquals(70.0, tarjeta.getSaldo());
    }

    @Test
    void testDeducirConSaldoInsuficienteLanzaExcepcion() {
        TarjetaVirtual tarjeta = new TarjetaVirtual();
        tarjeta.recargar(10.0);
        assertThrows(IllegalStateException.class, () -> tarjeta.deducir(20.0));
    }

    @Test
    void testDeducirConMontoNegativoLanzaExcepcion() {
        TarjetaVirtual tarjeta = new TarjetaVirtual();
        tarjeta.recargar(100.0);
        assertThrows(IllegalArgumentException.class, () -> tarjeta.deducir(-10.0));
    }

    @Test
    void testValidarSaldoSuficiente() {
        TarjetaVirtual tarjeta = new TarjetaVirtual();
        tarjeta.recargar(100.0);
        assertTrue(tarjeta.validarSaldoSuficiente(50.0));
        assertFalse(tarjeta.validarSaldoSuficiente(150.0));
    }

    @Test
    void testValidarSaldoSuficienteConMontoCero() {
        TarjetaVirtual tarjeta = new TarjetaVirtual();
        tarjeta.recargar(100.0);
        assertFalse(tarjeta.validarSaldoSuficiente(0.0));
    }

    @Test
    void testObtenerHistorialRetornaListaInmutable() {
        TarjetaVirtual tarjeta = new TarjetaVirtual();
        tarjeta.recargar(100.0);
        List<Transaccion> historial = tarjeta.obtenerHistorial();
        assertThrows(UnsupportedOperationException.class, () -> historial.add(new Transaccion(
            "test", TipoTransaccion.RECARGA, 10.0, 0.0, 10.0, LocalDateTime.now(), "test"
        )));
    }

    @Test
    void testObtenerHistorialVacioInicialmente() {
        TarjetaVirtual tarjeta = new TarjetaVirtual();
        assertTrue(tarjeta.obtenerHistorial().isEmpty());
    }

    @Test
    void testObtenerHistorialDespuesDeRecarga() {
        TarjetaVirtual tarjeta = new TarjetaVirtual();
        tarjeta.recargar(100.0);
        assertEquals(1, tarjeta.obtenerHistorial().size());
    }

    @Test
    void testObtenerUltimaTransaccion() {
        TarjetaVirtual tarjeta = new TarjetaVirtual();
        tarjeta.recargar(100.0);
        tarjeta.deducir(30.0);
        Transaccion ultima = tarjeta.obtenerUltimaTransaccion();
        assertNotNull(ultima);
        assertEquals(TipoTransaccion.COMPRA, ultima.tipo());
    }

    @Test
    void testObtenerUltimaTransaccionConHistorialVacio() {
        TarjetaVirtual tarjeta = new TarjetaVirtual();
        assertNull(tarjeta.obtenerUltimaTransaccion());
    }

    @Test
    void testRegistrarTransaccion() {
        TarjetaVirtual tarjeta = new TarjetaVirtual();
        Transaccion transaccion = new Transaccion(
            "test-id", TipoTransaccion.RECARGA, 100.0, 0.0, 100.0, LocalDateTime.now(), "Test"
        );
        tarjeta.registrarTransaccion(transaccion);
        assertEquals(1, tarjeta.obtenerHistorial().size());
    }

    @Test
    void testDesactivarTarjeta() {
        TarjetaVirtual tarjeta = new TarjetaVirtual();
        tarjeta.desactivar();
        assertFalse(tarjeta.isActiva());
    }

    @Test
    void testActivarTarjeta() {
        TarjetaVirtual tarjeta = new TarjetaVirtual();
        tarjeta.desactivar();
        tarjeta.activar();
        assertTrue(tarjeta.isActiva());
    }

    @Test
    void testObtenerResumenTransaccionesVacio() {
        TarjetaVirtual tarjeta = new TarjetaVirtual();
        String resumen = tarjeta.obtenerResumenTransacciones();
        assertTrue(resumen.contains("No hay transacciones"));
    }

    @Test
    void testObtenerResumenTransaccionesConDatos() {
        TarjetaVirtual tarjeta = new TarjetaVirtual();
        tarjeta.recargar(100.0);
        tarjeta.deducir(30.0);
        String resumen = tarjeta.obtenerResumenTransacciones();
        assertTrue(resumen.contains("Total transacciones: 2"));
        assertTrue(resumen.contains("Total recargas: $100.0"));
        assertTrue(resumen.contains("Total compras: $30.0"));
    }

    @Test
    void testFechaUltimaActividadSeActualiza() throws InterruptedException {
        TarjetaVirtual tarjeta = new TarjetaVirtual();
        LocalDateTime fechaInicial = tarjeta.getFechaUltimaActividad();
        Thread.sleep(10);
        tarjeta.recargar(100.0);
        assertTrue(tarjeta.getFechaUltimaActividad().isAfter(fechaInicial));
    }
}
