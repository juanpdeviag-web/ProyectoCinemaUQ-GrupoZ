package co.edu.uq.cinemauq.ui.models;

import co.edu.uq.cinemauq.core.domain.Funcion;
import co.edu.uq.cinemauq.core.domain.Pelicula;
import co.edu.uq.cinemauq.patterns.composite.ItemCompra;

import java.util.ArrayList;
import java.util.List;

/**
 * Mantiene el estado de una compra en curso a través de las diferentes pantallas.
 * Incluye una redención de puntos pendiente, que solo se consume al confirmar el pago.
 */
public class SesionCompra {
    private Pelicula peliculaSeleccionada;
    private Funcion funcionSeleccionada;
    private final List<String> asientosSeleccionados;
    private final List<ItemCompra> productosSeleccionados;
    private double precioEntrada;
    private double precioProductos;
    private int puntosARedimir;
    private double valorRedencionPuntos;

    public SesionCompra() {
        this.asientosSeleccionados = new ArrayList<>();
        this.productosSeleccionados = new ArrayList<>();
        this.precioEntrada = 60000.0; // Precio base de entrada en COP
        this.precioProductos = 0.0;
        this.puntosARedimir = 0;
        this.valorRedencionPuntos = 0.0;
    }

    public Pelicula getPeliculaSeleccionada() {
        return peliculaSeleccionada;
    }

    public void setPeliculaSeleccionada(Pelicula peliculaSeleccionada) {
        this.peliculaSeleccionada = peliculaSeleccionada;
    }

    public Funcion getFuncionSeleccionada() {
        return funcionSeleccionada;
    }

    public void setFuncionSeleccionada(Funcion funcionSeleccionada) {
        this.funcionSeleccionada = funcionSeleccionada;
    }

    public List<String> getAsientosSeleccionados() {
        return asientosSeleccionados;
    }

    public void agregarAsiento(String asiento) {
        if (!this.asientosSeleccionados.contains(asiento)) {
            this.asientosSeleccionados.add(asiento);
        }
    }

    public void removerAsiento(String asiento) {
        this.asientosSeleccionados.remove(asiento);
    }

    public void limpiarAsientos() {
        this.asientosSeleccionados.clear();
    }

    public List<ItemCompra> getProductosSeleccionados() {
        return productosSeleccionados;
    }

    public void agregarProducto(ItemCompra producto) {
        this.productosSeleccionados.add(producto);
        this.precioProductos += producto.getPrecio();
    }

    public void removerProducto(ItemCompra producto) {
        if (this.productosSeleccionados.remove(producto)) {
            this.precioProductos -= producto.getPrecio();
        }
    }

    public void limpiarProductos() {
        this.productosSeleccionados.clear();
        this.precioProductos = 0.0;
    }

    public double getPrecioEntrada() {
        return precioEntrada;
    }

    public void setPrecioEntrada(double precioEntrada) {
        this.precioEntrada = precioEntrada;
    }

    public double getPrecioProductos() {
        return precioProductos;
    }

    /** Total de entradas y productos antes de aplicar puntos. */
    public double calcularSubtotalSinPuntos() {
        return (asientosSeleccionados.size() * precioEntrada) + precioProductos;
    }

    /** Total final después de la redención pendiente de puntos. */
    public double calcularTotal() {
        return Math.max(0.0, calcularSubtotalSinPuntos() - valorRedencionPuntos);
    }

    public void aplicarRedencionPuntos(int puntos, double valorMonetario) {
        if (puntos <= 0) {
            throw new IllegalArgumentException("Los puntos a redimir deben ser mayores que cero");
        }
        if (valorMonetario <= 0) {
            throw new IllegalArgumentException("El valor de redención debe ser mayor que cero");
        }
        if (valorMonetario > calcularSubtotalSinPuntos()) {
            throw new IllegalArgumentException("La redención no puede superar el valor de la compra");
        }
        this.puntosARedimir = puntos;
        this.valorRedencionPuntos = valorMonetario;
    }

    public void quitarRedencionPuntos() {
        this.puntosARedimir = 0;
        this.valorRedencionPuntos = 0.0;
    }

    public boolean tieneRedencionPuntos() {
        return puntosARedimir > 0;
    }

    public int getPuntosARedimir() {
        return puntosARedimir;
    }

    public double getValorRedencionPuntos() {
        return valorRedencionPuntos;
    }

    public void limpiar() {
        this.peliculaSeleccionada = null;
        this.funcionSeleccionada = null;
        this.asientosSeleccionados.clear();
        this.productosSeleccionados.clear();
        this.precioProductos = 0.0;
        quitarRedencionPuntos();
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
}
