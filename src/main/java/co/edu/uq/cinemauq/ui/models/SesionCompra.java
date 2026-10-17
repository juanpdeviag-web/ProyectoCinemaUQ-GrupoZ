package co.edu.uq.cinemauq.ui.models;

import co.edu.uq.cinemauq.core.domain.Funcion;
import co.edu.uq.cinemauq.core.domain.Pelicula;
import co.edu.uq.cinemauq.patterns.composite.ItemCompra;

import java.util.ArrayList;
import java.util.List;

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

    public double calcularTotal() {
        return (asientosSeleccionados.size() * precioEntrada) + precioProductos;
    }

    public void limpiar() {
        this.peliculaSeleccionada = null;
        this.funcionSeleccionada = null;
        this.asientosSeleccionados.clear();
        this.productosSeleccionados.clear();
        this.precioProductos = 0.0;
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
