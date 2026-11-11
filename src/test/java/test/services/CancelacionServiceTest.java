package test.services;

import co.edu.uq.cinemauq.core.domain.*;
import co.edu.uq.cinemauq.core.exceptions.CompraException;
import co.edu.uq.cinemauq.core.exceptions.ReembolsoDuplicadoException;
import co.edu.uq.cinemauq.core.managers.AsientosManager;
import co.edu.uq.cinemauq.core.managers.ReservasManager;
import co.edu.uq.cinemauq.services.*;
import co.edu.uq.cinemauq.ui.models.SesionCompra;
import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CancelacionServiceTest {
    private static final LocalDateTime AHORA = LocalDateTime.of(2030, 1, 1, 12, 0);
    private final Clock reloj = Clock.fixed(AHORA.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
    private final ReservasManager registro = new ReservasManager();
    private final PagoCompraService pagos = new PagoCompraService(new PuntosService(), registro);
    private final CancelacionService cancelaciones = new CancelacionService(registro, reloj);

    private Cliente cliente() {
        Cliente cliente = new Cliente("C1", "Ana", "ana@correo.com");
        cliente.getTarjeta().recargar(200000);
        return cliente;
    }

    private SesionCompra sesion(Duration antesDeFuncion) {
        SesionCompra sesion = new SesionCompra();
        Pelicula pelicula = new Pelicula("Película", "Drama", 100);
        sesion.setPeliculaSeleccionada(pelicula);
        sesion.setFuncionSeleccionada(new Funcion(pelicula, new Sala("1", 5, 5), AHORA.plus(antesDeFuncion)));
        sesion.agregarAsiento("A1");
        return sesion;
    }

    private String pagar(Cliente cliente, Duration antes) {
        return pagos.pagar(cliente, sesion(antes)).idCompra();
    }

    @Test void casoAPagoRegistraCompraYElMismoIdEnTodasLasReservas() {
        Cliente cliente = cliente();
        SesionCompra sesion = sesion(Duration.ofHours(48));
        sesion.agregarAsiento("A2");
        String idSesion = sesion.getIdCompra();
        var comprobante = pagos.pagar(cliente, sesion);
        CompraConfirmada compra = cancelaciones.buscarCompra(comprobante.idCompra());
        assertEquals(idSesion, compra.compra().id());
        assertEquals(idSesion, comprobante.idCompra());
        assertEquals(2, compra.reservas().size());
        compra.reservas().forEach(r -> assertEquals(idSesion, r.getCompraId()));
        assertSame(cliente, compra.cliente());
        assertSame(cliente.getTarjeta(), compra.tarjetaPago());
        assertEquals(comprobante.cobrado(), compra.importePagado());
        assertEquals(idSesion, new CancelacionService(registro, reloj).listarComprasPorCliente(cliente.getId()).getFirst().compra().id());
    }

    @Test void casoBMasDe24HorasDevuelve100PorCiento() {
        Cliente cliente = cliente();
        String id = pagar(cliente, Duration.ofHours(48));
        Reembolso reembolso = cancelaciones.solicitarCancelacionPorCliente(id, "No puedo asistir", cliente.getId());
        assertEquals(1.0, reembolso.porcentajeAplicado());
        assertEquals(60000, reembolso.montoReembolsado());
        assertEquals(200000, cliente.getTarjeta().getSaldo());
        assertTrue(registro.buscarCompra(id).compra().estaCancelada());
    }

    @Test void casoCEntre2Y24HorasDevuelve80PorCiento() {
        Cliente cliente = cliente();
        String id = pagar(cliente, Duration.ofHours(5));
        Reembolso reembolso = cancelaciones.solicitarCancelacionPorCliente(id, "Cambio de planes", cliente.getId());
        assertEquals(0.8, reembolso.porcentajeAplicado());
        assertEquals(48000, reembolso.montoReembolsado());
        assertEquals(188000, cliente.getTarjeta().getSaldo());
    }

    @Test void casoDMenosDe2HorasRechazaSinModificarCompraSaldoOMovimientos() {
        Cliente cliente = cliente();
        String id = pagar(cliente, Duration.ofMinutes(119));
        CompraConfirmada original = registro.buscarCompra(id);
        List<Transaccion> historial = List.copyOf(cliente.getTarjeta().obtenerHistorial());
        assertThrows(CompraException.class, () -> cancelaciones.solicitarCancelacionPorCliente(id, "Motivo", cliente.getId()));
        assertSame(original, registro.buscarCompra(id));
        assertEquals(140000, cliente.getTarjeta().getSaldo());
        assertEquals(historial, cliente.getTarjeta().obtenerHistorial());
        assertTrue(cancelaciones.getHistorialReembolsos().isEmpty());
        assertTrue(cancelaciones.getSolicitudesCancelacion().isEmpty());
        assertTrue(AsientosManager.getInstance().isAsientoOcupado(original.compra().funcion().getId(), "A1"));
    }

    @Test void casosEIJReabrirServicioConservaEstadoEImpideSegundoAbono() {
        Cliente cliente = cliente();
        String id = pagar(cliente, Duration.ofHours(5));
        cancelaciones.solicitarCancelacionPorCliente(id, "Motivo", cliente.getId());
        CancelacionService otraPantalla = new CancelacionService(registro, reloj);
        assertTrue(otraPantalla.buscarCompra(id).compra().estaCancelada());
        assertNotNull(otraPantalla.buscarCompra(id).reembolso());
        assertThrows(ReembolsoDuplicadoException.class, () -> otraPantalla.solicitarCancelacionPorCliente(id, "Otro intento", cliente.getId()));
        assertEquals(188000, cliente.getTarjeta().getSaldo());
        assertEquals(1, otraPantalla.getHistorialReembolsos().size());
        assertEquals(1, cliente.getTarjeta().obtenerHistorial().stream().filter(t -> t.tipo() == TipoTransaccion.REEMBOLSO).count());
    }

    @Test void casoFIdInexistenteProduceErrorControlado() {
        assertThrows(CompraException.class, () -> cancelaciones.solicitarCancelacionPorCliente("inexistente", "Motivo", "C1"));
        assertTrue(registro.listarCompras().isEmpty());
    }

    @Test void casoGConPuntosReembolsa59990Por80PorCiento() {
        Cliente cliente = cliente();
        cliente.agregarPuntos(500);
        SesionCompra sesion = sesion(Duration.ofHours(5));
        pagos.prepararPuntos(cliente, sesion, 200);
        var comprobante = pagos.pagar(cliente, sesion);
        assertEquals(new BigDecimal("59990.00"), comprobante.cobrado());
        int puntosAntes = cliente.getPuntos();
        var lotesAntes = List.copyOf(cliente.getPuntosAcumulados());
        Reembolso reembolso = cancelaciones.solicitarCancelacionPorCliente(comprobante.idCompra(), "Motivo", cliente.getId());
        assertEquals(47992, reembolso.montoReembolsado());
        assertEquals(188002, cliente.getTarjeta().getSaldo());
        assertEquals(puntosAntes, cliente.getPuntos());
        assertEquals(lotesAntes, cliente.getPuntosAcumulados());
    }

    @Test void promocionYPuntosNoSeReembolsanComoSiHubiesenSidoDineroCobrado() {
        Cliente cliente = cliente();
        cliente.agregarPuntos(500);
        SesionCompra sesion = sesion(Duration.ofHours(5));
        sesion.configurarPromociones(true, false);
        pagos.prepararPuntos(cliente, sesion, 200);
        var comprobante = pagos.pagar(cliente, sesion);
        assertEquals(new BigDecimal("54990.00"), comprobante.cobrado());
        assertEquals(43992, cancelaciones.solicitarCancelacionPorCliente(comprobante.idCompra(), "Motivo", cliente.getId()).montoReembolsado());
    }

    @Test void casoHAbonaALaMismaTarjetaOriginalYRegistraElMovimiento() {
        Cliente cliente = cliente();
        TarjetaVirtual original = cliente.getTarjeta();
        String id = pagar(cliente, Duration.ofHours(5));
        TarjetaVirtual nueva = new TarjetaVirtual();
        nueva.recargar(100);
        cliente.setTarjeta(nueva);
        cancelaciones.solicitarCancelacionPorCliente(id, "Motivo", cliente.getId());
        assertEquals(188000, original.getSaldo());
        assertEquals(100, nueva.getSaldo());
        Transaccion movimiento = original.obtenerUltimaTransaccion();
        assertEquals(TipoTransaccion.REEMBOLSO, movimiento.tipo());
        assertEquals(48000, movimiento.monto());
        assertTrue(movimiento.descripcion().contains(id));
        assertEquals(140000, movimiento.saldoAnterior());
        assertEquals(188000, movimiento.saldoPosterior());
    }

    @Test void cinePuedeCancelarCon100PorCientoInclusoFueraDelPlazoDelCliente() {
        Cliente cliente = cliente();
        String id = pagar(cliente, Duration.ofMinutes(30));
        Reembolso reembolso = cancelaciones.solicitarCancelacionPorCine(id, "Función cancelada por el cine");
        assertEquals(1.0, reembolso.porcentajeAplicado());
        assertEquals(60000, reembolso.montoReembolsado());
    }

    @Test void limitesExactosDe2Y24HorasPertenecenAl80PorCiento() {
        Cliente cliente = cliente();
        String dos = pagar(cliente, Duration.ofHours(2));
        String veinticuatro = pagar(cliente, Duration.ofHours(24));
        String masDe24 = pagar(cliente, Duration.ofHours(24).plusSeconds(1));
        assertEquals(0.8, cancelaciones.porcentajeReembolso(dos));
        assertEquals(0.8, cancelaciones.porcentajeReembolso(veinticuatro));
        assertEquals(1.0, cancelaciones.porcentajeReembolso(masDe24));
    }

    @Test void clienteAjenoNoPuedeCancelarOtraCompra() {
        Cliente cliente = cliente();
        String id = pagar(cliente, Duration.ofHours(48));
        assertThrows(CompraException.class, () -> cancelaciones.solicitarCancelacionPorCliente(id, "Motivo", "OTRO"));
        assertEquals(140000, cliente.getTarjeta().getSaldo());
        assertFalse(registro.buscarCompra(id).compra().estaCancelada());
    }

    @Test void motivoInvalidoNoModificaNada() {
        Cliente cliente = cliente();
        String id = pagar(cliente, Duration.ofHours(48));
        assertThrows(CompraException.class, () -> cancelaciones.solicitarCancelacionPorCliente(id, " ", cliente.getId()));
        assertEquals(140000, cliente.getTarjeta().getSaldo());
        assertFalse(registro.buscarCompra(id).compra().estaCancelada());
    }

    @Test void liberarAsientosNoAfectaOtraCompraDeLaMismaFuncion() {
        Cliente cliente = cliente();
        SesionCompra primera = sesion(Duration.ofHours(48));
        String idPrimera = pagos.pagar(cliente, primera).idCompra();
        SesionCompra segunda = new SesionCompra();
        segunda.setPeliculaSeleccionada(primera.getPeliculaSeleccionada());
        segunda.setFuncionSeleccionada(primera.getFuncionSeleccionada());
        segunda.agregarAsiento("B1");
        String idSegunda = pagos.pagar(cliente, segunda).idCompra();
        assertNotEquals(idPrimera, idSegunda);
        assertEquals(2, registro.listarComprasPorUsuario(cliente.getId()).size());
        assertEquals(List.of("A1"), registro.buscarCompra(idPrimera).compra().asientos());
        assertEquals(List.of("B1"), registro.buscarCompra(idSegunda).compra().asientos());
        cancelaciones.solicitarCancelacionPorCliente(idPrimera, "Motivo", cliente.getId());
        String funcion = primera.getFuncionSeleccionada().getId();
        assertFalse(AsientosManager.getInstance().isAsientoOcupado(funcion, "A1"));
        assertTrue(AsientosManager.getInstance().isAsientoOcupado(funcion, "B1"));
        assertTrue(registro.buscarCompra(idSegunda).compra().esActiva());
        assertEquals(idPrimera, registro.buscarCompra(idPrimera).reembolso().idCompra());
    }

    @Test void pagoFallidoNoRegistraCompraNiReservas() {
        Cliente cliente = new Cliente("C", "Ana", "ana@correo.com");
        SesionCompra sesion = sesion(Duration.ofHours(48));
        assertThrows(RuntimeException.class, () -> pagos.pagar(cliente, sesion));
        assertNull(registro.buscarCompra(sesion.getIdCompra()));
        assertTrue(registro.getTodasLasReservas().isEmpty());
        assertFalse(AsientosManager.getInstance().isAsientoOcupado(sesion.getFuncionSeleccionada().getId(), "A1"));
    }

    @Test void reintentoDePagoNoDuplicaCompraNiReservas() {
        Cliente cliente = cliente();
        SesionCompra sesion = sesion(Duration.ofHours(48));
        var comprobante = pagos.pagar(cliente, sesion);
        sesion.limpiar();
        assertSame(comprobante, new PagoCompraService(new PuntosService(), registro).pagar(cliente, sesion));
        assertEquals(1, registro.listarCompras().size());
        assertEquals(1, registro.getTodasLasReservas().size());
    }

    @Test void falloTrasAbonarYRegistrarMovimientoRevierteSaldoHistorialYCompra() {
        boolean[] fallar = {true};
        Cliente cliente = new Cliente("C1", "Ana", "ana@correo.com");
        TarjetaVirtual tarjeta = new TarjetaVirtual() {
            @Override public void registrarTransaccion(Transaccion movimiento) {
                super.registrarTransaccion(movimiento);
                if (fallar[0] && movimiento.tipo() == TipoTransaccion.REEMBOLSO) {
                    throw new IllegalStateException("Fallo después de acreditar y registrar");
                }
            }
        };
        cliente.setTarjeta(tarjeta);
        tarjeta.recargar(200000);
        String id = pagar(cliente, Duration.ofHours(5));
        CompraConfirmada original = registro.buscarCompra(id);
        var historial = List.copyOf(tarjeta.obtenerHistorial());
        var actividad = tarjeta.getFechaUltimaActividad();
        assertThrows(IllegalStateException.class, () -> cancelaciones.solicitarCancelacionPorCliente(id, "Motivo", cliente.getId()));
        assertEquals(140000, tarjeta.getSaldo());
        assertEquals(historial, tarjeta.obtenerHistorial());
        assertEquals(actividad, tarjeta.getFechaUltimaActividad());
        assertSame(original, registro.buscarCompra(id));
        assertTrue(AsientosManager.getInstance().isAsientoOcupado(original.compra().funcion().getId(), "A1"));
        fallar[0] = false;
        cancelaciones.solicitarCancelacionPorCliente(id, "Reintento", cliente.getId());
        assertEquals(188000, tarjeta.getSaldo());
    }

    @Test void falloTrasCambiarEstadoYAsientosRevierteTodaLaCancelacion() {
        ReservasManager defectuoso = new ReservasManager() {
            @Override public synchronized void actualizarCompra(CompraConfirmada compra) {
                super.actualizarCompra(compra);
                throw new IllegalStateException("No se pudo finalizar el registro");
            }
        };
        Cliente cliente = cliente();
        SesionCompra sesion = sesion(Duration.ofHours(5));
        String id = new PagoCompraService(new PuntosService(), defectuoso).pagar(cliente, sesion).idCompra();
        var historial = List.copyOf(cliente.getTarjeta().obtenerHistorial());
        CancelacionService servicio = new CancelacionService(defectuoso, reloj);
        assertThrows(IllegalStateException.class, () -> servicio.solicitarCancelacionPorCliente(id, "Motivo", cliente.getId()));
        assertEquals(140000, cliente.getTarjeta().getSaldo());
        assertEquals(historial, cliente.getTarjeta().obtenerHistorial());
        assertTrue(defectuoso.buscarCompra(id).compra().esActiva());
        assertTrue(servicio.getHistorialReembolsos().isEmpty());
        assertTrue(AsientosManager.getInstance().isAsientoOcupado(sesion.getFuncionSeleccionada().getId(), "A1"));
    }

    @Test void falloDeRegistroAlPagarRevierteCobroPuntosReservasYAsientos() {
        ReservasManager defectuoso = new ReservasManager() {
            @Override public synchronized void registrarCompra(CompraConfirmada compra) {
                super.registrarCompra(compra);
                throw new IllegalStateException("Fallo al registrar compra pagada");
            }
        };
        Cliente cliente = cliente();
        SesionCompra sesion = sesion(Duration.ofHours(48));
        assertThrows(IllegalStateException.class, () -> new PagoCompraService(new PuntosService(), defectuoso).pagar(cliente, sesion));
        assertEquals(200000, cliente.getTarjeta().getSaldo());
        assertEquals(0, cliente.getPuntos());
        assertNull(sesion.getPagoConfirmado());
        assertTrue(defectuoso.listarCompras().isEmpty());
        assertTrue(defectuoso.getTodasLasReservas().isEmpty());
        assertFalse(AsientosManager.getInstance().isAsientoOcupado(sesion.getFuncionSeleccionada().getId(), "A1"));
    }

    @Test void dosSolicitudesSimultaneasSoloAbonanUnaVez() throws Exception {
        Cliente cliente = cliente();
        String id = pagar(cliente, Duration.ofHours(5));
        try (var executor = java.util.concurrent.Executors.newFixedThreadPool(2)) {
            var tareas = List.<java.util.concurrent.Callable<Boolean>>of(
                    () -> { try { cancelaciones.solicitarCancelacionPorCliente(id, "Uno", cliente.getId()); return true; }
                            catch (ReembolsoDuplicadoException e) { return false; } },
                    () -> { try { new CancelacionService(registro, reloj).solicitarCancelacionPorCliente(id, "Dos", cliente.getId()); return true; }
                            catch (ReembolsoDuplicadoException e) { return false; } });
            int exitosas = 0;
            for (var resultado : executor.invokeAll(tareas)) if (resultado.get()) exitosas++;
            assertEquals(1, exitosas);
        }
        assertEquals(188000, cliente.getTarjeta().getSaldo());
        assertEquals(1, cancelaciones.getHistorialReembolsos().size());
    }

    @Test void compraCubiertaConPuntosNoInventaUnAbonoMonetario() {
        Cliente cliente = new Cliente("C1", "Ana", "ana@correo.com");
        cliente.agregarPuntos(100);
        SesionCompra sesion = sesion(Duration.ofHours(48));
        sesion.setPrecioEntrada(1);
        pagos.prepararPuntos(cliente, sesion, 20);
        String id = pagos.pagar(cliente, sesion).idCompra();
        assertEquals(0, cancelaciones.solicitarCancelacionPorCliente(id, "Motivo", cliente.getId()).montoReembolsado());
        assertEquals(0, cliente.getTarjeta().getSaldo());
        assertEquals(80, cliente.getPuntos());
        assertTrue(cliente.getTarjeta().obtenerHistorial().isEmpty());
        assertTrue(registro.buscarCompra(id).compra().estaCancelada());
    }
}
