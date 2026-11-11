package co.edu.uq.cinemauq.ui.controllers;

import co.edu.uq.cinemauq.core.domain.Cliente;
import co.edu.uq.cinemauq.core.domain.PuntoCinemaUQ;
import co.edu.uq.cinemauq.core.domain.Usuario;
import co.edu.uq.cinemauq.core.exceptions.PuntosException;
import co.edu.uq.cinemauq.services.PuntosService;
import co.edu.uq.cinemauq.ui.models.SesionCompra;
import co.edu.uq.cinemauq.ui.utils.Navegador;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.ResourceBundle;

/**
 * Vista de consulta y preparación de redención de puntos CinemaUQ.
 * La redención se deja pendiente en SesionCompra y solo se consume al confirmar el pago.
 */
public class PuntosController implements Initializable {
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML private Label clienteLabel;
    @FXML private Label disponiblesLabel;
    @FXML private Label vencidosLabel;
    @FXML private Label equivalenteLabel;
    @FXML private Label proximoVencimientoLabel;
    @FXML private Label compraPendienteLabel;
    @FXML private Label valorRedencionLabel;
    @FXML private Label mensajeLabel;
    @FXML private ListView<String> lotesListView;
    @FXML private TextField puntosRedimirField;
    @FXML private Button btnAplicar;
    @FXML private Button btnQuitar;

    private final PuntosService puntosService = new PuntosService();
    private SesionCompra sesionCompra;
    private Cliente cliente;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        puntosRedimirField.textProperty().addListener((obs, anterior, actual) -> actualizarVistaPrevia(actual));
        mensajeLabel.setVisible(false);
    }

    public void cargarDatos(SesionCompra sesionCompra) {
        this.sesionCompra = sesionCompra;
        Usuario usuario = Navegador.getUsuarioActual();
        if (!(usuario instanceof Cliente clienteActual)) {
            mostrarError("La consulta de puntos está disponible únicamente para clientes.");
            btnAplicar.setDisable(true);
            btnQuitar.setDisable(true);
            return;
        }

        this.cliente = clienteActual;
        clienteLabel.setText(cliente.getNombre() + " · " + cliente.getId());
        refrescar();
    }

    private void refrescar() {
        puntosService.actualizarEstados(cliente);
        int disponibles = puntosService.obtenerPuntosDisponibles(cliente);
        int vencidos = puntosService.obtenerPuntosVencidos(cliente);

        disponiblesLabel.setText(String.format("%,d pts", disponibles));
        vencidosLabel.setText(String.format("%,d pts", vencidos));
        equivalenteLabel.setText(String.format("$%,.2f", puntosService.calcularValorRedencion(disponibles)));
        proximoVencimientoLabel.setText(
                puntosService.obtenerProximoVencimiento(cliente)
                        .map(fecha -> fecha.format(FECHA))
                        .orElse("Sin puntos vigentes")
        );

        lotesListView.getItems().clear();
        cliente.getPuntosAcumulados().stream()
                .sorted(Comparator.comparing(PuntoCinemaUQ::getFechaVencimiento)
                        .thenComparing(PuntoCinemaUQ::getFechaObtencion))
                .forEach(punto -> lotesListView.getItems().add(formatearLote(punto)));

        if (lotesListView.getItems().isEmpty()) {
            lotesListView.getItems().add("Aún no tienes acumulaciones de puntos.");
        }

        boolean hayCompra = sesionCompra != null && sesionCompra.tieneAsientosSeleccionados();
        if (hayCompra) {
            String redencion = sesionCompra.tieneRedencionPuntos()
                    ? String.format(" · Redención preparada: %,d pts (-$%,.2f)",
                        sesionCompra.getPuntosARedimir(), sesionCompra.getValorRedencionPuntos())
                    : "";
            compraPendienteLabel.setText(String.format("Compra pendiente: $%,.2f%s",
                    sesionCompra.calcularSubtotalSinPuntos(), redencion));
        } else {
            compraPendienteLabel.setText("No hay una compra pendiente. Puedes consultar tus puntos y fechas de vencimiento.");
        }

        btnAplicar.setDisable(!hayCompra || disponibles == 0);
        btnQuitar.setDisable(sesionCompra == null || !sesionCompra.tieneRedencionPuntos());
    }

    private String formatearLote(PuntoCinemaUQ punto) {
        String cantidad = punto.getCantidad() == punto.getCantidadOriginal()
                ? String.format("%,d pts", punto.getCantidad())
                : String.format("%,d de %,d pts", punto.getCantidad(), punto.getCantidadOriginal());
        return String.format("%s  |  Obtenidos: %s  |  Vencen: %s  |  %s",
                cantidad,
                punto.getFechaObtencion().format(FECHA),
                punto.getFechaVencimiento().format(FECHA),
                punto.getNombreEstado());
    }

    private void actualizarVistaPrevia(String texto) {
        if (texto == null || texto.isBlank()) {
            valorRedencionLabel.setText("$0.00");
            return;
        }
        try {
            int puntos = Integer.parseInt(texto.trim());
            if (puntos <= 0) {
                valorRedencionLabel.setText("$0.00");
                return;
            }
            valorRedencionLabel.setText(String.format("$%,.2f", puntosService.calcularValorRedencion(puntos)));
        } catch (NumberFormatException e) {
            valorRedencionLabel.setText("Valor inválido");
        }
    }

    @FXML
    private void onAplicarRedencion() {
        ocultarMensaje();
        if (sesionCompra == null || !sesionCompra.tieneAsientosSeleccionados()) {
            mostrarError("Debes tener una compra en curso para aplicar la redención.");
            return;
        }

        try {
            int puntos = Integer.parseInt(puntosRedimirField.getText().trim());
            if (!puntosService.verificarDisponibilidad(cliente, puntos)) {
                mostrarError("No tienes suficientes puntos vigentes para esa redención.");
                return;
            }

            double valor = puntosService.calcularValorRedencion(puntos);
            if (valor > sesionCompra.calcularSubtotalSinPuntos()) {
                mostrarError("La redención no puede superar el valor total de la compra.");
                return;
            }

            sesionCompra.aplicarRedencionPuntos(puntos, valor);
            mostrarExito(String.format("Redención preparada: %,d puntos = $%,.2f. Se consumirá al confirmar el pago.", puntos, valor));
            refrescar();
        } catch (NumberFormatException e) {
            mostrarError("Ingresa una cantidad entera de puntos.");
        } catch (PuntosException | IllegalArgumentException e) {
            mostrarError(e.getMessage());
        }
    }

    @FXML
    private void onQuitarRedencion() {
        if (sesionCompra != null) {
            sesionCompra.quitarRedencionPuntos();
            puntosRedimirField.clear();
            mostrarExito("Se retiró la redención pendiente de la compra.");
            refrescar();
        }
    }

    @FXML
    private void onVolver() {
        if (sesionCompra != null && sesionCompra.tieneAsientosSeleccionados()) {
            Navegador.navegarA("/ui/views/compra.fxml", "CinemaUQ - Resumen de Compra");
        } else {
            Navegador.navegarA("/ui/views/mi_cuenta.fxml", "CinemaUQ - Mi Cuenta");
        }
    }

    private void mostrarError(String mensaje) {
        mensajeLabel.setText("❌ " + mensaje);
        mensajeLabel.setStyle("-fx-text-fill: #e94560;");
        mensajeLabel.setVisible(true);
    }

    private void mostrarExito(String mensaje) {
        mensajeLabel.setText("✅ " + mensaje);
        mensajeLabel.setStyle("-fx-text-fill: #2e7d32;");
        mensajeLabel.setVisible(true);
    }

    private void ocultarMensaje() {
        mensajeLabel.setVisible(false);
    }
}
