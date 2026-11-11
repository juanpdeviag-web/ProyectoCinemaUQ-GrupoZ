package co.edu.uq.cinemauq.ui.controllers;

import co.edu.uq.cinemauq.core.domain.*;
import co.edu.uq.cinemauq.core.exceptions.CompraException;
import co.edu.uq.cinemauq.core.exceptions.ReembolsoDuplicadoException;
import co.edu.uq.cinemauq.core.managers.ReservasManager;
import co.edu.uq.cinemauq.services.CancelacionService;
import co.edu.uq.cinemauq.ui.utils.Navegador;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controlador para la pantalla de Cancelación de Compras.
 * Permite a los clientes solicitar cancelaciones y ver políticas aplicadas.
 * Implementa: RF-0XX (Cancelación y Reembolso)
 * Patrón de Comportamiento: Strategy
 */
public class CancelacionController implements Initializable {

    @FXML
    private VBox comprasContainer;

    @FXML
    private Label sinComprasLabel;

    @FXML
    private Button btnVolver;

private CancelacionService cancelacionService;
    private DateTimeFormatter fechaFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sinComprasLabel.managedProperty().bind(sinComprasLabel.visibleProperty());
        inicializarCancelacionService();
        cargarCompras();

    }

    private void inicializarCancelacionService() {
        cancelacionService = new CancelacionService(
            Navegador.getUserRepository(),
            Navegador.getPagoProcessor()
        );
    }

    private void cargarCompras() {
        comprasContainer.getChildren().clear();
        Usuario usuarioActual = Navegador.getUsuarioActual();

        if (usuarioActual == null) {
            sinComprasLabel.setText("Debes iniciar sesión para ver tus compras");
            sinComprasLabel.setVisible(true);
            return;
        }


        List<ReservaAsiento> reservas = ReservasManager.getInstance().getReservasPorUsuario(usuarioActual.getId());

        if (reservas.isEmpty()) {
            sinComprasLabel.setText("No tienes compras registradas");
            sinComprasLabel.setVisible(true);
            return;
        }

        sinComprasLabel.setVisible(false);

        Map<String, List<ReservaAsiento>> reservasPorFuncion = reservas.stream()
            .collect(Collectors.groupingBy(r -> r.getFuncionId()));

        for (Map.Entry<String, List<ReservaAsiento>> entry : reservasPorFuncion.entrySet()) {
            List<ReservaAsiento> reservasGrupo = entry.getValue();
            ReservaAsiento primera = reservasGrupo.get(0);

            VBox compraCard = crearTarjetaCompra(reservasGrupo);
            comprasContainer.getChildren().add(compraCard);
        }
    }

    private VBox crearTarjetaCompra(List<ReservaAsiento> reservas) {
        ReservaAsiento primera = reservas.get(0);
        String compraId = primera.getFuncionId();

        VBox card = new VBox();
        card.setSpacing(10);
        card.getStyleClass().add("card");

        Label peliculaLabel = new Label("🎬 " + primera.getPeliculaTitulo());
        peliculaLabel.getStyleClass().add("card-title");

        Label fechaLabel = new Label("📅 " + primera.getFechaFuncion().format(fechaFormatter));
        fechaLabel.getStyleClass().add("helper-text");

        Label salaLabel = new Label("🏢 Sala " + primera.getSalaNumero());
        salaLabel.getStyleClass().add("helper-text");

        String asientos = reservas.stream()
            .map(ReservaAsiento::getAsientoId)
            .sorted()
            .collect(Collectors.joining(", "));
        Label asientosLabel = new Label("💺 Asientos: " + asientos);
        asientosLabel.getStyleClass().add("helper-text");

        double total = reservas.stream().mapToDouble(ReservaAsiento::getMontoPagado).sum();
        Label totalLabel = new Label("💰 Total: COP $" + String.format("%,.0f", total));
        totalLabel.getStyleClass().add("stat-value");

        Label tiempoRestanteLabel = calcularTiempoRestante(primera.getFechaFuncion());
        tiempoRestanteLabel.getStyleClass().add("helper-text");

        Button btnCancelar = new Button("❌ Cancelar Compra");
        btnCancelar.getStyleClass().add("boton-peligro");
        btnCancelar.setOnAction(event -> mostrarDialogoCancelacion(compraId, primera.getPeliculaTitulo(), total));

        card.getChildren().addAll(peliculaLabel, fechaLabel, salaLabel, asientosLabel, totalLabel, tiempoRestanteLabel, btnCancelar);
        boolean reembolsada = cancelacionService.getHistorialReembolsos().stream()
                .anyMatch(r -> r.idCompra().equals(compraId) && r.procesado());
        long horas = Duration.between(LocalDateTime.now(), primera.getFechaFuncion()).toHours();
        Label estado = new Label(reembolsada ? "REEMBOLSADO" : horas < 2 ? "NO CANCELABLE" : "CANCELABLE POR HORARIO");
        estado.getStyleClass().add(reembolsada ? "estado-inactivo" : "estado-activo");
        card.getChildren().add(1, estado);
        btnCancelar.setDisable(reembolsada || horas < 2);
        if (btnCancelar.isDisabled()) {
            btnCancelar.setTooltip(new Tooltip(reembolsada ? "Compra reembolsada" : "Fuera del plazo de cancelación"));
        }
        for (javafx.scene.Node nodo : card.getChildren()) {
            if (nodo instanceof Label etiqueta) etiqueta.setWrapText(true);
        }
        return card;
    }

    private Label calcularTiempoRestante(LocalDateTime fechaFuncion) {
        LocalDateTime ahora = LocalDateTime.now();
        Duration duracion = Duration.between(ahora, fechaFuncion);
        long horas = duracion.toHours();

        if (horas < 0) {
            return new Label("⚠️ Función ya ocurrió");
        } else if (horas < 2) {
            return new Label("⚠️ Menos de 2h - No se permite cancelación");
        } else if (horas < 24) {
            return new Label("⏰ " + horas + "h restantes - Reembolso 80%");
        } else {
            return new Label("✅ " + horas + "h restantes - Reembolso 100%");
        }
    }

    private void mostrarDialogoCancelacion(String compraId, String peliculaTitulo, double monto) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        aplicarTema(alert);
        alert.setTitle("Cancelar Compra");
        alert.setHeaderText("¿Deseas cancelar la compra de: " + peliculaTitulo + "?");
        alert.setContentText("Monto original: COP $" + String.format("%,.0f", monto) + "\n\nEl reembolso se aplicará según la política vigente.");

        TextArea motivoArea = new TextArea();
        motivoArea.setPromptText("Ingresa el motivo de la cancelación...");
        motivoArea.setWrapText(true);
        motivoArea.setMaxHeight(100);

        VBox content = new VBox(10, new Label("Motivo:"), motivoArea);
        alert.getDialogPane().setContent(content);

        ButtonType btnConfirmar = new ButtonType("Confirmar Cancelación", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancelar = new ButtonType("Volver", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(btnConfirmar, btnCancelar);

        alert.showAndWait().ifPresent(response -> {
            if (response == btnConfirmar) {
                String motivo = motivoArea.getText();
                if (motivo == null || motivo.trim().isEmpty()) {
                    mostrarError("El motivo es obligatorio");
                    return;
                }
                procesarCancelacion(compraId, motivo);
            }
        });
    }

    private void procesarCancelacion(String compraId, String motivo) {
        try {
            Reembolso reembolso = cancelacionService.solicitarCancelacionPorCliente(compraId, motivo);
            mostrarReembolsoExitoso(reembolso);
            cargarCompras();
        } catch (ReembolsoDuplicadoException e) {
            mostrarError("Esta compra ya ha sido reembolsada anteriormente");
        } catch (CompraException e) {
            mostrarError(e.getMessage());
        } catch (Exception e) {
            mostrarError("Error al procesar la cancelación: " + e.getMessage());
        }
    }

    private void mostrarReembolsoExitoso(Reembolso reembolso) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        aplicarTema(alert);
        alert.setTitle("Reembolso Procesado");
        alert.setHeaderText("✅ Reembolso Exitoso");

        String mensaje = String.format(
            "Monto reembolsado: COP $%,.0f\n" +
            "Porcentaje aplicado: %.0f%%\n" +
            "Política: %s\n" +
            "Fecha: %s",
            reembolso.montoReembolsado(),
            reembolso.porcentajeAplicado() * 100,
            reembolso.politicaAplicada(),
            reembolso.fechaReembolso().format(fechaFormatter)
        );

        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        aplicarTema(alert);
        alert.setTitle("Error");
        alert.setHeaderText("No se pudo procesar la cancelación");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void aplicarTema(Alert alerta) {
        alerta.getDialogPane().getStylesheets().add(getClass().getResource("/ui/styles/estilos.css").toExternalForm());
        alerta.getDialogPane().getStyleClass().add("root-app");
    }

@FXML
    private void onVolver() {
        Navegador.navegarA("/ui/views/mis_tickets.fxml", "CinemaUQ - Mis Tickets");
    }

@FXML
    private void onCancelaciones() {
        // Ya estamos en Cancelaciones
    }


}
