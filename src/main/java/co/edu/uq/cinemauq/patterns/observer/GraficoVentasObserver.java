package co.edu.uq.cinemauq.patterns.observer;

import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

public class GraficoVentasObserver implements EstadisticasObserver {
    private final BarChart<String, Number> barChart;

    public GraficoVentasObserver(BarChart<String, Number> barChart) {
        this.barChart = barChart;
    }

    @Override
    public void actualizar(EstadisticasEvent evento) {
        var indicadores = evento.indicadores();
        barChart.getData().clear();
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        double ventas = indicadores.getOrDefault("Ventas cobradas", 0.0);
        double reembolsos = indicadores.getOrDefault("Reembolsos", 0.0);
        double recargas = indicadores.getOrDefault("Recargas", 0.0);
        
        serie.getData().add(new XYChart.Data<>("Ventas cobradas", ventas));
        serie.getData().add(new XYChart.Data<>("Reembolsos", reembolsos));
        serie.getData().add(new XYChart.Data<>("Recargas", recargas));
        
        barChart.getData().add(serie);
        if (evento.tipoCambio() != null && !evento.tipoCambio().isEmpty()) {
            String titulo = "Ventas y reembolsos · COP";
            barChart.setTitle(titulo);
        }
    }
    public static BarChart<String, Number> crearGrafico() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Ventas y reembolsos · COP");
        chart.setLegendVisible(false);
        chart.setMinWidth(0);
        chart.setAnimated(false);
        chart.setPrefHeight(300);
        return chart;
    }
}
