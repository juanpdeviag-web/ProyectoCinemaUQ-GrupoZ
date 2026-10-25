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

        // Configurar el Stage
        primaryStage.setTitle("CinemaUQ - Gestión de Cine");
        primaryStage.setResizable(true);
        
        // Configurar tamaño por defecto para modo ventana (80% de pantalla)
        primaryStage.setWidth(1280);
        primaryStage.setHeight(720);
        
        // Iniciar en modo maximizado
        primaryStage.setMaximized(true);

        // Navegar a la pantalla inicial (cartelera)
        Navegador.navegarA("/ui/views/cartelera.fxml", "CinemaUQ - Cartelera de Películas");
    }

    public static void main(String[] args) {
        launch(args);
    }
}