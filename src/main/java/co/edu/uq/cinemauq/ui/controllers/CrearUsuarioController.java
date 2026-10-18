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
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controlador para la pantalla de crear usuario.
 * Permite crear nuevos clientes o administradores.
 */
public class CrearUsuarioController implements Initializable {

    @FXML
    private TextField idTextField;

    @FXML
    private TextField nombreTextField;

    @FXML
    private TextField emailTextField;

    @FXML
    private PasswordField contrasenaField;

    @FXML
    private PasswordField confirmarContrasenaField;

    @FXML
    private ComboBox<String> rolComboBox;

    @FXML
    private TextField saldoInicialTextField;

    @FXML
    private Label infoLabel;

    @FXML
    private Button btnCrear;

    @FXML
    private Button btnVolver;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarComboBox();
    }

    private void configurarComboBox() {
        rolComboBox.getItems().addAll("Cliente", "Administrador");
        rolComboBox.setOnAction(event -> onRolSeleccionado());
    }

    private void onRolSeleccionado() {
        String rol = rolComboBox.getValue();
        if ("Cliente".equals(rol)) {
            saldoInicialTextField.setDisable(false);
            saldoInicialTextField.setPromptText("Saldo inicial (COP)");
        } else {
            saldoInicialTextField.setDisable(true);
            saldoInicialTextField.clear();
        }
    }

    @FXML
    private void onCrear() {
        String id = idTextField.getText();
        String nombre = nombreTextField.getText();
        String email = emailTextField.getText();
        String contrasena = contrasenaField.getText();
        String confirmarContrasena = confirmarContrasenaField.getText();
        String rol = rolComboBox.getValue();
        String saldoStr = saldoInicialTextField.getText();

        // Validaciones
        if (id == null || id.trim().isEmpty()) {
            infoLabel.setText("Por favor ingresa un ID");
            infoLabel.setStyle("-fx-text-fill: #e94560;");
            return;
        }

        if (nombre == null || nombre.trim().isEmpty()) {
            infoLabel.setText("Por favor ingresa un nombre");
            infoLabel.setStyle("-fx-text-fill: #e94560;");
            return;
        }

        if (email == null || !email.contains("@")) {
            infoLabel.setText("Por favor ingresa un email válido");
            infoLabel.setStyle("-fx-text-fill: #e94560;");
            return;
        }

        if (contrasena == null || contrasena.trim().isEmpty()) {
            infoLabel.setText("Por favor ingresa una contraseña");
            infoLabel.setStyle("-fx-text-fill: #e94560;");
            return;
        }

        if (!contrasena.equals(confirmarContrasena)) {
            infoLabel.setText("Las contraseñas no coinciden");
            infoLabel.setStyle("-fx-text-fill: #e94560;");
            return;
        }

        if (rol == null) {
            infoLabel.setText("Por favor selecciona un rol");
            infoLabel.setStyle("-fx-text-fill: #e94560;");
            return;
        }

        // Verificar si el ID ya existe
        if (UserSeeder.buscarUsuarioPorId(id) != null) {
            infoLabel.setText("El ID ya existe. Usa otro ID.");
            infoLabel.setStyle("-fx-text-fill: #e94560;");
            return;
        }

        // Crear usuario según rol
        Usuario nuevoUsuario;
        if ("Cliente".equals(rol)) {
            nuevoUsuario = new Cliente(id, nombre, email);
            double saldo = 0;
            if (saldoStr != null && !saldoStr.trim().isEmpty()) {
                try {
                    saldo = Double.parseDouble(saldoStr);
                    ((Cliente) nuevoUsuario).getTarjeta().recargar(saldo);
                } catch (NumberFormatException e) {
                    infoLabel.setText("Saldo inválido. Se usará 0.");
                    infoLabel.setStyle("-fx-text-fill: #e94560;");
                }
            }
        } else {
            nuevoUsuario = new Administrador(id, nombre, email);
        }

        // Validar usuario
        try {
            nuevoUsuario.validar();
        } catch (Exception e) {
            infoLabel.setText("Error de validación: " + e.getMessage());
            infoLabel.setStyle("-fx-text-fill: #e94560;");
            return;
        }

        // Guardar contraseña
        UserSeeder.agregarUsuario(nuevoUsuario, contrasena);

        System.out.println("Usuario creado exitosamente: " + nombre + " (" + rol + ")");
        infoLabel.setText("Usuario creado exitosamente: " + nombre);
        infoLabel.setStyle("-fx-text-fill: #8B7355;");

        // Limpiar campos
        idTextField.clear();
        nombreTextField.clear();
        emailTextField.clear();
        contrasenaField.clear();
        confirmarContrasenaField.clear();
        saldoInicialTextField.clear();
        rolComboBox.getSelectionModel().clearSelection();
    }

    @FXML
    private void onVolver() {
        Navegador.navegarA("/ui/views/login.fxml", "CinemaUQ - Iniciar Sesión");
    }
}
