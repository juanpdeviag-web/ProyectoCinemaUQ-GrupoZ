package co.edu.uq.cinemauq.patterns.composite;
import java.util.ArrayList;
import java.util.List;

// Un combo trata productos y otros combos mediante la misma interfaz.
public class Combo implements ItemCompra {
    private final String nombre;
    private final List<ItemCompra> items = new ArrayList<>();

    public Combo(String nombre) {
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("Nombre requerido");
        this.nombre = nombre;
    }

    public Combo agregar(ItemCompra item) {
        if (item == null) throw new IllegalArgumentException("El item no puede ser nulo");
        items.add(item); return this;
    }

    public List<ItemCompra> getItems() { return List.copyOf(items); }

    @Override
    public String getNombre() { return nombre; }

    @Override
    public double getPrecio() { return items.stream().mapToDouble(ItemCompra::getPrecio).sum(); }
}
