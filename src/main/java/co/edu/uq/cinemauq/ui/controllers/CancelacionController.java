package co.edu.uq.cinemauq.ui.controllers;

import co.edu.uq.cinemauq.core.domain.*;
import co.edu.uq.cinemauq.core.exceptions.CompraException;
import co.edu.uq.cinemauq.core.exceptions.ReembolsoDuplicadoException;

import co.edu.uq.cinemauq.services.CancelacionService;
import co.edu.uq.cinemauq.ui.utils.Navegador;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.net.URL;


import java.time.format.DateTimeFormatter;
import java.util.List;

import java.util.ResourceBundle;


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
        cancelacionService = new CancelacionService();
    }

    private void cargarCompras() {
        comprasContainer.getChildren().clear();
        Usuario usuario = Navegador.getUsuarioActual();
        if (!(usuario instanceof Cliente)) {
            sinComprasLabel.setText("Inicia sesión como cliente para consultar tus compras.");
            sinComprasLabel.setVisible(true);
            return;
        }
        List<CompraConfirmada> compras = cancelacionService.listarComprasPorCliente(usuario.getId());
        sinComprasLabel.setVisible(compras.isEmpty());
        for (CompraConfirmada compra : compras) comprasContainer.getChildren().add(crearTarjetaCompra(compra));
    }

    private VBox crearTarjetaCompra(CompraConfirmada registro) {
        var compra = registro.compra();
        String compraId = compra.id();
        VBox card = new VBox(10);
        card.setUserData(compraId);
        card.getStyleClass().add("card");
        Label pelicula = new Label("🎬 " + compra.funcion().getPelicula().getTitulo());
        pelicula.getStyleClass().add("card-title");
        Label identidad = new Label("Compra: " + compraId);
        identidad.setId("compraIdLabel");
        identidad.getStyleClass().add("helper-text");
        boolean cancelada = compra.estaCancelada();
        boolean reembolsada = registro.reembolso() != null;
        boolean cancelable = cancelacionService.validarTiempo(compraId);
        Label estado = new Label(cancelada ? (reembolsada ? "CANCELADA · REEMBOLSADA" : "CANCELADA")
                : cancelable ? "CANCELABLE" : "NO CANCELABLE");
        estado.setId("estadoCompraLabel");
        estado.getStyleClass().add(cancelada ? "estado-inactivo" : "estado-activo");
        Label fecha = new Label("📅 " + compra.funcion().getFechaHora().format(fechaFormatter));
        Label sala = new Label("Sala: " + compra.funcion().getSala().getNumero());
        Label asientos = new Label("Asientos: " + String.join(", ", compra.asientos()));
        Label importe = new Label(String.format("Pagado con tarjeta: COP $%,.2f", registro.importePagado()));
        importe.getStyleClass().add("stat-value");
        Label politica = new Label(cancelada ? "Esta operación ya está cerrada."
                : cancelable ? "Reembolso aplicable: " + Math.round(cancelacionService.porcentajeReembolso(compraId) * 100) + " % del dinero pagado."
                : "No se permite cancelar a menos de 2 horas de la función.");
        Button cancelar = new Button("Cancelar compra");
        cancelar.setId("btnCancelarCompra");
        cancelar.setUserData(compraId);
        cancelar.getStyleClass().add("boton-peligro");
        cancelar.setDisable(cancelada || !cancelable);
        cancelar.setOnAction(e -> mostrarDialogoCancelacion(compraId, compra.funcion().getPelicula().getTitulo(), registro.importePagado().doubleValue()));
        card.getChildren().addAll(pelicula, identidad, estado, fecha, sala, asientos, importe, politica);
        if (reembolsada) {
            Label devuelto = new Label(String.format("Reembolsado: COP $%,.2f · %.0f %%", registro.reembolso().montoReembolsado(), registro.reembolso().porcentajeAplicado() * 100));
            devuelto.setId("valorReembolsoLabel");
            devuelto.getStyleClass().add("mensaje-exito");
            card.getChildren().add(devuelto);
        }
        card.getChildren().add(cancelar);
        card.getChildren().forEach(n -> { if (n instanceof Label label) label.setWrapText(true); });
        return card;
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

        VBox content = new VBox(10, new Label(String.format("Importe realmente pagado: COP $%,.2f", monto)), new Label("Motivo:"), motivoArea);
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
            Reembolso reembolso = cancelacionService.solicitarCancelacionPorCliente(compraId, motivo, Navegador.getUsuarioActual().getId());
            cargarCompras();
            mostrarReembolsoExitoso(reembolso);
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
            "Monto reembolsado: COP $%,.2f\n" +
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
