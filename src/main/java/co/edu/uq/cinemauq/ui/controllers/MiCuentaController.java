package co.edu.uq.cinemauq.ui.controllers;

import co.edu.uq.cinemauq.core.domain.Administrador;
import co.edu.uq.cinemauq.core.domain.Cliente;
import co.edu.uq.cinemauq.core.domain.Usuario;
import co.edu.uq.cinemauq.ui.utils.Navegador;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
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
    private Button btnSalir;

    @FXML
    private VBox perfilContainer;

    @FXML
    private Label perfilNombreLabel;

    @FXML
    private Label perfilRolLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cargarInformacionUsuario();
        configurarMenuPerfil();
    }

    private void cargarInformacionUsuario() {
        Usuario usuarioActual = Navegador.getUsuarioActual();

        if (usuarioActual != null) {
            idLabel.setText(usuarioActual.getId());
            nombreLabel.setText(usuarioActual.getNombre());
            emailLabel.setText(usuarioActual.getCorreo());

            // Actualizar header
            perfilNombreLabel.setText(usuarioActual.getNombre());
            if (usuarioActual instanceof Administrador) {
                rolLabel.setText("Administrador");
                perfilRolLabel.setText("Rol: Administrador");
            } else if (usuarioActual instanceof Cliente) {
                rolLabel.setText("Cliente");
                perfilRolLabel.setText("Rol: Cliente");
                Cliente cliente = (Cliente) usuarioActual;
                saldoContainer.setVisible(true);
                saldoContainer.setManaged(true);
                DecimalFormat df = new DecimalFormat("#,###.00");
                saldoLabel.setText("$" + df.format(cliente.getTarjeta().getSaldo()) + " COP");
            }
        }
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
