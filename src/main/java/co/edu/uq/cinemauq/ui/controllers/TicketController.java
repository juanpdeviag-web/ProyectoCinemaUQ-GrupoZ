package co.edu.uq.cinemauq.ui.controllers;

import co.edu.uq.cinemauq.core.domain.ReservaAsiento;
import co.edu.uq.cinemauq.core.managers.ReservasManager;
import co.edu.uq.cinemauq.ui.utils.FuncionesGenerator;
import co.edu.uq.cinemauq.ui.utils.Navegador;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controlador para la pantalla de ticket de compra.
 * Muestra los asientos adquiridos en formato de ticket.
 */
public class TicketController implements Initializable {

    @FXML
    private Label peliculaLabel;

    @FXML
    private Label fechaLabel;

    @FXML
    private Label horaLabel;

    @FXML
    private Label salaLabel;

    @FXML
    private Label asientosLabel;

    @FXML
    private Label usuarioLabel;

    @FXML
    private Label totalLabel;

    @FXML
    private Label fechaCompraLabel;

    @FXML
    private Button btnVolver;

    private List<ReservaAsiento> reservas;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // La inicialización se completa en cargarDatos()
    }

    /**
     * Carga los datos de las reservas y muestra el ticket.
     */
    public void cargarDatos(List<ReservaAsiento> reservas) {
        this.reservas = reservas;

        if (reservas == null || reservas.isEmpty()) {
            System.err.println("Error: No hay reservas para mostrar");
            return;
        }

        System.out.println("=== MOSTRANDO TICKET ===");
        System.out.println("Reservas: " + reservas.size());
        reservas.forEach(r -> System.out.println("  - " + r));

        // Usar la primera reserva para la información común
        ReservaAsiento primeraReserva = reservas.get(0);

        // Mostrar información de la película
        peliculaLabel.setText(primeraReserva.getPeliculaTitulo());

        // Mostrar fecha y hora de la función
        DateTimeFormatter fechaFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter horaFormatter = DateTimeFormatter.ofPattern("HH:mm");
        fechaLabel.setText("Fecha: " + primeraReserva.getFechaFuncion().format(fechaFormatter));
        horaLabel.setText("Hora: " + primeraReserva.getFechaFuncion().format(horaFormatter));

        // Mostrar sala
        salaLabel.setText("Sala: " + primeraReserva.getSalaNumero());

        // Mostrar todos los asientos
        String asientos = reservas.stream()
            .map(ReservaAsiento::getAsientoId)
            .sorted()
            .collect(Collectors.joining(", "));
        asientosLabel.setText(asientos);

        // Mostrar usuario
        usuarioLabel.setText("Cliente: " + primeraReserva.getUsuarioNombre());

        // Calcular total sumando todos los asientos
        double total = reservas.stream().mapToDouble(ReservaAsiento::getMontoPagado).sum();
        totalLabel.setText(String.format("Total: COP $%,.0f", total));

        // Mostrar fecha de compra (usar la última reserva)
        ReservaAsiento ultimaReserva = reservas.get(reservas.size() - 1);
        DateTimeFormatter compraFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm");
        fechaCompraLabel.setText("Compra realizada: " + ultimaReserva.getFechaCompra().format(compraFormatter));
    }

    /**
     * Carga los datos de las últimas reservas del usuario actual (mismo función).
     */
    public void cargarUltimaReserva() {
        String usuarioId = Navegador.getUsuarioActual() != null ? Navegador.getUsuarioActual().getId() : null;
        if (usuarioId == null) {
            System.err.println("Error: No hay usuario actual");
            return;
        }

        List<ReservaAsiento> todasLasReservas = ReservasManager.getInstance().getReservasPorUsuario(usuarioId);
        if (todasLasReservas.isEmpty()) {
            System.err.println("Error: No hay reservas para el usuario");
            return;
        }

        // Obtener todas las reservas de la misma función (las más recientes)
        // Agrupar por función y tomar el grupo más reciente
        String ultimaFuncionId = todasLasReservas.get(todasLasReservas.size() - 1).getFuncionId();
        List<ReservaAsiento> reservasMismaFuncion = todasLasReservas.stream()
            .filter(r -> r.getFuncionId().equals(ultimaFuncionId))
            .collect(Collectors.toList());

        cargarDatos(reservasMismaFuncion);
    }

    @FXML
    private void onVolver() {
        Navegador.navegarA("/ui/views/cartelera.fxml", "CinemaUQ - Cartelera de Películas");
    }
}
