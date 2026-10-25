package co.edu.uq.cinemauq.patterns.decorator;
public class Promocion extends CompraDecorador {
    private final String nombre;
    private final double descuento; // porcentaje de 0 a 100
    public Promocion(Compra compra, String nombre, double descuento) {
        super(compra);
        if (descuento < 0 || descuento > 100) throw new IllegalArgumentException("Descuento entre 0 y 100");
        this.nombre = nombre; this.descuento = descuento;
    }
    @Override public String descripcion() { return compra.descripcion() + " + promoción " + nombre + " (" + descuento + "%)"; }
    @Override public double precio() { return compra.precio() * (1 - descuento / 100.0); }
}
