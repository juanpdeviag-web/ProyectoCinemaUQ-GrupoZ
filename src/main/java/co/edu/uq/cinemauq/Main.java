package co.edu.uq.cinemauq;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("CinemaUQ - Gestión de Cine");
        // El esqueleto queda preparado para lanzar la interfaz en la Entrega 2
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}