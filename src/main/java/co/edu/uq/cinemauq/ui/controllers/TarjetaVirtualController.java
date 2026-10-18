package co.edu.uq.cinemauq.ui.controllers;

import co.edu.uq.cinemauq.core.domain.Cliente;
import co.edu.uq.cinemauq.core.domain.ReservaAsiento;
import co.edu.uq.cinemauq.core.domain.TarjetaVirtual;
import co.edu.uq.cinemauq.core.domain.Transaccion;
import co.edu.uq.cinemauq.core.exceptions.SaldoInsuficienteException;
import co.edu.uq.cinemauq.core.exceptions.TarjetaInactivaException;
import co.edu.uq.cinemauq.core.managers.AsientosManager;
import co.edu.uq.cinemauq.core.managers.ReservasManager;
import co.edu.uq.cinemauq.ui.models.SesionCompra;
import co.edu.uq.cinemauq.ui.utils.FuncionesGenerator;
import co.edu.uq.cinemauq.ui.utils.Navegador;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

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
    }
    
    public void cargarDatos(SesionCompra sesionCompra) {
        this.sesionCompra = sesionCompra;

        System.out.println("=== CARGANDO TARJETA VIRTUAL ===");

        // Obtener el cliente actual del navegador
        this.cliente = (Cliente) Navegador.getUsuarioActual();
        if (this.cliente == null) {
            System.err.println("❌ No hay usuario actual, creando cliente de prueba");
            // Crear un cliente de prueba con tarjeta virtual
            this.cliente = new Cliente("CLI001", "Usuario Prueba", "usuario@test.com");
        } else {
            System.out.println("✅ Usuario actual: " + cliente.getNombre());
        }

        this.tarjeta = cliente.getTarjeta();
        System.out.println("💳 Tarjeta: " + tarjeta.getNumeroTarjeta());
        System.out.println("💰 Saldo actual: $" + tarjeta.getSaldo());

        // NO recargar automáticamente - el usuario debe hacerlo manualmente
        // if (tarjeta.getSaldo() == 0.0) {
        //     tarjeta.recargar(100000.0);
        //     System.out.println("Saldo inicial recargado: $100,000 COP");
        // }

        mostrarInformacionTarjeta();
    }

    private void mostrarInformacionTarjeta() {
        codigoTarjetaLabel.setText("CÓDIGO: " + tarjeta.getNumeroTarjeta());
        nombreUsuarioLabel.setText("Titular: " + cliente.getNombre());
        descuentosLabel.setText("Descuentos: " + (cliente.getTarjeta().getDescuentos().isEmpty() ? "Ninguno" : String.join(", ", cliente.getTarjeta().getDescuentos())));
        saldoLabel.setText(String.format("$%,.0f", tarjeta.getSaldo()));

        // Mostrar total a pagar de la sesión de compra
        if (sesionCompra != null) {
            double totalAPagar = sesionCompra.calcularTotal();
            totalAPagarLabel.setText(String.format("COP $%,.0f", totalAPagar));
            System.out.println("💵 Total a pagar: COP $" + totalAPagar);
        } else {
            totalAPagarLabel.setText("No hay compra pendiente");
        }

        if (tarjeta.isActiva()) {
            estadoLabel.setText("✅ Activa");
            estadoLabel.setStyle("-fx-text-fill: #4CAF50;");
        } else {
            estadoLabel.setText("❌ Inactiva");
            estadoLabel.setStyle("-fx-text-fill: #e94560;");
        }
    }
    
    @FXML
    private void onRecargar() {
        try {
            String montoStr = recargaTextField.getText();
            if (montoStr == null || montoStr.trim().isEmpty()) {
                System.err.println("Ingrese un monto para recargar");
                return;
            }
            
            double monto = Double.parseDouble(montoStr);
            tarjeta.recargar(monto);
            mostrarInformacionTarjeta();
            recargaTextField.clear();
            System.out.println("Recarga exitosa de $" + monto);
        } catch (NumberFormatException e) {
            System.err.println("Monto inválido");
        } catch (TarjetaInactivaException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    @FXML
    private void onPagar() {
        double totalCompra = sesionCompra.calcularTotal();

        System.out.println("=== PROCESANDO PAGO ===");
        System.out.println("Saldo antes del pago: $" + tarjeta.getSaldo());
        System.out.println("Monto a pagar: $" + totalCompra);

        try {
            if (tarjeta.validarSaldoSuficiente(totalCompra)) {
                tarjeta.deducir(totalCompra);
                mostrarInformacionTarjeta();
                System.out.println("✅ Pago exitoso de COP $" + totalCompra);
                System.out.println("Saldo después del pago: $" + tarjeta.getSaldo());
                System.out.println(tarjeta.obtenerResumenTransacciones());

                // Marcar asientos como ocupados en el AsientosManager
                if (sesionCompra.tieneFuncionSeleccionada() && !sesionCompra.getAsientosSeleccionados().isEmpty()) {
                    String funcionId = sesionCompra.getFuncionSeleccionada().getId();
                    Set<String> asientos = new HashSet<>(sesionCompra.getAsientosSeleccionados());
                    AsientosManager.getInstance().marcarAsientosOcupados(funcionId, asientos);
                    System.out.println("✅ Asientos marcados como ocupados: " + asientos);

                    // Crear reservas para cada asiento comprado
                    for (String asientoId : asientos) {
                        ReservaAsiento reserva = new ReservaAsiento(
                            cliente.getId(),
                            cliente.getNombre(),
                            funcionId,
                            sesionCompra.getFuncionSeleccionada().getPelicula().getTitulo(),
                            sesionCompra.getFuncionSeleccionada().getSala().getNumero(),
                            sesionCompra.getFuncionSeleccionada().getFechaHora(),
                            asientoId,
                            totalCompra / asientos.size() // Dividir el total entre los asientos
                        );
                        ReservasManager.getInstance().agregarReserva(reserva);
                    }
                }

                // Navegar a ticket después del pago
                sesionCompra.limpiar();
                Navegador.navegarA("/ui/views/ticket.fxml", "CinemaUQ - Ticket de Compra");
            } else {
                System.err.println("❌ Saldo insuficiente. Saldo actual: COP $" + tarjeta.getSaldo() + ", requerido: COP $" + totalCompra);
            }
        } catch (SaldoInsuficienteException e) {
            System.err.println("❌ Error: " + e.getMessage());
        } catch (TarjetaInactivaException e) {
            System.err.println("❌ Error: " + e.getMessage());
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
