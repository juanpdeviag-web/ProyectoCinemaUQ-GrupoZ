package co.edu.uq.cinemauq.ui.controllers;

import co.edu.uq.cinemauq.core.domain.Usuario;
import co.edu.uq.cinemauq.ui.utils.Navegador;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import java.net.URL;
import java.util.ResourceBundle;

/** Controlador para la pantalla de Mi Cuenta. */
public class MiCuentaController implements Initializable {

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void cargarDatos(Usuario usuario) {
    }

    @FXML
    private void onVolver() {
        Navegador.navegarA("/ui/views/cartelera.fxml", "CinemaUQ - Cartelera de Películas");
    }
}
