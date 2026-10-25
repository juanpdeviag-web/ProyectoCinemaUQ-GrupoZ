package co.edu.uq.cinemauq.patterns.adapter;

// Biblioteca externa, el metodo no coincide con la interfaz

public class GeneradorReporteExterno {
    public String exportarComoTextoPlano(String titulo, String contenido) {
        return "=== " + titulo + " ===\\n" + contenido;
    }
}