package co.edu.uq.cinemauq.ui.controllers;

import co.edu.uq.cinemauq.core.domain.Administrador;
import co.edu.uq.cinemauq.core.domain.Cliente;
import co.edu.uq.cinemauq.core.domain.Usuario;
import co.edu.uq.cinemauq.core.seed.UserSeeder;
import co.edu.uq.cinemauq.ui.utils.Navegador;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador para la pantalla de login.
 * Permite seleccionar un usuario (cliente o administrador) e iniciar sesión con contraseña.
 */
public class LoginController implements Initializable {

    @FXML
    private TilePane usuariosTilePane;

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
    }

    private void cargarUsuarios() {
        List<Usuario> usuarios = UserSeeder.cargarUsuarios();
        
        for (Usuario usuario : usuarios) {
            VBox usuarioCard = crearTarjetaUsuario(usuario);
            usuariosTilePane.getChildren().add(usuarioCard);
        }
    }

    private VBox crearTarjetaUsuario(Usuario usuario) {
        VBox card = new VBox();
        card.getStyleClass().add("usuario-card");
        card.setSpacing(12);

        // Icono según rol
        Label iconoLabel = new Label(usuario instanceof Administrador ? "👨‍💼" : "👤");
        iconoLabel.getStyleClass().add("usuario-icono");

        Label nombreLabel = new Label(usuario.getNombre());
        nombreLabel.getStyleClass().add("usuario-nombre");
        nombreLabel.setWrapText(true);

        Label rolLabel = new Label(usuario instanceof Administrador ? "Administrador" : "Cliente");
        rolLabel.getStyleClass().add("usuario-rol");

        Label correoLabel = new Label(usuario.getCorreo());
        correoLabel.getStyleClass().add("usuario-correo");
        correoLabel.setWrapText(true);

        card.getChildren().addAll(iconoLabel, nombreLabel, rolLabel, correoLabel);

        // Al hacer clic en la tarjeta, seleccionar el usuario
        card.setOnMouseClicked(event -> onUsuarioSeleccionado(usuario));

        return card;
    }

    private void onUsuarioSeleccionado(Usuario usuario) {
        usuarioSeleccionado = usuario;

        // Deseleccionar todas las tarjetas
        for (javafx.scene.Node node : usuariosTilePane.getChildren()) {
            node.getStyleClass().remove("usuario-card-selected");
        }

        // Seleccionar la tarjeta clickeada
        VBox selectedCard = (VBox) usuariosTilePane.getChildren().get(UserSeeder.cargarUsuarios().indexOf(usuario));
        selectedCard.getStyleClass().add("usuario-card-selected");

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
