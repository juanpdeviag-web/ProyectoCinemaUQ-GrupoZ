package co.edu.uq.cinemauq.patterns.facade;
import java.util.List;
/** Fachada simple para que un controlador JavaFX no coordine subsistemas por separado. */
public class CompraFacade {
    public record Resumen(String detalle, double total) {}
    public Resumen prepararCompra(String funcion, List<String> asientos, List<String> productos, double precioEntrada, double precioProductos) {
        if (funcion == null || funcion.isBlank()) throw new IllegalArgumentException("Seleccione una función");

        if (asientos == null || asientos.isEmpty()) throw new IllegalArgumentException("Seleccione al menos un asiento");

        if (precioEntrada < 0 || precioProductos < 0) throw new IllegalArgumentException("Precios inválidos");

        double total = asientos.size() * precioEntrada + precioProductos;
        String detalle = "Función: " + funcion + "; asientos: " + String.join(", ", asientos)
            + "; productos: " + (productos == null ? "ninguno" : String.join(", ", productos));
        return new Resumen(detalle, total);
    }
}
