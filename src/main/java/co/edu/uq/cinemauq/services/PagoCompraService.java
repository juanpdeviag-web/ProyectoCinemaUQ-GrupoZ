package co.edu.uq.cinemauq.services;

import co.edu.uq.cinemauq.core.domain.Cliente;
import co.edu.uq.cinemauq.core.domain.TarjetaVirtual;
import co.edu.uq.cinemauq.core.domain.CompraConfirmada;
import co.edu.uq.cinemauq.core.domain.ReservaAsiento;
import co.edu.uq.cinemauq.core.domain.EstadoCompra;
import co.edu.uq.cinemauq.core.managers.ReservasManager;
import co.edu.uq.cinemauq.core.exceptions.CompraException;
import co.edu.uq.cinemauq.core.exceptions.SaldoInsuficienteException;
import co.edu.uq.cinemauq.core.exceptions.TarjetaInactivaException;
import co.edu.uq.cinemauq.ui.models.SesionCompra;
import java.math.BigDecimal;
import java.util.Objects;

/** Coordina la preparación y confirmación de un pago local sin depender de JavaFX. */
public class PagoCompraService {
    private static final BigDecimal VALOR_PUNTO = new BigDecimal("0.05");
    private final PuntosService puntos;
    private final ReservasManager registro;

    public PagoCompraService() { this(new PuntosService(), ReservasManager.getInstance()); }
    public PagoCompraService(PuntosService puntos) { this(puntos, ReservasManager.getInstance()); }
    public PagoCompraService(PuntosService puntos, ReservasManager registro) {
        this.puntos = Objects.requireNonNull(puntos);
        this.registro = Objects.requireNonNull(registro);
    }

    public static BigDecimal equivalencia(long cantidad) {
        return BigDecimal.valueOf(cantidad).multiply(VALOR_PUNTO);
    }

    public int prepararPuntos(Cliente cliente, SesionCompra sesion, int solicitados) {
        synchronized (sesion) {
            if (sesion.getPagoConfirmado() != null) throw new CompraException("Esta compra ya fue pagada");
            if (solicitados <= 0) throw new CompraException("Ingresa una cantidad entera de puntos mayor que cero");
            if (!puntos.verificarDisponibilidad(cliente, solicitados)) {
                throw new CompraException("Puntos disponibles insuficientes");
            }
            int aplicados = sesion.calcularTotalConPromociones().divideToIntegralValue(VALOR_PUNTO)
                    .min(BigDecimal.valueOf(solicitados)).intValueExact();
            sesion.prepararPuntos(aplicados, equivalencia(aplicados), cliente.getId());
            return aplicados;
        }
    }

    public SesionCompra.ComprobantePago pagar(Cliente cliente, SesionCompra sesion) {
        Objects.requireNonNull(cliente, "El cliente es obligatorio");
        synchronized (sesion) {
            if (sesion.getPagoConfirmado() != null) {
                if (!cliente.getId().equals(sesion.getPagoConfirmado().clienteId())) {
                    throw new CompraException("La compra confirmada pertenece a otro cliente");
                }
                return sesion.getPagoConfirmado();
            }
            if (!sesion.tienePeliculaSeleccionada() || !sesion.tieneFuncionSeleccionada()
                    || !sesion.tieneAsientosSeleccionados()) {
                throw new CompraException("Selecciona película, función y asientos antes de pagar");
            }
            return registro.ejecutarTransaccion(() -> cliente.ejecutarPagoAtomico(() -> {
                int cantidad = sesion.getPuntosAplicados();
                BigDecimal descuento = sesion.getValorDescuentoPuntos();
                BigDecimal antesDePuntos = sesion.calcularTotalConPromociones();
                if (descuento.compareTo(equivalencia(cantidad)) != 0 || descuento.compareTo(antesDePuntos) > 0) {
                    throw new CompraException("El descuento preparado no coincide con los puntos. Aplícalos nuevamente.");
                }
                if (cantidad > 0 && (!cliente.getId().equals(sesion.getClientePuntosId())
                        || !puntos.verificarDisponibilidad(cliente, cantidad))) {
                    throw new CompraException("Los puntos preparados ya no están disponibles para este cliente");
                }
                BigDecimal cobro = antesDePuntos.subtract(descuento);
                TarjetaVirtual tarjeta = cliente.getTarjeta();
                if (!tarjeta.isActiva()) throw new TarjetaInactivaException("La tarjeta virtual está inactiva");
                if (!Double.isFinite(tarjeta.getSaldo())) throw new CompraException("El saldo de la tarjeta no es válido");
                if (cobro.signum() > 0 && !tarjeta.validarSaldoSuficiente(cobro.doubleValue())) {
                    throw new SaldoInsuficienteException(tarjeta.getSaldo(), cobro.doubleValue());
                }
                // Comprobar el rango antes de modificar saldo o lotes.
                cobro.divideToIntegralValue(BigDecimal.valueOf(100)).intValueExact();
                if (cantidad > 0 && puntos.redimirPuntos(cliente, cantidad).compareTo(descuento) != 0) {
                    throw new CompraException("La equivalencia de redención no coincide con el descuento preparado");
                }
                // La tarjeta no registra cargos de cero: una compra cubierta por puntos no genera un cargo monetario.
                if (cobro.signum() > 0) tarjeta.deducir(cobro.doubleValue());
                int generados = puntos.acumularPuntos(cliente, cobro);
                SesionCompra.ComprobantePago comprobante = new SesionCompra.ComprobantePago(
                        sesion.getIdCompra(), cliente.getId(), cobro, cantidad, descuento, generados);
                var compra = new co.edu.uq.cinemauq.core.factory.Compra(comprobante.idCompra(),
                        sesion.getFuncionSeleccionada(), sesion.getAsientosSeleccionados(),
                        sesion.getProductosSeleccionados().stream().map(item -> item.getNombre()).toList(),
                        EstadoCompra.ACTIVA, java.time.LocalDateTime.now());
                var reservas = compra.asientos().stream().map(asiento -> new ReservaAsiento(compra.id(),
                        cliente.getId(), cliente.getNombre(), compra.funcion().getId(), compra.funcion().getPelicula().getTitulo(),
                        compra.funcion().getSala().getNumero(), compra.funcion().getFechaHora(), asiento,
                        cobro.doubleValue() / compra.asientos().size())).toList();
                registro.registrarCompra(new CompraConfirmada(compra, cliente, tarjeta, cobro, reservas, null, null));
                sesion.confirmarPago(comprobante);
                return comprobante;
            }));
        }
    }
}
