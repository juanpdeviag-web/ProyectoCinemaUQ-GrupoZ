package co.edu.uq.cinemauq.patterns.decorator;

public abstract class CompraDecorador implements Compra {
    protected final Compra compra;

    protected CompraDecorador(Compra compra) {
        if (compra == null) throw new IllegalArgumentException("Compra requerida");
        this.compra = compra;
    }
}
