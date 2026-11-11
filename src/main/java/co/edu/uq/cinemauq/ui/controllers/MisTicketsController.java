package co.edu.uq.cinemauq.ui.controllers;

import co.edu.uq.cinemauq.core.domain.ReservaAsiento;
import co.edu.uq.cinemauq.core.domain.Usuario;
import co.edu.uq.cinemauq.core.managers.ReservasManager;
import co.edu.uq.cinemauq.ui.utils.Navegador;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;

import javafx.scene.control.Label;

import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controlador para la pantalla de Mis Tickets.
 * Muestra el historial de compras del usuario.
 */
public class MisTicketsController implements Initializable {

    @FXML
    private VBox ticketsContainer;

    @FXML
    private Label sinTicketsLabel;

    @FXML
    private Button btnVolver;

@Override
    public void initialize(URL location, ResourceBundle resources) {
        sinTicketsLabel.managedProperty().bind(sinTicketsLabel.visibleProperty());
        cargarTickets();

    }

    private void cargarTickets() {
        Usuario usuarioActual = Navegador.getUsuarioActual();

        if (usuarioActual == null) {
            sinTicketsLabel.setText("Debes iniciar sesión para ver tus tickets");
            sinTicketsLabel.setVisible(true);
            return;
        }

        // Actualizar header


        // Obtener reservas del usuario
        List<ReservaAsiento> reservas = ReservasManager.getInstance().getReservasPorUsuario(usuarioActual.getId());

        if (reservas.isEmpty()) {
            sinTicketsLabel.setText("No tienes tickets comprados aún");
            sinTicketsLabel.setVisible(true);
            return;
        }

        sinTicketsLabel.setVisible(false);

        // Agrupar reservas por función (mismo película, fecha, sala)
        Map<String, List<ReservaAsiento>> reservasPorFuncion = reservas.stream()
            .collect(Collectors.groupingBy(r -> r.getFuncionId()));

        // Mostrar cada grupo de reservas como un ticket
        DateTimeFormatter fechaFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm");

        for (Map.Entry<String, List<ReservaAsiento>> entry : reservasPorFuncion.entrySet()) {
            List<ReservaAsiento> reservasGrupo = entry.getValue();
            ReservaAsiento primera = reservasGrupo.get(0);

            VBox ticketCard = new VBox();
            ticketCard.setSpacing(10);
            ticketCard.getStyleClass().add("card");

            Label peliculaLabel = new Label("🎬 " + primera.getPeliculaTitulo());
            peliculaLabel.getStyleClass().add("card-title");

            Label fechaLabel = new Label("📅 " + primera.getFechaFuncion().format(fechaFormatter));
            fechaLabel.getStyleClass().add("helper-text");

            Label salaLabel = new Label("🏢 Sala " + primera.getSalaNumero());
            salaLabel.getStyleClass().add("helper-text");

            String asientos = reservasGrupo.stream()
                .map(ReservaAsiento::getAsientoId)
                .sorted()
                .collect(Collectors.joining(", "));
            Label asientosLabel = new Label("💺 Asientos: " + asientos);
            asientosLabel.getStyleClass().add("helper-text");

            double total = reservasGrupo.stream().mapToDouble(ReservaAsiento::getMontoPagado).sum();
            Label totalLabel = new Label("💰 Total: COP $" + String.format("%,.0f", total));
            totalLabel.getStyleClass().add("stat-value");

            Button verDetalleLabel = new Button("Ver detalle");
            verDetalleLabel.getStyleClass().add("boton-secundario");
            verDetalleLabel.setOnAction(event -> verDetalleTicket(reservasGrupo));

            Button cancelarLabel = new Button("Opciones de cancelación");
            cancelarLabel.getStyleClass().add("boton-peligro");
            cancelarLabel.setOnAction(event -> onCancelarCompras());

            ticketCard.getChildren().addAll(peliculaLabel, fechaLabel, salaLabel, asientosLabel, totalLabel, verDetalleLabel, cancelarLabel);
            Label estado = new Label(primera.getFechaFuncion().isBefore(java.time.LocalDateTime.now())
                    ? "Estado: función realizada" : "Estado: próxima función");
            estado.getStyleClass().add("estado-activo");
            ticketCard.getChildren().add(1, estado);
            for (javafx.scene.Node nodo : ticketCard.getChildren()) {
                if (nodo instanceof Label etiqueta) etiqueta.setWrapText(true);
            }
            ticketsContainer.getChildren().add(ticketCard);
        }
    }

    private void verDetalleTicket(List<ReservaAsiento> reservas) {
        // Establecer las reservas seleccionadas para que el TicketController las use
        Navegador.setReservasSeleccionadas(reservas);
        Navegador.navegarA("/ui/views/ticket.fxml", "CinemaUQ - Ticket de Compra");
    }

@FXML
    private void onVolver() {
        Navegador.navegarA("/ui/views/cartelera.fxml", "CinemaUQ - Cartelera de Películas");
    }

@FXML
    private void onCancelarCompras() {
        Navegador.navegarA("/ui/views/cancelaciones.fxml", "CinemaUQ - Cancelación de Compras");
    }
}
