package co.edu.uq.cinemauq.ui.controllers;

import co.edu.uq.cinemauq.core.domain.Administrador;
import co.edu.uq.cinemauq.core.domain.Cliente;
import co.edu.uq.cinemauq.core.domain.Usuario;
import co.edu.uq.cinemauq.core.seed.UserSeeder;
import co.edu.uq.cinemauq.ui.utils.Navegador;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.PasswordField;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador para la pantalla de login.
 * Permite seleccionar un usuario (cliente o administrador) e iniciar sesión con contraseña.
 */
public class LoginController implements Initializable {

    @FXML
    private ComboBox<Usuario> usuarioComboBox;

    @FXML
    private PasswordField contrasenaField;

    @FXML
    private Label infoLabel;

    @FXML
    private Button btnIniciarSesion;

    @FXML
    private Button btnCrearUsuario;

    @FXML
    private Button btnVolver;

    private Usuario usuarioSeleccionado;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cargarUsuarios();
        configurarComboBox();
    }

    private void cargarUsuarios() {
        List<Usuario> usuarios = UserSeeder.cargarUsuarios();
        usuarioComboBox.getItems().addAll(usuarios);
    }

    private void configurarComboBox() {
        // Personalizar cómo se muestran los usuarios en el ComboBox
        usuarioComboBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Usuario usuario, boolean empty) {
                super.updateItem(usuario, empty);
                if (empty || usuario == null) {
                    setText(null);
                } else {
                    String rol = usuario instanceof Administrador ? "ADMIN" : "CLIENTE";
                    setText(usuario.getNombre() + " (" + rol + ") - " + usuario.getCorreo());
                }
            }
        });

        // Personalizar el texto del item seleccionado
        usuarioComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Usuario usuario, boolean empty) {
                super.updateItem(usuario, empty);
                if (empty || usuario == null) {
                    setText(null);
                } else {
                    String rol = usuario instanceof Administrador ? "ADMIN" : "CLIENTE";
                    setText(usuario.getNombre() + " (" + rol + ")");
                }
            }
        });

        // Manejar selección de usuario
        usuarioComboBox.setOnAction(event -> onUsuarioSeleccionado());
    }

    private void onUsuarioSeleccionado() {
        usuarioSeleccionado = usuarioComboBox.getValue();

        if (usuarioSeleccionado != null) {
            System.out.println("Usuario seleccionado: " + usuarioSeleccionado.getNombre() + " (" + usuarioSeleccionado.getId() + ")");
            infoLabel.setText("Usuario seleccionado: " + usuarioSeleccionado.getNombre() + ". Ingresa tu contraseña.");
            infoLabel.setStyle("-fx-text-fill: #8B7355;");
        }
    }

    @FXML
    private void onIniciarSesion() {
        if (usuarioSeleccionado == null) {
            infoLabel.setText("Por favor selecciona un usuario");
            infoLabel.setStyle("-fx-text-fill: #e94560;");
            return;
        }

        String contrasena = contrasenaField.getText();
        if (contrasena == null || contrasena.trim().isEmpty()) {
            infoLabel.setText("Por favor ingresa tu contraseña");
            infoLabel.setStyle("-fx-text-fill: #e94560;");
            return;
        }

        // Validar contraseña
        if (UserSeeder.validarContrasena(usuarioSeleccionado.getId(), contrasena)) {
            System.out.println("Iniciando sesión como: " + usuarioSeleccionado.getNombre());
            System.out.println("Rol: " + (usuarioSeleccionado instanceof Administrador ? "Administrador" : "Cliente"));

            // Guardar el usuario actual en Navegador
            Navegador.setUsuarioActual(usuarioSeleccionado);

            if (usuarioSeleccionado instanceof Administrador) {
                // Navegar a panel de administrador
                Navegador.navegarA("/ui/views/admin_panel.fxml", "CinemaUQ - Panel de Administrador");
            } else {
                // Navegar a cartelera como cliente
                Navegador.navegarA("/ui/views/cartelera.fxml", "CinemaUQ - Cartelera de Películas");
            }
        } else {
            infoLabel.setText("Contraseña incorrecta. Intenta nuevamente.");
            infoLabel.setStyle("-fx-text-fill: #e94560;");
            contrasenaField.clear();
        }
    }

    @FXML
    private void onCrearUsuario() {
        Navegador.navegarA("/ui/views/crear_usuario.fxml", "CinemaUQ - Crear Usuario");
    }

    @FXML
    private void onVolver() {
        Navegador.navegarA("/ui/views/cartelera.fxml", "CinemaUQ - Cartelera de Películas");
    }
}
