package co.edu.uq.cinemauq.ui.controllers;

import co.edu.uq.cinemauq.core.domain.Administrador;
import co.edu.uq.cinemauq.core.domain.Cliente;
import co.edu.uq.cinemauq.core.domain.Usuario;
import co.edu.uq.cinemauq.ui.utils.Navegador;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;

import javafx.scene.control.Label;

import javafx.scene.layout.VBox;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.ResourceBundle;

/**
 * Controlador para la pantalla de Mi Cuenta.
 * Muestra la información del perfil del usuario.
 */
public class MiCuentaController implements Initializable {

    @FXML
    private Label idLabel;

    @FXML
    private Label nombreLabel;

    @FXML
    private Label emailLabel;

    @FXML
    private Label rolLabel;

    @FXML
    private Label saldoLabel;

    @FXML
    private VBox saldoContainer;

    @FXML
    private Button btnVolver;

    @FXML
    private Button btnMisPuntos;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cargarInformacionUsuario();

    }

    private void cargarInformacionUsuario() {
        Usuario usuarioActual = Navegador.getUsuarioActual();
        boolean esCliente = usuarioActual instanceof Cliente;
        btnMisPuntos.setVisible(esCliente);
        btnMisPuntos.setManaged(esCliente);

        if (usuarioActual != null) {
            idLabel.setText(usuarioActual.getId());
            nombreLabel.setText(usuarioActual.getNombre());
            emailLabel.setText(usuarioActual.getCorreo());

            // Actualizar header
            if (usuarioActual instanceof Administrador) {
                rolLabel.setText("Administrador");
            } else if (usuarioActual instanceof Cliente) {
                rolLabel.setText("Cliente");
                Cliente cliente = (Cliente) usuarioActual;
                saldoContainer.setVisible(true);
                saldoContainer.setManaged(true);
                DecimalFormat df = new DecimalFormat("#,##0.00");
                saldoLabel.setText("$" + df.format(cliente.getTarjeta().getSaldo()) + " COP");
            }
        }
    }

    @FXML
    private void onVolver() {
        Navegador.navegarA("/ui/views/cartelera.fxml", "CinemaUQ - Cartelera de Películas");
    }

    @FXML
    private void onMisPuntos() {
        if (Navegador.getUsuarioActual() instanceof Cliente) {
            Navegador.navegarA("/ui/views/mis_puntos.fxml", "CinemaUQ - Mis puntos");
        }
    }

    @FXML
    private void onSalir() {
        System.exit(0);
    }

    @FXML
    private void onMiCuenta() {
        // Ya estamos en Mi Cuenta
    }

    @FXML
    private void onTarjetaVirtual() {
        Usuario usuarioActual = Navegador.getUsuarioActual();
        if (usuarioActual instanceof Cliente) {
            Navegador.navegarA("/ui/views/tarjeta_virtual.fxml", "CinemaUQ - Tarjeta Virtual");
        }
    }

    @FXML
    private void onMisTickets() {
        Navegador.navegarA("/ui/views/mis_tickets.fxml", "CinemaUQ - Mis Tickets");
    }

    @FXML
    private void onIrACartelera() {
        Navegador.navegarA("/ui/views/cartelera.fxml", "CinemaUQ - Cartelera de Películas");
    }
}
