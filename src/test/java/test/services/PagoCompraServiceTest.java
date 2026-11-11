package test.services;

import co.edu.uq.cinemauq.core.domain.*;
import co.edu.uq.cinemauq.patterns.state.*;
import co.edu.uq.cinemauq.services.PagoCompraService;
import co.edu.uq.cinemauq.services.PuntosService;
import co.edu.uq.cinemauq.ui.models.SesionCompra;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PagoCompraServiceTest {
    private final PagoCompraService pagos = new PagoCompraService();

    private Cliente cliente(int puntos, double saldo) {
        Cliente cliente = new Cliente("C1", "Ana", "ana@correo.com");
        if (puntos > 0) cliente.agregarPuntos(puntos);
        if (saldo > 0) cliente.getTarjeta().recargar(saldo);
        return cliente;
    }

    private SesionCompra compra(double precio) {
        SesionCompra sesion = new SesionCompra();
        Pelicula pelicula = new Pelicula("Película", "Drama", 100);
        sesion.setPeliculaSeleccionada(pelicula);
        sesion.setFuncionSeleccionada(new Funcion(pelicula, new Sala("1", 5, 5), LocalDateTime.now().plusDays(1)));
        sesion.setPrecioEntrada(precio);
        sesion.agregarAsiento("A1");
        return sesion;
    }

    @Test void casoACompraSinPuntosAcumulaSoloCentenasCompletas() {
        Cliente cliente = cliente(0, 100000);
        var resultado = pagos.pagar(cliente, compra(60050));
        assertEquals(new BigDecimal("60050.00"), resultado.cobrado());
        assertEquals(39950, cliente.getTarjeta().getSaldo());
        assertEquals(600, resultado.puntosGenerados());
        PuntoCinemaUQ lote = cliente.getPuntosAcumulados().getFirst();
        assertEquals(600, lote.getCantidad());
        assertEquals(LocalDate.now(), lote.getFechaObtencion());
        assertEquals(LocalDate.now().plusYears(1), lote.getFechaVencimiento());
        assertInstanceOf(PuntoDisponible.class, lote.getEstado());
    }

    @Test void casoBPrepararNoConsumeYPagarConsume200YAcumula599() {
        Cliente cliente = cliente(500, 100000);
        PuntoCinemaUQ original = cliente.getPuntosAcumulados().getFirst();
        SesionCompra sesion = compra(60000);
        pagos.prepararPuntos(cliente, sesion, 200);
        assertEquals(500, original.getCantidadRestante());
        assertEquals(new BigDecimal("59990.00"), sesion.calcularTotalAPagar());
        var resultado = pagos.pagar(cliente, sesion);
        assertEquals(200, resultado.puntosRedimidos());
        assertEquals(599, resultado.puntosGenerados());
        assertEquals(40010, cliente.getTarjeta().getSaldo());
        assertEquals(300, original.getCantidadRestante());
        assertEquals(899, cliente.getPuntos());
        assertInstanceOf(PuntoDisponible.class, original.getEstado());
    }

    @Test void casoCRechazaPuntosInsuficientesSinPrepararNiModificarLotes() {
        Cliente cliente = cliente(100, 100000);
        SesionCompra sesion = compra(60000);
        assertThrows(RuntimeException.class, () -> pagos.prepararPuntos(cliente, sesion, 101));
        assertEquals(0, sesion.getPuntosAplicados());
        assertEquals(100, cliente.getPuntos());
    }

    @Test void casoDNoCuentaLotesVencidos() {
        Cliente cliente = cliente(0, 100000);
        PuntoCinemaUQ vencido = new PuntoCinemaUQ(500, LocalDate.now().minusYears(2));
        cliente.agregarPuntos(vencido);
        assertThrows(RuntimeException.class, () -> pagos.prepararPuntos(cliente, compra(60000), 1));
        assertEquals(500, vencido.getCantidadRestante());
        assertInstanceOf(PuntoVencido.class, vencido.getEstado());
    }

    @Test void casoESaldoInsuficienteConservaTodosLosPuntos() {
        Cliente cliente = cliente(300, 10);
        SesionCompra sesion = compra(60000);
        pagos.prepararPuntos(cliente, sesion, 300);
        int movimientos = cliente.getTarjeta().obtenerHistorial().size();
        assertThrows(RuntimeException.class, () -> pagos.pagar(cliente, sesion));
        assertEquals(300, cliente.getPuntos());
        assertEquals(10, cliente.getTarjeta().getSaldo());
        assertEquals(movimientos, cliente.getTarjeta().obtenerHistorial().size());
        assertNull(sesion.getPagoConfirmado());
    }

    @Test void tarjetaInactivaNoConsumePuntos() {
        Cliente cliente = cliente(300, 100000);
        SesionCompra sesion = compra(60000);
        pagos.prepararPuntos(cliente, sesion, 300);
        cliente.getTarjeta().desactivar();
        assertThrows(RuntimeException.class, () -> pagos.pagar(cliente, sesion));
        assertEquals(300, cliente.getPuntos());
        assertEquals(100000, cliente.getTarjeta().getSaldo());
    }

    @Test void casoGRedencionTotalMarcaLoteRedimido() {
        Cliente cliente = cliente(200, 100000);
        PuntoCinemaUQ lote = cliente.getPuntosAcumulados().getFirst();
        SesionCompra sesion = compra(60000);
        pagos.prepararPuntos(cliente, sesion, 200);
        pagos.pagar(cliente, sesion);
        assertEquals(0, lote.getCantidadRestante());
        assertInstanceOf(PuntoRedimido.class, lote.getEstado());
    }

    @Test void casoHUsaAntesElLoteQueVencePrimero() {
        Cliente cliente = cliente(100, 100000);
        PuntoCinemaUQ reciente = cliente.getPuntosAcumulados().getFirst();
        PuntoCinemaUQ antiguo = new PuntoCinemaUQ(60, LocalDate.now().minusMonths(6));
        cliente.agregarPuntos(antiguo);
        SesionCompra sesion = compra(60000);
        pagos.prepararPuntos(cliente, sesion, 100);
        pagos.pagar(cliente, sesion);
        assertInstanceOf(PuntoRedimido.class, antiguo.getEstado());
        assertEquals(60, reciente.getCantidadRestante());
    }

    @Test void aplicaPromocionExistenteAntesDePuntosYAcumulaSobreCobroReal() {
        Cliente cliente = cliente(500, 100000);
        SesionCompra sesion = compra(60000);
        sesion.configurarPromociones(true, false);
        pagos.prepararPuntos(cliente, sesion, 200);
        var resultado = pagos.pagar(cliente, sesion);
        assertEquals(new BigDecimal("54990.00"), resultado.cobrado());
        assertEquals(549, resultado.puntosGenerados());
    }

    @Test void conservaElOrdenDeLosDecoradoresAlCombinarPromociones() {
        Cliente cliente = cliente(500, 100000);
        SesionCompra sesion = compra(60000);
        sesion.configurarPromociones(true, true);
        assertEquals(new BigDecimal("49500.00"), sesion.calcularTotalConPromociones());
        pagos.prepararPuntos(cliente, sesion, 200);
        assertEquals(new BigDecimal("49490.00"), pagos.pagar(cliente, sesion).cobrado());
    }

    @Test void limitaRedencionYAdmiteCompraCubiertaPorPuntosSinCargoDeCero() {
        Cliente cliente = cliente(100, 0);
        SesionCompra sesion = compra(1);
        assertEquals(20, pagos.prepararPuntos(cliente, sesion, 100));
        var resultado = pagos.pagar(cliente, sesion);
        assertEquals(0, resultado.cobrado().signum());
        assertEquals(0, resultado.puntosGenerados());
        assertEquals(80, cliente.getPuntos());
        assertTrue(cliente.getTarjeta().obtenerHistorial().isEmpty());
    }

    @Test void noConsumePuntosDeMasCuandoElSaldoNoEsMultiploDeCincoCentavos() {
        Cliente cliente = cliente(100, 10);
        SesionCompra sesion = compra(1.03);
        assertEquals(20, pagos.prepararPuntos(cliente, sesion, 100));
        assertEquals(new BigDecimal("0.03"), pagos.pagar(cliente, sesion).cobrado());
    }

    @Test void revalidaDisponibilidadAlPagar() {
        Cliente cliente = cliente(300, 100000);
        SesionCompra sesion = compra(60000);
        pagos.prepararPuntos(cliente, sesion, 300);
        cliente.getPuntosAcumulados().getFirst().redimir(1);
        assertThrows(RuntimeException.class, () -> pagos.pagar(cliente, sesion));
        assertEquals(299, cliente.getPuntos());
        assertEquals(100000, cliente.getTarjeta().getSaldo());
    }

    @Test void falloDespuesDeRedimirRestauraElMismoLoteYEstado() {
        Cliente cliente = cliente(200, 100000);
        PuntoCinemaUQ lote = cliente.getPuntosAcumulados().getFirst();
        EstadoPunto inicial = lote.getEstado();
        PagoCompraService falla = new PagoCompraService(new PuntosService() {
            @Override public BigDecimal redimirPuntos(Cliente c, int cantidad) {
                super.redimirPuntos(c, cantidad);
                throw new IllegalStateException("Fallo después de redimir");
            }
        });
        SesionCompra sesion = compra(60000);
        falla.prepararPuntos(cliente, sesion, 200);
        assertThrows(IllegalStateException.class, () -> falla.pagar(cliente, sesion));
        assertSame(lote, cliente.getPuntosAcumulados().getFirst());
        assertSame(inicial, lote.getEstado());
        assertEquals(200, lote.getCantidadRestante());
        assertEquals(100000, cliente.getTarjeta().getSaldo());
    }

    @Test void falloDeCobroInclusoDespuesDeDeducirRestauraSaldoHistorialYLotes() {
        Cliente cliente = cliente(200, 0);
        TarjetaVirtual tarjeta = new TarjetaVirtual() {
            @Override public void deducir(double monto) {
                super.deducir(monto);
                throw new IllegalStateException("Fallo de cobro");
            }
        };
        cliente.setTarjeta(tarjeta);
        tarjeta.recargar(100000);
        var historial = java.util.List.copyOf(tarjeta.obtenerHistorial());
        var actividad = tarjeta.getFechaUltimaActividad();
        SesionCompra sesion = compra(60000);
        pagos.prepararPuntos(cliente, sesion, 200);
        assertThrows(IllegalStateException.class, () -> pagos.pagar(cliente, sesion));
        assertEquals(100000, tarjeta.getSaldo());
        assertEquals(historial, tarjeta.obtenerHistorial());
        assertEquals(actividad, tarjeta.getFechaUltimaActividad());
        assertEquals(200, cliente.getPuntos());
    }

    @Test void falloAlAcumularRevierteTambienElLoteNuevoYPermiteReintentar() {
        Cliente cliente = cliente(200, 100000);
        PagoCompraService falla = new PagoCompraService(new PuntosService() {
            @Override public int acumularPuntos(Cliente c, BigDecimal monto) {
                super.acumularPuntos(c, monto);
                throw new IllegalStateException("Fallo al acumular");
            }
        });
        SesionCompra sesion = compra(60000);
        falla.prepararPuntos(cliente, sesion, 200);
        assertThrows(IllegalStateException.class, () -> falla.pagar(cliente, sesion));
        assertEquals(1, cliente.getPuntosAcumulados().size());
        assertEquals(200, cliente.getPuntos());
        assertEquals(100000, cliente.getTarjeta().getSaldo());
        assertNull(sesion.getPagoConfirmado());
        assertEquals(599, pagos.pagar(cliente, sesion).puntosGenerados());
    }

    @Test void equivalenciaInconsistenteRevierteRedencion() {
        Cliente cliente = cliente(200, 100000);
        PagoCompraService falla = new PagoCompraService(new PuntosService() {
            @Override public BigDecimal redimirPuntos(Cliente c, int cantidad) {
                return super.redimirPuntos(c, cantidad).add(BigDecimal.ONE);
            }
        });
        SesionCompra sesion = compra(60000);
        falla.prepararPuntos(cliente, sesion, 200);
        assertThrows(RuntimeException.class, () -> falla.pagar(cliente, sesion));
        assertEquals(200, cliente.getPuntos());
        assertEquals(100000, cliente.getTarjeta().getSaldo());
    }

    @Test void reintentarInclusoTrasLimpiarNoCobraNiAcumulaDosVeces() {
        Cliente cliente = cliente(200, 100000);
        SesionCompra sesion = compra(60000);
        pagos.prepararPuntos(cliente, sesion, 200);
        var confirmado = pagos.pagar(cliente, sesion);
        sesion.limpiar();
        assertSame(confirmado, new PagoCompraService().pagar(cliente, sesion));
        assertEquals(2, cliente.getTarjeta().obtenerHistorial().size());
        assertEquals(2, cliente.getPuntosAcumulados().size());
        assertEquals(599, cliente.getPuntos());
    }

    @Test void otraCompraTieneIdentidadNuevaYGeneraUnSoloLoteAdicional() {
        Cliente cliente = cliente(0, 200000);
        SesionCompra sesion = compra(60000);
        String primero = pagos.pagar(cliente, sesion).idCompra();
        sesion.limpiar();
        SesionCompra nueva = compra(60000);
        sesion.setPeliculaSeleccionada(nueva.getPeliculaSeleccionada());
        sesion.setFuncionSeleccionada(nueva.getFuncionSeleccionada());
        sesion.agregarAsiento("A1");
        assertNotEquals(primero, pagos.pagar(cliente, sesion).idCompra());
        assertEquals(1200, cliente.getPuntos());
    }

    @Test void cambiarCompraOQuitarPreparacionNoConsumeLotes() {
        Cliente cliente = cliente(500, 100000);
        SesionCompra sesion = compra(60000);
        pagos.prepararPuntos(cliente, sesion, 200);
        sesion.quitarPuntos();
        assertEquals(0, sesion.getPuntosAplicados());
        pagos.prepararPuntos(cliente, sesion, 200);
        sesion.agregarAsiento("A2");
        assertEquals(0, sesion.getPuntosAplicados());
        assertEquals(500, cliente.getPuntos());
    }

    @Test void preparacionNoPuedeUsarsePorOtroCliente() {
        Cliente cliente = cliente(500, 100000);
        SesionCompra sesion = compra(60000);
        pagos.prepararPuntos(cliente, sesion, 200);
        Cliente otro = new Cliente("C2", "Luis", "luis@correo.com");
        otro.agregarPuntos(500);
        otro.getTarjeta().recargar(100000);
        assertThrows(RuntimeException.class, () -> pagos.pagar(otro, sesion));
        assertEquals(500, otro.getPuntos());
        assertEquals(100000, otro.getTarjeta().getSaldo());
    }

    @Test void solicitudesSimultaneasSoloConfirmanUnaVez() throws Exception {
        Cliente cliente = cliente(200, 100000);
        SesionCompra sesion = compra(60000);
        pagos.prepararPuntos(cliente, sesion, 200);
        try (var executor = java.util.concurrent.Executors.newFixedThreadPool(4)) {
            var tareas = java.util.stream.IntStream.range(0, 4)
                    .<java.util.concurrent.Callable<SesionCompra.ComprobantePago>>mapToObj(i ->
                            () -> new PagoCompraService().pagar(cliente, sesion)).toList();
            for (var resultado : executor.invokeAll(tareas)) {
                assertEquals(599, resultado.get().puntosGenerados());
            }
        }
        assertEquals(2, cliente.getPuntosAcumulados().size());
        assertEquals(2, cliente.getTarjeta().obtenerHistorial().size());
        assertEquals(40010, cliente.getTarjeta().getSaldo());
    }

    @Test void falloAntesDeMarcarConfirmacionTambienRevierteElPago() {
        Cliente cliente = cliente(200, 100000);
        SesionCompra base = compra(60000);
        SesionCompra sesion = new SesionCompra() {
            @Override public void confirmarPago(ComprobantePago comprobante) {
                throw new IllegalStateException("No se pudo confirmar");
            }
        };
        sesion.setPeliculaSeleccionada(base.getPeliculaSeleccionada());
        sesion.setFuncionSeleccionada(base.getFuncionSeleccionada());
        sesion.agregarAsiento("A1");
        pagos.prepararPuntos(cliente, sesion, 200);
        assertThrows(IllegalStateException.class, () -> pagos.pagar(cliente, sesion));
        assertEquals(200, cliente.getPuntos());
        assertEquals(1, cliente.getPuntosAcumulados().size());
        assertEquals(100000, cliente.getTarjeta().getSaldo());
        assertEquals(1, cliente.getTarjeta().obtenerHistorial().size());
        assertNull(sesion.getPagoConfirmado());
    }
}
