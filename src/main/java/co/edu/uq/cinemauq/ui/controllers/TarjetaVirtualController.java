package co.edu.uq.cinemauq.ui.controllers;

import co.edu.uq.cinemauq.core.domain.Cliente;
import co.edu.uq.cinemauq.core.domain.TarjetaVirtual;
import co.edu.uq.cinemauq.core.domain.Transaccion;
import co.edu.uq.cinemauq.core.exceptions.SaldoInsuficienteException;
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
    private Label numeroTarjetaLabel;
    
    @FXML
    private Label saldoLabel;
    
    @FXML
    private Label estadoLabel;
    
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
        historialListView.setVisible(false);
    }
    
    public void cargarDatos(SesionCompra sesionCompra) {
        this.sesionCompra = sesionCompra;
        
        // Crear un cliente de prueba con tarjeta virtual
        this.cliente = new Cliente("CLI001", "Usuario Prueba", "usuario@test.com");
        this.tarjeta = cliente.getTarjeta();

        // Recargar saldo inicial para pruebas (COP)
        tarjeta.recargar(100000.0); // $100,000 COP
        
        mostrarInformacionTarjeta();
    }
    
    private void mostrarInformacionTarjeta() {
        numeroTarjetaLabel.setText("Tarjeta: " + tarjeta.getNumeroTarjeta());
        saldoLabel.setText(String.format("Saldo: COP $%,.2f", tarjeta.getSaldo()));

        if (tarjeta.isActiva()) {
            estadoLabel.setText("Estado: Activa");
            estadoLabel.setStyle("-fx-text-fill: #00ff00;");
        } else {
            estadoLabel.setText("Estado: Inactiva");
            estadoLabel.setStyle("-fx-text-fill: #ff0000;");
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
        
        try {
            if (tarjeta.validarSaldoSuficiente(totalCompra)) {
                tarjeta.deducir(totalCompra);
                mostrarInformacionTarjeta();
                System.out.println("Pago exitoso de COP $" + totalCompra);
                System.out.println(tarjeta.obtenerResumenTransacciones());

                // Navegar a cartelera después del pago
                sesionCompra.limpiar();
                Navegador.navegarA("/ui/views/cartelera.fxml", "CinemaUQ - Cartelera de Películas");
            } else {
                System.err.println("Saldo insuficiente. Saldo actual: COP $" + tarjeta.getSaldo() + ", requerido: COP $" + totalCompra);
            }
        } catch (SaldoInsuficienteException e) {
            System.err.println("Error: " + e.getMessage());
        } catch (TarjetaInactivaException e) {
            System.err.println("Error: " + e.getMessage());
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
