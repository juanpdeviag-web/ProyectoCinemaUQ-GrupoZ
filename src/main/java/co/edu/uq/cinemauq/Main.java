package co.edu.uq.cinemauq;

import co.edu.uq.cinemauq.ui.models.SesionCompra;
import co.edu.uq.cinemauq.ui.utils.Navegador;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Crear la sesión de compra compartida
        SesionCompra sesionCompra = new SesionCompra();

        // Inicializar el navegador con el Stage y la sesión
        Navegador.inicializar(primaryStage, sesionCompra);

        // Configurar el Stage con resolución 1920x1080
        primaryStage.setTitle("CinemaUQ - Gestión de Cine");
        primaryStage.setResizable(false);

        // Navegar a la pantalla inicial (cartelera)
        Navegador.navegarA("/ui/views/cartelera.fxml", "CinemaUQ - Cartelera de Películas");
    }

    public static void main(String[] args) {
        launch(args);
    }
}