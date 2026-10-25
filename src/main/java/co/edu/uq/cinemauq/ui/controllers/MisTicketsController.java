package co.edu.uq.cinemauq.ui.controllers;

import co.edu.uq.cinemauq.core.domain.ReservaAsiento;
import co.edu.uq.cinemauq.core.domain.Usuario;
import co.edu.uq.cinemauq.core.managers.ReservasManager;
import co.edu.uq.cinemauq.ui.utils.Navegador;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
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

    @FXML
    private Button btnSalir;

    @FXML
    private VBox perfilContainer;

    @FXML
    private Label perfilNombreLabel;

    @FXML
    private Label perfilRolLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cargarTickets();
        configurarMenuPerfil();
    }

    private void cargarTickets() {
        Usuario usuarioActual = Navegador.getUsuarioActual();

        if (usuarioActual == null) {
            sinTicketsLabel.setText("Debes iniciar sesión para ver tus tickets");
            sinTicketsLabel.setVisible(true);
            return;
        }

        // Actualizar header
        perfilNombreLabel.setText(usuarioActual.getNombre());
        if (usuarioActual instanceof co.edu.uq.cinemauq.core.domain.Administrador) {
            perfilRolLabel.setText("Rol: Administrador");
        } else {
            perfilRolLabel.setText("Rol: Cliente");
        }

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
            ticketCard.setStyle("-fx-background-color: #FAF8F5; -fx-background-radius: 10; -fx-padding: 15; -fx-border-color: #C4A77D; -fx-border-radius: 10; -fx-border-width: 1;");

            Label peliculaLabel = new Label("🎬 " + primera.getPeliculaTitulo());
            peliculaLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #4A3F35;");

            Label fechaLabel = new Label("📅 " + primera.getFechaFuncion().format(fechaFormatter));
            fechaLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #8B7355;");

            Label salaLabel = new Label("🏢 Sala " + primera.getSalaNumero());
            salaLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #8B7355;");

            String asientos = reservasGrupo.stream()
                .map(ReservaAsiento::getAsientoId)
                .sorted()
                .collect(Collectors.joining(", "));
            Label asientosLabel = new Label("💺 Asientos: " + asientos);
            asientosLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #8B7355;");

            double total = reservasGrupo.stream().mapToDouble(ReservaAsiento::getMontoPagado).sum();
            Label totalLabel = new Label("💰 Total: COP $" + String.format("%,.0f", total));
            totalLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #C4A77D;");

            Label verDetalleLabel = new Label("👁️ Ver detalle");
            verDetalleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #4A3F35; -fx-underline: true; -fx-cursor: hand;");
            verDetalleLabel.setOnMouseClicked(event -> verDetalleTicket(reservasGrupo));

            ticketCard.getChildren().addAll(peliculaLabel, fechaLabel, salaLabel, asientosLabel, totalLabel, verDetalleLabel);
            ticketsContainer.getChildren().add(ticketCard);
        }
    }

    private void verDetalleTicket(List<ReservaAsiento> reservas) {
        // Establecer las reservas seleccionadas para que el TicketController las use
        Navegador.setReservasSeleccionadas(reservas);
        Navegador.navegarA("/ui/views/ticket.fxml", "CinemaUQ - Ticket de Compra");
    }

    private void configurarMenuPerfil() {
        ContextMenu menu = new ContextMenu();

        MenuItem miCuentaItem = new MenuItem("👤 Mi Cuenta");
        miCuentaItem.setOnAction(event -> onMiCuenta());

        MenuItem tarjetaVirtualItem = new MenuItem("💳 Tarjeta Virtual");
        tarjetaVirtualItem.setOnAction(event -> onTarjetaVirtual());

        MenuItem misTicketsItem = new MenuItem("🎟️ Mis Tickets");
        misTicketsItem.setOnAction(event -> onMisTickets());

        MenuItem carteleraItem = new MenuItem("🎬 Ir a Cartelera");
        carteleraItem.setOnAction(event -> onIrACartelera());

        menu.getItems().addAll(miCuentaItem, tarjetaVirtualItem, misTicketsItem, carteleraItem);

        perfilContainer.setOnMouseClicked(event -> {
            menu.show(perfilContainer, event.getScreenX(), event.getScreenY());
        });
    }

    @FXML
    private void onVolver() {
        Navegador.navegarA("/ui/views/cartelera.fxml", "CinemaUQ - Cartelera de Películas");
    }

    @FXML
    private void onSalir() {
        System.exit(0);
    }

    @FXML
    private void onMiCuenta() {
        Navegador.navegarA("/ui/views/mi_cuenta.fxml", "CinemaUQ - Mi Cuenta");
    }

    @FXML
    private void onTarjetaVirtual() {
        Usuario usuarioActual = Navegador.getUsuarioActual();
        if (usuarioActual instanceof co.edu.uq.cinemauq.core.domain.Cliente) {
            Navegador.navegarA("/ui/views/tarjeta_virtual.fxml", "CinemaUQ - Tarjeta Virtual");
        }
    }

    @FXML
    private void onMisTickets() {
        // Ya estamos en Mis Tickets
    }

    @FXML
    private void onIrACartelera() {
        Navegador.navegarA("/ui/views/cartelera.fxml", "CinemaUQ - Cartelera de Películas");
    }
}
