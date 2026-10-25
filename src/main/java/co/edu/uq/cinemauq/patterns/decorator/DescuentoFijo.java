package co.edu.uq.cinemauq.patterns.decorator;

public class DescuentoFijo extends CompraDecorador {
    private final double descuento;

    public DescuentoFijo(Compra compra, double descuento) {
        super(compra);
        if (descuento < 0) throw new IllegalArgumentException("Descuento no puede ser negativo");
        this.descuento = descuento;
    }

    @Override
    public String descripcion() {
        return compra.descripcion() + " + descuento fijo $" + descuento;
    }

    @Override
    public double precio() {
        return Math.max(0, compra.precio() - descuento);
    }
}
