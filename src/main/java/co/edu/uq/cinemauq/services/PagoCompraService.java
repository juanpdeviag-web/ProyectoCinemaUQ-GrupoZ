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
    private final CatalogoCinema catalogo;

    public PagoCompraService() { this(new PuntosService(), ReservasManager.getInstance()); }
    public PagoCompraService(PuntosService puntos) { this(puntos, ReservasManager.getInstance()); }
    public PagoCompraService(PuntosService puntos, ReservasManager registro) {
        this(puntos, registro, CatalogoCinema.getInstance());
    }
    public PagoCompraService(PuntosService puntos, ReservasManager registro, CatalogoCinema catalogo) {
        this.puntos = Objects.requireNonNull(puntos);
        this.registro = Objects.requireNonNull(registro);
        this.catalogo = Objects.requireNonNull(catalogo);
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
            synchronized (catalogo) {
            if (!catalogo.vendible(sesion.getFuncionSeleccionada())) throw new CompraException("La función ya no está disponible");
            if (catalogo.gestionada(sesion.getFuncionSeleccionada())) {
                if (Double.compare(sesion.getPrecioEntrada(), catalogo.precio(sesion.getFuncionSeleccionada())) != 0)
                    throw new CompraException("El precio de la función cambió; vuelve a seleccionarla");
                var disponibles = catalogo.confiteria();
                for (var item : sesion.getProductosSeleccionados()) if (disponibles.stream().noneMatch(p -> p.getNombre().equals(item.getNombre()) && Double.compare(p.getPrecio(), item.getPrecio()) == 0))
                    throw new CompraException("La confitería cambió; vuelve a seleccionarla");
            }
            return registro.ejecutarTransaccion(() -> cliente.ejecutarPagoAtomico(() -> {
                for (String asiento : sesion.getAsientosSeleccionados()) {
                    if (!catalogo.asientoHabilitado(sesion.getFuncionSeleccionada(), asiento)) throw new CompraException("Asiento deshabilitado: " + asiento);
                    if (co.edu.uq.cinemauq.core.managers.AsientosManager.getInstance().isAsientoOcupado(sesion.getFuncionSeleccionada().getId(), asiento))
                        throw new CompraException("El asiento ya fue vendido: " + asiento);
                }
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
                java.util.Map<String, CompraConfirmada.DetalleItem> items = new java.util.LinkedHashMap<>();
                for(var item:sesion.getProductosSeleccionados()) {
                    var anterior=items.get(item.getNombre());
                    items.put(item.getNombre(),new CompraConfirmada.DetalleItem(item.getNombre(),anterior==null?1:anterior.cantidad()+1,
                            BigDecimal.valueOf(item.getPrecio()),item instanceof co.edu.uq.cinemauq.patterns.composite.Combo));
                }
                registro.registrarCompra(new CompraConfirmada(compra, cliente, tarjeta, cobro, reservas, null, null,
                        new CompraConfirmada.DetallePago(sesion.descripcionPromociones(), cantidad, descuento,
                                BigDecimal.valueOf(sesion.getPrecioEntrada()).multiply(BigDecimal.valueOf(compra.asientos().size())),
                                BigDecimal.valueOf(sesion.getPrecioProductos()), BigDecimal.valueOf(sesion.calcularTotal()).subtract(antesDePuntos),
                                java.util.List.copyOf(items.values()))));
                sesion.confirmarPago(comprobante);
                return comprobante;
            }));
            }
        }
    }
}
