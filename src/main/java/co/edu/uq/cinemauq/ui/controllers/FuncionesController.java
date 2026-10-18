package co.edu.uq.cinemauq.ui.controllers;

import co.edu.uq.cinemauq.core.domain.Funcion;
import co.edu.uq.cinemauq.core.domain.Pelicula;
import co.edu.uq.cinemauq.ui.models.SesionCompra;
import co.edu.uq.cinemauq.ui.utils.FuncionesGenerator;
import co.edu.uq.cinemauq.ui.utils.Navegador;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador para la pantalla de funciones de una película.
 * Muestra las funciones disponibles para la película seleccionada y permite elegir una.
 */
public class FuncionesController implements Initializable {
    
    @FXML
    private Label peliculaTituloLabel;
    
    @FXML
    private Label peliculaInfoLabel;
    
    @FXML
    private VBox funcionesContainer;
    
    @FXML
    private Button btnVolver;
    
    private SesionCompra sesionCompra;
    private List<Funcion> funciones;
    
    /**
     * Inicializa el controlador.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // La inicialización se completa en cargarDatos()
    }
    
    /**
     * Carga los datos de la película seleccionada y sus funciones.
     * Este método debe llamarse después de crear el controlador.
     */
    public void cargarDatos(SesionCompra sesionCompra) {
        this.sesionCompra = sesionCompra;

        System.out.println("FuncionesController.cargarDatos() llamado");

        if (sesionCompra == null || !sesionCompra.tienePeliculaSeleccionada()) {
            System.err.println("Error: No hay película seleccionada en la sesión");
            return;
        }

        Pelicula pelicula = sesionCompra.getPeliculaSeleccionada();
        System.out.println("Película seleccionada: " + pelicula.getTitulo());

        // Mostrar información de la película
        peliculaTituloLabel.setText(pelicula.getTitulo());
        peliculaInfoLabel.setText(pelicula.getGenero() + " - " + pelicula.getDuracionMinutos() + " min");

        // Generar y mostrar funciones
        cargarFunciones(pelicula);
    }
    
    /**
     * Carga las funciones de la película y crea las tarjetas visuales.
     */
    private void cargarFunciones(Pelicula pelicula) {
        // Generar funciones de prueba para esta película
        this.funciones = FuncionesGenerator.generarFuncionesPrueba();
        System.out.println("Total funciones generadas: " + funciones.size());

        // Filtrar solo las funciones de esta película
        funcionesContainer.getChildren().clear();

        int count = 0;
        for (Funcion funcion : funciones) {
            System.out.println("Función: " + funcion.getPelicula().getTitulo() + " vs " + pelicula.getTitulo());
            if (funcion.getPelicula().getTitulo().equals(pelicula.getTitulo())) {
                VBox funcionCard = crearTarjetaFuncion(funcion);
                funcionesContainer.getChildren().add(funcionCard);
                count++;
            }
        }

        System.out.println("Funciones agregadas para " + pelicula.getTitulo() + ": " + count);

        if (funcionesContainer.getChildren().isEmpty()) {
            Label noFunciones = new Label("No hay funciones disponibles para esta película");
            noFunciones.setStyle("-fx-text-fill: #a0a0a0;");
            funcionesContainer.getChildren().add(noFunciones);
        }
    }
    
    /**
     * Crea una tarjeta visual para una función.
     */
    private VBox crearTarjetaFuncion(Funcion funcion) {
        VBox card = new VBox();
        card.getStyleClass().add("funcion-card");
        card.setSpacing(10);
        
        // Fecha y hora formateada
        String fechaHora = FuncionesGenerator.formatearFechaHora(funcion.getFechaHora());
        
        Label fechaLabel = new Label(fechaHora);
        fechaLabel.getStyleClass().add("funcion-fecha");
        
        // Información de sala
        Label salaLabel = new Label("Sala: " + funcion.getSala().getNumero());
        salaLabel.getStyleClass().add("funcion-sala");
        
        // Capacidad de la sala
        int capacidad = funcion.getSala().calcularCapacidad();
        Label capacidadLabel = new Label("Capacidad: " + capacidad + " asientos");
        capacidadLabel.getStyleClass().add("funcion-capacidad");
        
        card.getChildren().addAll(fechaLabel, salaLabel, capacidadLabel);
        
        // Al hacer clic en la tarjeta, seleccionar la función
        card.setOnMouseClicked(event -> onSeleccionarFuncion(funcion));
        
        return card;
    }
    
    /**
     * Maneja la selección de una función.
     * Guarda la función en la sesión de compra y navega a la pantalla de asientos.
     */
    private void onSeleccionarFuncion(Funcion funcion) {
        sesionCompra.setFuncionSeleccionada(funcion);
        System.out.println("Función seleccionada: " +
            FuncionesGenerator.formatearFechaHora(funcion.getFechaHora()) +
            " - Sala: " + funcion.getSala().getNumero());
        Navegador.navegarA("/ui/views/asientos.fxml", "CinemaUQ - Selección de Asientos");
    }

    /**
     * Maneja el botón de volver.
     * Regresa a la pantalla de cartelera.
     */
    @FXML
    private void onVolver() {
        Navegador.navegarA("/ui/views/cartelera.fxml", "CinemaUQ - Cartelera de Películas");
    }
    
    /**
     * Obtiene la sesión de compra actual.
     */
    public SesionCompra getSesionCompra() {
        return sesionCompra;
    }
}
