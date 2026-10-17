package co.edu.uq.cinemauq.patterns.adapter;

public class ReporteAdapter implements Reporte {
    private final GeneradorReporteExterno generador;

    public ReporteAdapter(GeneradorReporteExterno generador) {
        if (generador == null) throw new IllegalArgumentException("Generador requerido");
        this.generador = generador;
    }

    @Override
    public String generar(String titulo, String contenido) {
        return generador.exportarComoTextoPlano(titulo, contenido);
    }
}
