package co.edu.uq.cinemauq.ui.controllers;

import co.edu.uq.cinemauq.core.domain.Cliente;
import co.edu.uq.cinemauq.core.domain.TarjetaVirtual;
import co.edu.uq.cinemauq.core.domain.Transaccion;
import co.edu.uq.cinemauq.core.exceptions.TarjetaInactivaException;
import co.edu.uq.cinemauq.ui.models.SesionCompra;
import co.edu.uq.cinemauq.ui.utils.Navegador;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;

import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador para la pantalla de tarjeta virtual.
 * Permite consultar saldo, recargar y ver historial de transacciones.
 */
public class TarjetaVirtualController implements Initializable {

    @FXML
    private Label codigoTarjetaLabel;

    @FXML
    private Label nombreUsuarioLabel;

    @FXML
    private Label descuentosLabel;

    @FXML
    private Label saldoLabel;

    @FXML
    private Label estadoLabel;

    @FXML
    private Label totalAPagarLabel;

    @FXML
    private Label mensajeLabel;

    @FXML private Label mensajeRecargaLabel;
    @FXML private Label puntosPagoLabel;
    private final co.edu.uq.cinemauq.services.PagoCompraService pagoCompra = new co.edu.uq.cinemauq.services.PagoCompraService();

    @FXML
    private TextField recargaTextField;

    @FXML
    private Button btnRecargar;

    @FXML
    private Button btnPagar;

    @FXML
    private Button btnHistorial;

    @FXML
    private Button btnVolver;

    @FXML
    private VBox tarjetaCard;

    @FXML
    private ListView<String> historialListView;

    private SesionCompra sesionCompra;
    private Cliente cliente;
    private TarjetaVirtual tarjeta;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("=== INICIALIZANDO TARJETA VIRTUAL ===");
        historialListView.setVisible(false);
        historialListView.managedProperty().bind(historialListView.visibleProperty());
        mensajeLabel.managedProperty().bind(mensajeLabel.visibleProperty());

    }

    public void cargarDatos(SesionCompra sesionCompra) {
        this.sesionCompra = sesionCompra;

        System.out.println("=== CARGANDO TARJETA VIRTUAL ===");

        // Obtener el cliente actual del navegador
        if (!(Navegador.getUsuarioActual() instanceof Cliente autenticado)) {
            mensajeLabel.setText("Inicia sesión como cliente para consultar tu tarjeta.");
            mensajeLabel.setVisible(true);
            btnPagar.setDisable(true);
            btnRecargar.setDisable(true);
            btnHistorial.setDisable(true);
            return;
        }
        this.cliente = autenticado;

        this.tarjeta = cliente.getTarjeta();
        System.out.println("💳 Tarjeta: " + tarjeta.getNumeroTarjeta());
        System.out.println("💰 Saldo actual: $" + tarjeta.getSaldo());

        // Actualizar header

        // NO recargar automáticamente - el usuario debe hacerlo manualmente
        // if (tarjeta.getSaldo() == 0.0) {
        //     tarjeta.recargar(100000.0);
        //     System.out.println("Saldo inicial recargado: $100,000 COP");
        // }

        mostrarInformacionTarjeta();
    }

    private void mostrarInformacionTarjeta() {
        btnPagar.setDisable(sesionCompra == null || !sesionCompra.tieneAsientosSeleccionados());
        codigoTarjetaLabel.setText("CÓDIGO: " + tarjeta.getNumeroTarjeta());
        nombreUsuarioLabel.setText("Titular: " + cliente.getNombre());
        descuentosLabel.setText("Descuentos: " + (cliente.getTarjeta().getDescuentos().isEmpty() ? "Ninguno" : String.join(", ", cliente.getTarjeta().getDescuentos())));
        saldoLabel.setText(String.format("$%,.0f", tarjeta.getSaldo()));

        puntosPagoLabel.setText(sesionCompra == null ? "" : "Puntos preparados: " + sesionCompra.getPuntosAplicados()
                + " · descuento COP $" + sesionCompra.getValorDescuentoPuntos().toPlainString());
        // Mostrar total a pagar de la sesión de compra
        if (sesionCompra != null && sesionCompra.tieneAsientosSeleccionados()) {
            double totalAPagar = sesionCompra.calcularTotalAPagar().doubleValue();
            totalAPagarLabel.setText(String.format("COP $%,.0f", totalAPagar));
            System.out.println("💵 Total a pagar: COP $" + totalAPagar);
        } else {
            totalAPagarLabel.setText("No hay compra pendiente");
        }

        if (tarjeta.isActiva()) {
            estadoLabel.getStyleClass().removeAll("estado-activo", "estado-inactivo");
            estadoLabel.setText("✅ Activa");
            estadoLabel.getStyleClass().add("estado-activo");
        } else {
            estadoLabel.getStyleClass().removeAll("estado-activo", "estado-inactivo");
            estadoLabel.setText("❌ Inactiva");
            estadoLabel.getStyleClass().add("estado-inactivo");
        }
    }

    @FXML
    private void onRecargar() {
        try {
            String montoStr = recargaTextField.getText();
            if (montoStr == null || montoStr.trim().isEmpty()) {
                System.err.println("Ingrese un monto para recargar");
                mostrarMensajeRecarga("Ingresa un monto para recargar.", false);
                return;
            }

            double monto = Double.parseDouble(montoStr);
            tarjeta.recargar(monto);
            mostrarInformacionTarjeta();
            recargaTextField.clear();
            System.out.println("Recarga exitosa de $" + monto);
            mostrarMensajeRecarga(String.format("Recarga exitosa: COP $%,.2f", monto), true);
        } catch (NumberFormatException e) {
            System.err.println("Monto inválido");
            mostrarMensajeRecarga("Escribe un monto numérico válido.", false);
        } catch (TarjetaInactivaException e) {
            System.err.println("Error: " + e.getMessage());
            mostrarMensajeRecarga(e.getMessage(), false);
        } catch (co.edu.uq.cinemauq.core.exceptions.CinemaUQException e) {
            mostrarMensajeRecarga(e.getMessage(), false);
        }
    }

    private void mostrarMensajeRecarga(String texto, boolean exito) {
        mensajeRecargaLabel.setText(texto);
        mensajeRecargaLabel.getStyleClass().removeAll("mensaje-exito", "mensaje-error");
        mensajeRecargaLabel.getStyleClass().add(exito ? "mensaje-exito" : "mensaje-error");
        mensajeRecargaLabel.setVisible(true);
        mensajeRecargaLabel.setManaged(true);
    }

    @FXML
    private void onPagar() {
        mensajeLabel.setVisible(false);
        btnPagar.setDisable(true);
        try {
            if (Navegador.getUsuarioActual() != cliente) {
                throw new IllegalStateException("La sesión del cliente cambió. Vuelve a revisar tu compra.");
            }
            if (sesionCompra.getPagoConfirmado() != null) {
                Navegador.navegarA("/ui/views/ticket.fxml", "CinemaUQ - Ticket de Compra");
                return;
            }
            if (!sesionCompra.tieneFuncionSeleccionada() || !sesionCompra.tieneAsientosSeleccionados()) {
                throw new IllegalStateException("No hay una compra pendiente para pagar.");
            }
            // El servicio confirma dinero, puntos, compra, reservas y ocupación una sola vez.
            pagoCompra.pagar(cliente, sesionCompra);
            sesionCompra.limpiar();
            Navegador.navegarA("/ui/views/ticket.fxml", "CinemaUQ - Ticket de Compra");
        } catch (RuntimeException e) {
            mensajeLabel.setText(sesionCompra != null && sesionCompra.getPagoConfirmado() != null
                    ? "El pago ya fue confirmado. No vuelvas a pagar. " + e.getMessage()
                    : "No se confirmó el pago: " + e.getMessage());
            mensajeLabel.getStyleClass().setAll("mensaje-error");
            mensajeLabel.setVisible(true);
            mostrarInformacionTarjeta();
        }
    }
    @FXML
    private void onHistorial() {
        List<Transaccion> transacciones = tarjeta.obtenerHistorial();
        historialListView.getItems().clear();

        if (transacciones.isEmpty()) {
            historialListView.getItems().add("No hay transacciones");
        } else {
            for (Transaccion t : transacciones) {
                String item = String.format("%s - %s: $%.2f (Saldo: $%.2f)",
                    t.fecha().toString().substring(0, 16),
                    t.tipo(),
                    t.monto(),
                    t.saldoPosterior());
                historialListView.getItems().add(item);
            }
        }

        historialListView.setVisible(!historialListView.isVisible());
    }

    @FXML
    private void onVolver() {
        Navegador.navegarA("/ui/views/compra.fxml", "CinemaUQ - Resumen de Compra");
    }

    public SesionCompra getSesionCompra() {
        return sesionCompra;
    }
}
