package co.edu.uq.cinemauq.ui.controllers;

import co.edu.uq.cinemauq.core.domain.Administrador;
import co.edu.uq.cinemauq.core.domain.Cliente;
import co.edu.uq.cinemauq.core.domain.Usuario;
import co.edu.uq.cinemauq.core.domain.Pelicula;
import co.edu.uq.cinemauq.core.seed.DataSeeder;
import co.edu.uq.cinemauq.ui.models.SesionCompra;
import co.edu.uq.cinemauq.ui.utils.Navegador;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Controlador para la pantalla de cartelera de películas.
 * Muestra las películas disponibles y permite seleccionar una para ver sus funciones.
 */
public class CarteleraController {

    @FXML
    private TilePane peliculasTilePane;

    @FXML
    private Label perfilNombreLabel;

    @FXML
    private Label perfilRolLabel;

    @FXML
    private Button btnLogin;

    private SesionCompra sesionCompra;
    
    /**
     * Inicializa el controlador. Carga las películas desde DataSeeder
     * y las muestra en la interfaz.
     */
    @FXML
    public void initialize() {
        // Usar la sesión de Navegador en lugar de crear una nueva
        this.sesionCompra = Navegador.getSesionCompra();
        cargarPeliculas();
        actualizarPerfil();
    }

    /**
     * Actualiza la información del perfil de usuario en la interfaz.
     * Muestra el nombre y rol si hay un usuario logueado.
     */
    private void actualizarPerfil() {
        Usuario usuarioActual = Navegador.getUsuarioActual();

        if (usuarioActual != null) {
            // Mostrar información del usuario
            perfilNombreLabel.setText(usuarioActual.getNombre());

            if (usuarioActual instanceof Administrador) {
                perfilRolLabel.setText("Rol: Administrador");
                perfilRolLabel.setStyle("-fx-text-fill: #8B7355;");
            } else if (usuarioActual instanceof Cliente) {
                perfilRolLabel.setText("Rol: Cliente");
                perfilRolLabel.setStyle("-fx-text-fill: #8B7355;");
            }

            // Cambiar botón de login a cerrar sesión
            btnLogin.setText("Cerrar Sesión");
        } else {
            // Ocultar información del perfil
            perfilNombreLabel.setText("");
            perfilRolLabel.setText("");
            btnLogin.setText("Log In");
        }
    }
    
    /**
     * Carga las películas desde DataSeeder y crea las tarjetas visuales.
     */
    private void cargarPeliculas() {
        List<Pelicula> peliculas = DataSeeder.cargarPeliculas();
        
        for (Pelicula pelicula : peliculas) {
            VBox peliculaCard = crearTarjetaPelicula(pelicula);
            peliculasTilePane.getChildren().add(peliculaCard);
        }
    }
    
    /**
     * Crea una tarjeta visual para una película.
     */
    private VBox crearTarjetaPelicula(Pelicula pelicula) {
        VBox card = new VBox();
        card.getStyleClass().add("pelicula-card");
        card.setSpacing(10);
        
        Label tituloLabel = new Label(pelicula.getTitulo());
        tituloLabel.getStyleClass().add("pelicula-titulo");
        tituloLabel.setWrapText(true);
        
        Label generoLabel = new Label(pelicula.getGenero());
        generoLabel.getStyleClass().add("pelicula-genero");
        
        Label duracionLabel = new Label(pelicula.getDuracionMinutos() + " min");
        duracionLabel.getStyleClass().add("pelicula-duracion");
        
        card.getChildren().addAll(tituloLabel, generoLabel, duracionLabel);
        
        // Al hacer clic en la tarjeta, seleccionar la película
        card.setOnMouseClicked(event -> onSeleccionarPelicula(pelicula));
        
        return card;
    }
    
    /**
     * Maneja la selección de una película.
     * Verifica si hay usuario logueado antes de permitir la selección.
     */
    private void onSeleccionarPelicula(Pelicula pelicula) {
        Usuario usuarioActual = Navegador.getUsuarioActual();

        if (usuarioActual == null) {
            // No hay usuario logueado, mostrar login
            System.out.println("Intento de selección sin sesión. Redirigiendo a login.");
            Navegador.navegarA("/ui/views/login.fxml", "CinemaUQ - Iniciar Sesión");
            return;
        }

        // Usuario logueado, permitir selección
        sesionCompra.setPeliculaSeleccionada(pelicula);
        System.out.println("Película seleccionada: " + pelicula.getTitulo() + " por " + usuarioActual.getNombre());
        Navegador.navegarA("/ui/views/funciones.fxml", "CinemaUQ - Funciones de " + pelicula.getTitulo());
    }
    
    /**
     * Maneja el botón de salir.
     * Cierra la aplicación.
     */
    @FXML
    private void onSalir() {
        System.exit(0);
    }

    /**
     * Maneja el botón de login/cerrar sesión.
     * Si hay usuario logueado, cierra sesión. Si no, navega a login.
     */
    @FXML
    private void onLogin() {
        Usuario usuarioActual = Navegador.getUsuarioActual();

        if (usuarioActual != null) {
            // Cerrar sesión
            Navegador.cerrarSesion();
            System.out.println("Sesión cerrada para: " + usuarioActual.getNombre());
            // Recargar la vista para actualizar el perfil
            Navegador.navegarA("/ui/views/cartelera.fxml", "CinemaUQ - Cartelera de Películas");
        } else {
            // Navegar a login
            Navegador.navegarA("/ui/views/login.fxml", "CinemaUQ - Iniciar Sesión");
        }
    }

    /**
     * Obtiene la sesión de compra actual.
     * Útil para compartir datos entre controladores.
     */
    public SesionCompra getSesionCompra() {
        return sesionCompra;
    }
}
