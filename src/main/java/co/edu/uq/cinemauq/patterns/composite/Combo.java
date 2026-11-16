package co.edu.uq.cinemauq.patterns.composite;
import java.util.ArrayList;
import java.util.List;

// Un combo trata productos y otros combos mediante la misma interfaz.
public class Combo implements ItemCompra {
    private final String nombre;
    private final Double precioConfigurado;
    private final List<ItemCompra> items = new ArrayList<>();

    public Combo(String nombre) {
        this(nombre, null);
    }

    public Combo(String nombre, Double precio) {
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("Nombre requerido");
        if (precio != null && (!Double.isFinite(precio) || precio < 0)) throw new IllegalArgumentException("Precio inválido");
        this.nombre = nombre;
        this.precioConfigurado = precio;
    }

    public Combo agregar(ItemCompra item) {
        if (item == null) throw new IllegalArgumentException("El item no puede ser nulo");
        items.add(item); return this;
    }

    public List<ItemCompra> getItems() { return List.copyOf(items); }

    @Override
    public String getNombre() { return nombre; }

    @Override
    public double getPrecio() { return precioConfigurado != null ? precioConfigurado : items.stream().mapToDouble(ItemCompra::getPrecio).sum(); }
}
