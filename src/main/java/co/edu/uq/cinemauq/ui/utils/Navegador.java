package co.edu.uq.cinemauq.ui.utils;

import co.edu.uq.cinemauq.core.domain.ReservaAsiento;
import co.edu.uq.cinemauq.core.domain.Usuario;
import co.edu.uq.cinemauq.ui.models.SesionCompra;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

/**
 * Utilidad para manejar la navegación entre pantallas en JavaFX.
 * Permite cargar vistas FXML y cambiar el Scene de manera organizada.
 */
public class Navegador {

    private static Stage primaryStage;
    private static SesionCompra sesionCompra;
    private static Usuario usuarioActual;
    private static List<ReservaAsiento> reservasSeleccionadas;

    /**
     * Inicializa el navegador con el Stage principal y la sesión de compra.
     */
    public static void inicializar(Stage stage, SesionCompra sesionCompra) {
        Navegador.primaryStage = stage;
        Navegador.sesionCompra = sesionCompra;
    }

    /**
     * Navega a una vista específica.
     * @param fxmlPath Ruta del archivo FXML (ej: "/ui/views/cartelera.fxml")
     * @param titulo Título de la ventana
     */
    public static void navegarA(String fxmlPath, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(Navegador.class.getResource(fxmlPath));
            Parent root = loader.load();

            // Si el controlador requiere la sesión de compra, se la pasamos
            Object controller = loader.getController();
            if (controller instanceof co.edu.uq.cinemauq.ui.controllers.FuncionesController) {
                ((co.edu.uq.cinemauq.ui.controllers.FuncionesController) controller).cargarDatos(sesionCompra);
            } else if (controller instanceof co.edu.uq.cinemauq.ui.controllers.AsientosController) {
                ((co.edu.uq.cinemauq.ui.controllers.AsientosController) controller).cargarDatos(sesionCompra);
            } else if (controller instanceof co.edu.uq.cinemauq.ui.controllers.ConfiteriaController) {
                ((co.edu.uq.cinemauq.ui.controllers.ConfiteriaController) controller).cargarDatos(sesionCompra);
            } else if (controller instanceof co.edu.uq.cinemauq.ui.controllers.CompraController) {
                ((co.edu.uq.cinemauq.ui.controllers.CompraController) controller).cargarDatos(sesionCompra);
            } else if (controller instanceof co.edu.uq.cinemauq.ui.controllers.TarjetaVirtualController) {
                ((co.edu.uq.cinemauq.ui.controllers.TarjetaVirtualController) controller).cargarDatos(sesionCompra);
            } else if (controller instanceof co.edu.uq.cinemauq.ui.controllers.TicketController) {
                if (reservasSeleccionadas != null && !reservasSeleccionadas.isEmpty()) {
                    ((co.edu.uq.cinemauq.ui.controllers.TicketController) controller).cargarDatos(reservasSeleccionadas);
                    reservasSeleccionadas = null; // Limpiar después de usar
                } else {
                    ((co.edu.uq.cinemauq.ui.controllers.TicketController) controller).cargarUltimaReserva();
                }
            } else if (controller instanceof co.edu.uq.cinemauq.ui.controllers.AdminPanelController) {
                ((co.edu.uq.cinemauq.ui.controllers.AdminPanelController) controller).cargarDatos(usuarioActual);
            }

            Scene scene = new Scene(root, 1920, 1080);
            primaryStage.setTitle(titulo);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Error al cargar la vista: " + fxmlPath);
            e.printStackTrace();
        }
    }

    /**
     * Obtiene la sesión de compra actual.
     */
    public static SesionCompra getSesionCompra() {
        return sesionCompra;
    }

    /**
     * Obtiene el Stage principal.
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Establece el usuario actual logueado.
     */
    public static void setUsuarioActual(Usuario usuario) {
        usuarioActual = usuario;
    }

    /**
     * Obtiene el usuario actual logueado.
     */
    public static Usuario getUsuarioActual() {
        return usuarioActual;
    }

    /**
     * Cierra sesión y navega a la cartelera.
     */
    public static void cerrarSesion() {
        usuarioActual = null;
        navegarA("/ui/views/cartelera.fxml", "CinemaUQ - Cartelera de Películas");
    }

    /**
     * Establece las reservas seleccionadas para mostrar en el ticket.
     */
    public static void setReservasSeleccionadas(List<ReservaAsiento> reservas) {
        reservasSeleccionadas = reservas;
    }

    /**
     * Obtiene las reservas seleccionadas.
     */
    public static List<ReservaAsiento> getReservasSeleccionadas() {
        return reservasSeleccionadas;
    }
}
