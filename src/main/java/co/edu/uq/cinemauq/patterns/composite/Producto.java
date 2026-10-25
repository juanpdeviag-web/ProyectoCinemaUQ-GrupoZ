package co.edu.uq.cinemauq.patterns.composite;

public record Producto(String nombre, double precio) implements ItemCompra {

    public Producto {
        if (nombre == null || nombre.isBlank() || precio < 0) throw new IllegalArgumentException("Producto inválido");
    }

    @Override
    public String getNombre() { return nombre; }

    @Override
    public double getPrecio() { return precio; }
}
