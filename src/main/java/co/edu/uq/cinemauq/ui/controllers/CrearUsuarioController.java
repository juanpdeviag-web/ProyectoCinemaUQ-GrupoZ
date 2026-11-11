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
        for (javafx.scene.Node campo : new javafx.scene.Node[]{idTextField, nombreTextField, emailTextField, contrasenaField, confirmarContrasenaField, rolComboBox, saldoInicialTextField}) {
            campo.getStyleClass().remove("form-error");
        }
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
            marcarCampo(idTextField);
            estiloMensaje("mensaje-error");
            return;
        }

        if (nombre == null || nombre.trim().isEmpty()) {
            infoLabel.setText("Por favor ingresa un nombre");
            marcarCampo(nombreTextField);
            estiloMensaje("mensaje-error");
            return;
        }

        if (email == null || !email.contains("@")) {
            infoLabel.setText("Por favor ingresa un email válido");
            marcarCampo(emailTextField);
            estiloMensaje("mensaje-error");
            return;
        }

        if (contrasena == null || contrasena.trim().isEmpty()) {
            infoLabel.setText("Por favor ingresa una contraseña");
            marcarCampo(contrasenaField);
            estiloMensaje("mensaje-error");
            return;
        }

        if (!contrasena.equals(confirmarContrasena)) {
            infoLabel.setText("Las contraseñas no coinciden");
            marcarCampo(confirmarContrasenaField);
            estiloMensaje("mensaje-error");
            return;
        }

        if (rol == null) {
            infoLabel.setText("Por favor selecciona un rol");
            marcarCampo(rolComboBox);
            estiloMensaje("mensaje-error");
            return;
        }

        // Verificar si el ID ya existe
        if (UserSeeder.buscarUsuarioPorId(id) != null) {
            infoLabel.setText("El ID ya existe. Usa otro ID.");
            marcarCampo(idTextField);
            estiloMensaje("mensaje-error");
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
                    marcarCampo(saldoInicialTextField);
                    estiloMensaje("mensaje-error");
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
            estiloMensaje("mensaje-error");
            return;
        }

        // Guardar contraseña
        UserSeeder.agregarUsuario(nuevoUsuario, contrasena);

        System.out.println("Usuario creado exitosamente: " + nombre + " (" + rol + ")");
        infoLabel.setText("Usuario creado exitosamente: " + nombre);
        estiloMensaje("mensaje-exito");

        // Limpiar campos
        idTextField.clear();
        nombreTextField.clear();
        emailTextField.clear();
        contrasenaField.clear();
        confirmarContrasenaField.clear();
        saldoInicialTextField.clear();
        rolComboBox.getSelectionModel().clearSelection();
    }

    private void estiloMensaje(String estilo) {
        infoLabel.getStyleClass().removeAll("mensaje-info", "mensaje-error", "mensaje-exito");
        infoLabel.getStyleClass().add(estilo);
        if ("mensaje-exito".equals(estilo)) {
            javafx.scene.layout.VBox contenedor = (javafx.scene.layout.VBox) btnCrear.getParent();
            contenedor.getChildren().remove(infoLabel);
            contenedor.getChildren().add(contenedor.getChildren().indexOf(btnCrear), infoLabel);
        }
    }

    private void marcarCampo(javafx.scene.control.Control campo) {
        campo.getStyleClass().remove("form-error");
        campo.getStyleClass().add("form-error");
        javafx.scene.layout.VBox contenedor = (javafx.scene.layout.VBox) campo.getParent();
        contenedor.getChildren().remove(infoLabel);
        contenedor.getChildren().add(contenedor.getChildren().indexOf(campo) + 1, infoLabel);
        campo.requestFocus();
    }
    @FXML
    private void onVolver() {
        Navegador.navegarA("/ui/views/login.fxml", "CinemaUQ - Iniciar Sesión");
    }
}
