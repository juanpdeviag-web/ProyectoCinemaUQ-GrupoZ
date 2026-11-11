package co.edu.uq.cinemauq.ui.controllers;

import co.edu.uq.cinemauq.core.domain.Usuario;
import co.edu.uq.cinemauq.core.domain.Pelicula;
import co.edu.uq.cinemauq.core.seed.DataSeeder;
import co.edu.uq.cinemauq.ui.models.SesionCompra;
import co.edu.uq.cinemauq.ui.utils.Navegador;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador para la pantalla de cartelera de películas.
 * Muestra las películas disponibles y permite seleccionar una para ver sus funciones.
 */
public class CarteleraController {

    @FXML
    private TilePane peliculasTilePane;

    @FXML
    private Label contadorPeliculas;

    @FXML
    private Label sinPeliculasLabel;
    @FXML private GridPane filtrosGrid;
    @FXML private VBox busquedaFiltro;
    @FXML private VBox generoFiltro;
    @FXML private VBox ordenFiltro;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> filtroGenero;

    @FXML
    private ComboBox<String> ordenarPor;

    private SesionCompra sesionCompra;
    private List<Pelicula> todasLasPeliculas;

    /**
     * Inicializa el controlador. Carga las películas desde DataSeeder
     * y las muestra en la interfaz.
     */
    @FXML
    public void initialize() {
        // Usar la sesión de Navegador en lugar de crear una nueva
        this.sesionCompra = Navegador.getSesionCompra();
        cargarPeliculas();

        inicializarFiltros();
        configurarListeners();
        configurarDistribucion();
    }

    /**
     * Adapta únicamente la distribución visual al espacio disponible.
     */
    private void configurarDistribucion() {
        filtrosGrid.widthProperty().addListener((obs, anterior, ancho) -> distribuirFiltros(ancho.doubleValue()));
        distribuirFiltros(filtrosGrid.getWidth());
        peliculasTilePane.widthProperty().addListener((obs, anterior, ancho) -> {
            double espacio = ancho.doubleValue();
            if (espacio > 0) {
                int columnas = Math.max(1, Math.min(4, (int) ((espacio + 24) / (250 + 24))));
                peliculasTilePane.setPrefColumns(columnas);
                // Dejar margen al redondeo de píxeles para no perder una columna.
                peliculasTilePane.setPrefTileWidth(Math.floor((espacio - (columnas - 1) * 24) / columnas) - 1);
            }
        });
    }

    private void distribuirFiltros(double ancho) {
        boolean vertical = ancho < 760;
        filtrosGrid.getColumnConstraints().clear();
        for (double porcentaje : vertical ? new double[]{100} : new double[]{44, 28, 28}) {
            ColumnConstraints columna = new ColumnConstraints();
            columna.setPercentWidth(porcentaje);
            columna.setHgrow(Priority.ALWAYS);
            filtrosGrid.getColumnConstraints().add(columna);
        }
        VBox[] filtros = {busquedaFiltro, generoFiltro, ordenFiltro};
        for (int i = 0; i < filtros.length; i++) {
            GridPane.setColumnIndex(filtros[i], vertical ? 0 : i);
            GridPane.setRowIndex(filtros[i], vertical ? i : 0);
        }
    }

    /**
     * Actualiza la información del perfil de usuario en la interfaz.
     * Muestra el nombre y rol si hay un usuario logueado.
     */


    /**
     * Carga las películas desde DataSeeder y crea las tarjetas visuales.
     */
    private void cargarPeliculas() {
        todasLasPeliculas = co.edu.uq.cinemauq.services.CatalogoCinema.getInstance().peliculasActivas();
        mostrarPeliculas(todasLasPeliculas);
        actualizarContador(todasLasPeliculas.size());
    }

    /**
     * Muestra las películas en el TilePane.
     */
    private void mostrarPeliculas(List<Pelicula> peliculas) {
        peliculasTilePane.getChildren().clear();
        sinPeliculasLabel.setVisible(peliculas.isEmpty());
        sinPeliculasLabel.setManaged(peliculas.isEmpty());
        for (Pelicula pelicula : peliculas) {
            VBox peliculaCard = crearTarjetaPelicula(pelicula);
            peliculasTilePane.getChildren().add(peliculaCard);
        }
    }

    /**
     * Actualiza el contador de películas.
     */
    private void actualizarContador(int cantidad) {
        contadorPeliculas.setText("(" + cantidad + " película" + (cantidad != 1 ? "s" : "") + ")");
    }

    /**
     * Inicializa los filtros de género y ordenamiento.
     */
    private void inicializarFiltros() {
        // Obtener géneros únicos
        List<String> generos = todasLasPeliculas.stream()
                .map(Pelicula::getGenero)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        filtroGenero.getItems().addAll("Todos los géneros");
        filtroGenero.getItems().addAll(generos);
        filtroGenero.setValue("Todos los géneros");

        // Opciones de ordenamiento
        ordenarPor.getItems().addAll("Relevancia", "Título A-Z", "Título Z-A", "Duración");
        ordenarPor.setValue("Relevancia");
    }

    /**
     * Configura los listeners para búsqueda y filtros.
     */
    private void configurarListeners() {
        // Listener para búsqueda
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filtrarPeliculas();
        });

        // Listener para filtro de género
        filtroGenero.valueProperty().addListener((observable, oldValue, newValue) -> {
            filtrarPeliculas();
        });

        // Listener para ordenamiento
        ordenarPor.valueProperty().addListener((observable, oldValue, newValue) -> {
            filtrarPeliculas();
        });
    }

    /**
     * Filtra las películas según búsqueda, género y ordenamiento.
     */
    private void filtrarPeliculas() {
        String busqueda = searchField.getText().toLowerCase();
        String generoSeleccionado = filtroGenero.getValue();
        String ordenamiento = ordenarPor.getValue();

        List<Pelicula> filtradas = todasLasPeliculas.stream()
                .filter(p -> {
                    boolean coincideBusqueda = p.getTitulo().toLowerCase().contains(busqueda) ||
                            p.getGenero().toLowerCase().contains(busqueda);
                    boolean coincideGenero = generoSeleccionado.equals("Todos los géneros") ||
                            p.getGenero().equals(generoSeleccionado);
                    return coincideBusqueda && coincideGenero;
                })
                .sorted((p1, p2) -> {
                    switch (ordenamiento) {
                        case "Título A-Z":
                            return p1.getTitulo().compareToIgnoreCase(p2.getTitulo());
                        case "Título Z-A":
                            return p2.getTitulo().compareToIgnoreCase(p1.getTitulo());
                        case "Duración":
                            return Integer.compare(p1.getDuracionMinutos(), p2.getDuracionMinutos());
                        default:
                            return 0; // Relevancia - mantener orden original
                    }
                })
                .collect(Collectors.toList());

        mostrarPeliculas(filtradas);
        actualizarContador(filtradas.size());
    }

    /**
     * Crea una tarjeta visual para una película.
     */
    private VBox crearTarjetaPelicula(Pelicula pelicula) {
        VBox card = new VBox();
        card.getStyleClass().add("pelicula-card");
        card.setSpacing(12);

        // Icono de película
        Label iconoLabel = new Label("🎬");
        iconoLabel.getStyleClass().add("pelicula-icono");

        Label tituloLabel = new Label(pelicula.getTitulo());
        tituloLabel.getStyleClass().add("pelicula-titulo");
        tituloLabel.setWrapText(true);

        Label generoLabel = new Label(pelicula.getGenero());
        generoLabel.getStyleClass().add("pelicula-genero");

        Label duracionLabel = new Label("⏱ " + pelicula.getDuracionMinutos() + " min");
        duracionLabel.getStyleClass().add("pelicula-duracion");

        Button verFunciones = new Button("Ver funciones");
        verFunciones.getStyleClass().add("boton-principal");
        verFunciones.setMaxWidth(Double.MAX_VALUE);
        verFunciones.setOnAction(event -> onSeleccionarPelicula(pelicula));
        Region espacio = new Region();
        VBox.setVgrow(espacio, Priority.ALWAYS);
        card.getChildren().addAll(iconoLabel, tituloLabel, generoLabel, duracionLabel, espacio, verFunciones);

        // Al hacer clic en la tarjeta, seleccionar la película
        card.setOnMouseClicked(event -> {
            javafx.scene.Node origen = (javafx.scene.Node) event.getTarget();
            while (origen != null && origen != card) {
                if (origen instanceof Button) return;
                origen = origen.getParent();
            }
            onSeleccionarPelicula(pelicula);
        });

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


    /**
     * Maneja el botón de panel de administrador.
     * Solo visible para usuarios con rol de administrador.
     */


    /**
     * Maneja la opción "Mi Cuenta" del menú del perfil.
     */

    /**
     * Maneja la opción "Tarjeta Virtual" del menú del perfil.
     */


    /**
     * Maneja la opción "Mis Tickets" del menú del perfil.
     */


    /**
     * Maneja la opción "Ir a Cartelera" del menú del perfil.
     */


    /**
     * Obtiene la sesión de compra actual.
     * Útil para compartir datos entre controladores.
     */
    public SesionCompra getSesionCompra() {
        return sesionCompra;
    }
}
