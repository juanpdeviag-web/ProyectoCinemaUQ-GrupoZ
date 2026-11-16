package co.edu.uq.cinemauq.patterns.observer;

import co.edu.uq.cinemauq.services.AdministracionService;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.LinkedHashMap;

public class GraficoProductosObserver implements EstadisticasObserver {
    private final LineChart<Number, Number> lineChart;
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMM yyyy");

    public GraficoProductosObserver(LineChart<Number, Number> lineChart) {
        this.lineChart = lineChart;
    }

    @Override
    public void actualizar(EstadisticasEvent evento) {
        var indicadores = evento.indicadores();
        lineChart.getData().clear();
        XYChart.Series<Number, Number> serie = new XYChart.Series<>();
        LocalDate inicio = evento.desde();
        LocalDate fin = evento.hasta();
        Map<YearMonth, Double> ventasPorMes = new LinkedHashMap<>();
        
        var servicio = EstadisticasManager.getInstance().getServicio();
        if (servicio != null) {
            String clienteId = EstadisticasManager.getInstance().getClienteSeleccionado();
            
            servicio.compras().stream()
                .filter(c -> {
                    var fecha = c.compra().fechaCompra().toLocalDate();
                    return !fecha.isBefore(inicio) && !fecha.isAfter(fin);
                })
                .filter(c -> clienteId == null || c.cliente().getId().equals(clienteId))
                .filter(c -> !c.compra().estaCancelada() && c.reembolso() == null)
                .forEach(c -> {
                    YearMonth mes = YearMonth.from(c.compra().fechaCompra());
                    double monto = c.importePagado().doubleValue();
                    ventasPorMes.merge(mes, monto, Double::sum);
                });
        }
        if (ventasPorMes.isEmpty()) {
            double totalVentas = indicadores.getOrDefault("Ventas cobradas", 0.0);
            if (totalVentas > 0) {
                YearMonth mesActual = YearMonth.from(inicio);
                YearMonth mesFinal = YearMonth.from(fin);
                int meses = (int) java.time.temporal.ChronoUnit.MONTHS.between(mesActual, mesFinal) + 1;
                double promedio = totalVentas / meses;
                
                int mes = 0;
                while (!mesActual.isAfter(mesFinal)) {
                    serie.getData().add(new XYChart.Data<>(mes, promedio));
                    mesActual = mesActual.plusMonths(1);
                    mes++;
                }
            }
        } else {
            int mes = 0;
            YearMonth mesActual = YearMonth.from(inicio);
            YearMonth mesFinal = YearMonth.from(fin);
            
            while (!mesActual.isAfter(mesFinal) && mes < 12) {
                double ventas = ventasPorMes.getOrDefault(mesActual, 0.0);
                serie.getData().add(new XYChart.Data<>(mes, ventas));
                mesActual = mesActual.plusMonths(1);
                mes++;
            }
        }
        lineChart.getData().add(serie);
        NumberAxis xAxis = (NumberAxis) lineChart.getXAxis();
        xAxis.setTickLabelFormatter(new javafx.util.StringConverter<Number>() {
            @Override
            public String toString(Number n) {
                YearMonth mes = YearMonth.from(inicio).plusMonths(n.intValue());
                return mes.format(MONTH_FORMATTER);
            }
            
            @Override
            public Number fromString(String s) {
                return 0;
            }
        });
    }
    public static LineChart<Number, Number> crearGrafico() {
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Tendencia de ventas mensuales · COP");
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setPrefHeight(300);
        return chart;
    }
}
