package co.edu.uq.cinemauq.patterns.observer;

import javafx.scene.chart.PieChart;

public class GraficoOcupacionObserver implements EstadisticasObserver {
    private final PieChart pieChart;

    public GraficoOcupacionObserver(PieChart pieChart) {
        this.pieChart = pieChart;
    }

    @Override
    public void actualizar(EstadisticasEvent evento) {
        var indicadores = evento.indicadores();
        pieChart.getData().clear();
        double disponibles = indicadores.getOrDefault("Puntos disponibles", 0.0);
        double vencidos = indicadores.getOrDefault("Puntos vencidos", 0.0);
        double redimidos = indicadores.getOrDefault("Puntos redimidos", 0.0);
        if (disponibles > 0 || vencidos > 0 || redimidos > 0) {
            if (disponibles > 0) {
                pieChart.getData().add(new PieChart.Data("Puntos disponibles", disponibles));
            }
            if (vencidos > 0) {
                pieChart.getData().add(new PieChart.Data("Puntos vencidos", vencidos));
            }
            if (redimidos > 0) {
                pieChart.getData().add(new PieChart.Data("Puntos redimidos", redimidos));
            }
        } else {
            pieChart.getData().add(new PieChart.Data("Sin puntos", 1));
        }
    }
    public static PieChart crearGrafico() {
        PieChart chart = new PieChart();
        chart.setTitle("Estado de puntos · situación actual");
        chart.setAnimated(false);
        chart.setPrefHeight(300);
        return chart;
    }
}
