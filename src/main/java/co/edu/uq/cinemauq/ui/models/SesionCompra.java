package co.edu.uq.cinemauq.ui.models;

import co.edu.uq.cinemauq.core.domain.Funcion;
import co.edu.uq.cinemauq.core.domain.Pelicula;
import co.edu.uq.cinemauq.patterns.composite.ItemCompra;

import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;
import co.edu.uq.cinemauq.patterns.decorator.CompraBase;
import co.edu.uq.cinemauq.patterns.decorator.DescuentoFijo;
import co.edu.uq.cinemauq.patterns.decorator.Promocion;

/**
 * Clase que mantiene el estado de una compra en curso a través de las diferentes pantallas.
 * Permite compartir información entre controladores JavaFX sin violar MVC.
 */
public class SesionCompra {
    private Pelicula peliculaSeleccionada;
    private Funcion funcionSeleccionada;
    private List<String> asientosSeleccionados;
    private List<ItemCompra> productosSeleccionados;
    private double precioEntrada;
    private double precioProductos;
    private boolean descuentoFijo;
    private boolean promocionEstudiante;
    private int puntosAplicados;
    private BigDecimal valorDescuentoPuntos = BigDecimal.ZERO;
    private String clientePuntosId;
    private String idCompra = UUID.randomUUID().toString();
    private ComprobantePago pagoConfirmado;

    public record ComprobantePago(String idCompra, String clienteId, BigDecimal cobrado, int puntosRedimidos,
                                 BigDecimal descuentoPuntos, int puntosGenerados) { }

    public SesionCompra() {
        this.asientosSeleccionados = new ArrayList<>();
        this.productosSeleccionados = new ArrayList<>();
        this.precioEntrada = 60000.0; // Precio base de entrada en COP
        this.precioProductos = 0.0;
    }

    public Pelicula getPeliculaSeleccionada() {
        return peliculaSeleccionada;
    }

    public void setPeliculaSeleccionada(Pelicula peliculaSeleccionada) {
        invalidarPreparacion();
        this.peliculaSeleccionada = peliculaSeleccionada;
    }

    public Funcion getFuncionSeleccionada() {
        return funcionSeleccionada;
    }

    public void setFuncionSeleccionada(Funcion funcionSeleccionada) {
        invalidarPreparacion();
        this.funcionSeleccionada = funcionSeleccionada;
    }

    public List<String> getAsientosSeleccionados() {
        return asientosSeleccionados;
    }

    public void agregarAsiento(String asiento) {
        invalidarPreparacion();
        if (!this.asientosSeleccionados.contains(asiento)) {
            this.asientosSeleccionados.add(asiento);
        }
    }

    public void removerAsiento(String asiento) {
        invalidarPreparacion();
        this.asientosSeleccionados.remove(asiento);
    }

    public void limpiarAsientos() {
        invalidarPreparacion();
        this.asientosSeleccionados.clear();
    }

    public List<ItemCompra> getProductosSeleccionados() {
        return productosSeleccionados;
    }

    public void agregarProducto(ItemCompra producto) {
        invalidarPreparacion();
        this.productosSeleccionados.add(producto);
        this.precioProductos += producto.getPrecio();
    }

    public void removerProducto(ItemCompra producto) {
        invalidarPreparacion();
        if (this.productosSeleccionados.remove(producto)) {
            this.precioProductos -= producto.getPrecio();
        }
    }

    public void limpiarProductos() {
        invalidarPreparacion();
        this.productosSeleccionados.clear();
        this.precioProductos = 0.0;
    }

    public double getPrecioEntrada() {
        return precioEntrada;
    }

    public void setPrecioEntrada(double precioEntrada) {
        invalidarPreparacion();
        this.precioEntrada = precioEntrada;
    }

    public double getPrecioProductos() {
        return precioProductos;
    }

    public double calcularTotal() {
        return (asientosSeleccionados.size() * precioEntrada) + precioProductos;
    }

    public void limpiar() {
        this.peliculaSeleccionada = null;
        this.funcionSeleccionada = null;
        this.asientosSeleccionados.clear();
        this.productosSeleccionados.clear();
        this.precioProductos = 0.0;
        quitarPuntos();
        descuentoFijo = false;
        promocionEstudiante = false;
        // Conservar el comprobante para que un reintento no repita el cobro.
    }

    public boolean tienePeliculaSeleccionada() {
        return peliculaSeleccionada != null;
    }

    public boolean tieneFuncionSeleccionada() {
        return funcionSeleccionada != null;
    }

    public boolean tieneAsientosSeleccionados() {
        return !asientosSeleccionados.isEmpty();
    }

    private void invalidarPreparacion() {
        quitarPuntos();
        if (pagoConfirmado != null) {
            pagoConfirmado = null;
            idCompra = UUID.randomUUID().toString();
            descuentoFijo = false;
            promocionEstudiante = false;
        }
    }

    public BigDecimal calcularTotalConPromociones() {
        co.edu.uq.cinemauq.patterns.decorator.Compra compra = new CompraBase("Entradas y confitería", calcularTotal());
        if (descuentoFijo) compra = new DescuentoFijo(compra, 5000.0);
        if (promocionEstudiante) compra = new Promocion(compra, "Estudiante", 10.0);
        return BigDecimal.valueOf(compra.precio()).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calcularTotalAPagar() {
        return calcularTotalConPromociones().subtract(valorDescuentoPuntos).max(BigDecimal.ZERO);
    }

    public void configurarPromociones(boolean fijo, boolean estudiante) {
        descuentoFijo = fijo;
        promocionEstudiante = estudiante;
    }

    public boolean isDescuentoFijo() { return descuentoFijo; }
    public boolean isPromocionEstudiante() { return promocionEstudiante; }
    public int getPuntosAplicados() { return puntosAplicados; }
    public BigDecimal getValorDescuentoPuntos() { return valorDescuentoPuntos; }
    public String getClientePuntosId() { return clientePuntosId; }
    public String getIdCompra() { return idCompra; }
    public ComprobantePago getPagoConfirmado() { return pagoConfirmado; }

    public void prepararPuntos(int cantidad, BigDecimal valor, String clienteId) {
        if (cantidad < 0 || valor == null || valor.signum() < 0) {
            throw new IllegalArgumentException("La preparación de puntos no puede ser negativa");
        }
        puntosAplicados = cantidad;
        valorDescuentoPuntos = valor;
        clientePuntosId = clienteId;
    }

    public void quitarPuntos() {
        puntosAplicados = 0;
        valorDescuentoPuntos = BigDecimal.ZERO;
        clientePuntosId = null;
    }

    public void confirmarPago(ComprobantePago comprobante) {
        pagoConfirmado = java.util.Objects.requireNonNull(comprobante);
    }
}
