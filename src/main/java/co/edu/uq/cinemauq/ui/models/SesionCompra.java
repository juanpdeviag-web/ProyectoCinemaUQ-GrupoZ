package co.edu.uq.cinemauq.ui.models;

import co.edu.uq.cinemauq.core.domain.Funcion;
import co.edu.uq.cinemauq.core.domain.Pelicula;
import co.edu.uq.cinemauq.patterns.composite.ItemCompra;

import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

import java.util.UUID;




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
    private final java.util.Set<String> promocionesAdicionales = new java.util.LinkedHashSet<>();
    private final co.edu.uq.cinemauq.services.CatalogoCinema catalogo;

    public record ComprobantePago(String idCompra, String clienteId, BigDecimal cobrado, int puntosRedimidos,
                                 BigDecimal descuentoPuntos, int puntosGenerados) { }

    public SesionCompra() {
        this(co.edu.uq.cinemauq.services.CatalogoCinema.getInstance());
    }
    public SesionCompra(co.edu.uq.cinemauq.services.CatalogoCinema catalogo) {
        this.catalogo = java.util.Objects.requireNonNull(catalogo);
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
        promocionesAdicionales.clear();
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
            promocionesAdicionales.clear();
        }
    }

    public BigDecimal calcularTotalConPromociones() {
        return catalogo.aplicarPromociones(BigDecimal.valueOf(calcularTotal()), getPromocionesSeleccionadas());
    }

    public java.util.Set<String> getPromocionesSeleccionadas() {
        java.util.Set<String> ids = new java.util.LinkedHashSet<>();
        if(descuentoFijo) ids.add("FIJO");
        if(promocionEstudiante) ids.add("ESTUDIANTE");
        ids.addAll(promocionesAdicionales); return java.util.Collections.unmodifiableSet(ids);
    }
    public void seleccionarPromocion(String id, boolean seleccionada) {
        quitarPuntos();
        if(id.equals("FIJO")) descuentoFijo=seleccionada;
        else if(id.equals("ESTUDIANTE")) promocionEstudiante=seleccionada;
        else if(seleccionada) promocionesAdicionales.add(id); else promocionesAdicionales.remove(id);
    }
    public String descripcionPromociones() {
        return catalogo.promocionesVigentes().stream().filter(p->getPromocionesSeleccionadas().contains(p.id()))
                .map(p->p.valor("Nombre")+" ("+p.valor("Tipo")+" "+p.valor("Valor")+")")
                .collect(java.util.stream.Collectors.joining(", "));
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
