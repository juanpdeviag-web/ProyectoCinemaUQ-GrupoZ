package co.edu.uq.cinemauq.patterns.decorator;

public record CompraBase(String descripcion, double precio) implements Compra {
    public CompraBase {
        if (descripcion == null || descripcion.isBlank() || precio < 0) throw new IllegalArgumentException("Compra inválida");
    }
}
