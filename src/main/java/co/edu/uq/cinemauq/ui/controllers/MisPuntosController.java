package co.edu.uq.cinemauq.ui.controllers;

import co.edu.uq.cinemauq.core.domain.Cliente;
import co.edu.uq.cinemauq.core.domain.PuntoCinemaUQ;
import co.edu.uq.cinemauq.core.exceptions.UsuarioException;
import co.edu.uq.cinemauq.patterns.state.EstadoPunto;
import co.edu.uq.cinemauq.patterns.state.PuntoDisponible;
import co.edu.uq.cinemauq.patterns.state.PuntoVencido;
import co.edu.uq.cinemauq.services.PuntosService;
import co.edu.uq.cinemauq.ui.utils.Navegador;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Locale;

/** Presentación de lotes; toda redención se delega al servicio existente. */
public class MisPuntosController {
    @FXML private Label disponiblesLabel;
    @FXML private Label vencidosLabel;
    @FXML private Label proximoVencimientoLabel;
    @FXML private Label equivalenteLabel;
    @FXML private Label disponibilidadLabel;
    @FXML private Label vistaPreviaLabel;
    @FXML private Label mensajeLabel;
    @FXML private TextField cantidadField;
    @FXML private Button btnRedimir;
    @FXML private TableView<PuntoCinemaUQ> lotesTable;
    @FXML private TableColumn<PuntoCinemaUQ, String> originalesColumn;
    @FXML private TableColumn<PuntoCinemaUQ, String> restantesColumn;
    @FXML private TableColumn<PuntoCinemaUQ, String> obtencionColumn;
    @FXML private TableColumn<PuntoCinemaUQ, String> vencimientoColumn;
    @FXML private TableColumn<PuntoCinemaUQ, String> estadoColumn;

    private final PuntosService puntosService = new PuntosService();
    private final DateTimeFormatter fechas = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final NumberFormat moneda = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CO"));
    private Cliente cliente;
    private long disponibles;

    @FXML
    private void initialize() {
        moneda.setMinimumFractionDigits(2);
        moneda.setMaximumFractionDigits(2);
        originalesColumn.setCellValueFactory(c -> texto(c.getValue().getCantidad()));
        restantesColumn.setCellValueFactory(c -> texto(c.getValue().getCantidadRestante()));
        obtencionColumn.setCellValueFactory(c -> texto(fechas.format(c.getValue().getFechaObtencion())));
        vencimientoColumn.setCellValueFactory(c -> texto(fechas.format(c.getValue().getFechaVencimiento())));
        estadoColumn.setCellValueFactory(c -> texto(nombreEstado(c.getValue())));
        estadoColumn.setCellFactory(columna -> new TableCell<>() {
            @Override
            protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);
                getStyleClass().removeAll("estado-disponible", "estado-vencido", "estado-redimido");
                setText(empty ? null : estado);
                if (!empty && estado != null) {
                    getStyleClass().add("estado-" + estado.toLowerCase(Locale.ROOT));
                }
            }
        });
        lotesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        lotesTable.setPlaceholder(new Label("Aún no tienes lotes de puntos registrados."));
        cantidadField.textProperty().addListener((observable, anterior, actual) -> actualizarVistaPrevia());
        if (Navegador.getUsuarioActual() instanceof Cliente autenticado) {
            cliente = autenticado;
        }
        actualizarPantalla();
        if (cliente == null) {
            mostrarMensaje("Inicia sesión como cliente para consultar y redimir tus puntos.", false);
        }
    }

    private ReadOnlyStringWrapper texto(Object valor) {
        return new ReadOnlyStringWrapper(String.valueOf(valor));
    }

    private String nombreEstado(PuntoCinemaUQ lote) {
        EstadoPunto estado = lote.getEstado();
        if (estado instanceof PuntoDisponible) return "DISPONIBLE";
        if (estado instanceof PuntoVencido) return "VENCIDO";
        return "REDIMIDO";
    }

    private void actualizarPantalla() {
        disponibles = 0;
        long vencidos = 0;
        LocalDate proximo = null;
        if (cliente != null) {
            for (PuntoCinemaUQ lote : cliente.getPuntosAcumulados()) {
                EstadoPunto estado = lote.getEstado();
                if (estado instanceof PuntoDisponible && lote.getCantidadRestante() > 0) {
                    disponibles += lote.getCantidadRestante();
                    if (proximo == null || lote.getFechaVencimiento().isBefore(proximo)) {
                        proximo = lote.getFechaVencimiento();
                    }
                } else if (estado instanceof PuntoVencido) {
                    vencidos += lote.getCantidadRestante();
                }
            }
            lotesTable.getItems().setAll(cliente.getPuntosAcumulados().stream()
                    .sorted(Comparator.comparing(PuntoCinemaUQ::getFechaVencimiento)).toList());
        } else {
            lotesTable.getItems().clear();
        }
        lotesTable.refresh();
        disponiblesLabel.setText(String.valueOf(disponibles));
        vencidosLabel.setText(String.valueOf(vencidos));
        proximoVencimientoLabel.setText(proximo == null ? "Sin próximos vencimientos" : fechas.format(proximo));
        equivalenteLabel.setText(formatearValor(disponibles));
        disponibilidadLabel.setText(disponibles == 0
                ? "No tienes puntos disponibles para redimir."
                : "Se utilizarán primero los lotes con vencimiento más próximo.");
        cantidadField.setDisable(cliente == null || disponibles == 0);
        actualizarVistaPrevia();
    }

    private String formatearValor(long puntos) {
        return moneda.format(BigDecimal.valueOf(puntos).multiply(new BigDecimal("0.05"))) + " COP";
    }

    private Integer cantidadSolicitada() {
        String entrada = cantidadField.getText() == null ? "" : cantidadField.getText().trim();
        if (!entrada.matches("[0-9]+")) return null;
        try {
            int cantidad = Integer.parseInt(entrada);
            return cantidad > 0 ? cantidad : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void actualizarVistaPrevia() {
        Integer cantidad = cantidadSolicitada();
        vistaPreviaLabel.setText("Valor equivalente: " + formatearValor(cantidad == null ? 0 : cantidad));
        btnRedimir.setDisable(cliente == null || disponibles == 0);
        cantidadField.getStyleClass().remove("puntos-campo-error");
        mensajeLabel.setVisible(false);
        mensajeLabel.setManaged(false);
        if (cliente == null || disponibles == 0 || cantidadField.getText().isBlank()) return;
        if (cantidad == null) {
            cantidadField.getStyleClass().add("puntos-campo-error");
            mostrarMensaje("Ingresa una cantidad entera de puntos mayor que cero.", false);
        } else if (cantidad > disponibles) {
            cantidadField.getStyleClass().add("puntos-campo-error");
            mostrarMensaje("Puntos insuficientes. Tienes " + disponibles + " puntos disponibles.", false);
        }
    }

    @FXML
    private void onRedimir() {
        if (cliente == null || Navegador.getUsuarioActual() != cliente) {
            mostrarMensaje("La sesión del cliente no está disponible. Vuelve a iniciar sesión.", false);
            return;
        }
        Integer cantidad = cantidadSolicitada();
        if (cantidad == null) {
            mostrarMensaje("Ingresa una cantidad entera de puntos mayor que cero.", false);
            return;
        }
        try {
            if (!puntosService.verificarDisponibilidad(cliente, cantidad)) {
                actualizarPantalla();
                mostrarMensaje("Puntos insuficientes para redimir la cantidad solicitada.", false);
                return;
            }
            BigDecimal valor = puntosService.redimirPuntos(cliente, cantidad);
            cantidadField.clear();
            actualizarPantalla();
            mostrarMensaje("Redimiste " + cantidad + " puntos. Valor equivalente: "
                    + moneda.format(valor) + " COP. Este valor no se ha abonado a tu tarjeta ni aplicado a una compra.", true);
        } catch (UsuarioException e) {
            actualizarPantalla();
            mostrarMensaje(e.getMessage(), false);
        }
    }

    private void mostrarMensaje(String mensaje, boolean exito) {
        mensajeLabel.getStyleClass().removeAll("mensaje-exito", "mensaje-error");
        mensajeLabel.getStyleClass().add(exito ? "mensaje-exito" : "mensaje-error");
        mensajeLabel.setText(mensaje);
        mensajeLabel.setVisible(true);
        mensajeLabel.setManaged(true);
    }

    @FXML
    private void onVolver() {
        Navegador.navegarA("/ui/views/mi_cuenta.fxml", "CinemaUQ - Mi Cuenta");
    }
}
