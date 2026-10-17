package co.edu.uq.cinemauq.ui.controllers;

import co.edu.uq.cinemauq.core.domain.Funcion;
import co.edu.uq.cinemauq.core.domain.Sala;
import co.edu.uq.cinemauq.ui.models.SesionCompra;
import co.edu.uq.cinemauq.ui.utils.FuncionesGenerator;
import co.edu.uq.cinemauq.ui.utils.Navegador;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Controlador para la pantalla de selección de asientos.
 * Muestra la grilla de asientos de la sala y permite seleccionar los disponibles.
 */
public class AsientosController implements Initializable {
    
    @FXML
    private Label funcionInfoLabel;
    
    @FXML
    private Label salaInfoLabel;
    
    @FXML
    private GridPane asientosGrid;
    
    @FXML
    private Label asientosSeleccionadosLabel;
    
    @FXML
    private Label totalLabel;
    
    @FXML
    private Button btnContinuar;
    
    @FXML
    private Button btnVolver;
    
    private SesionCompra sesionCompra;
    private Sala sala;
    private Set<String> asientosOcupados;
    private double precioEntrada;
    
    /**
     * Inicializa el controlador.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // La inicialización se completa en cargarDatos()
    }
    
    /**
     * Carga los datos de la función seleccionada y renderiza los asientos.
     * Este método debe llamarse después de crear el controlador.
     */
    public void cargarDatos(SesionCompra sesionCompra) {
        this.sesionCompra = sesionCompra;
        
        if (sesionCompra == null || !sesionCompra.tieneFuncionSeleccionada()) {
            System.err.println("Error: No hay función seleccionada en la sesión");
            return;
        }
        
        Funcion funcion = sesionCompra.getFuncionSeleccionada();
        this.sala = funcion.getSala();
        this.precioEntrada = sesionCompra.getPrecioEntrada();
        
        // Generar asientos ocupados aleatoriamente para simulación
        this.asientosOcupados = generarAsientosOcupados(sala);
        
        // Mostrar información de la función
        String fechaHora = FuncionesGenerator.formatearFechaHora(funcion.getFechaHora());
        funcionInfoLabel.setText(fechaHora + " - " + funcion.getPelicula().getTitulo());
        salaInfoLabel.setText("Sala " + sala.getNumero() + " - Capacidad: " + sala.calcularCapacidad() + " asientos");
        
        // Renderizar grilla de asientos
        renderizarAsientos();
        
        // Actualizar resumen
        actualizarResumen();
    }
    
    /**
     * Genera asientos ocupados aleatoriamente para simulación.
     * En un sistema real, esto vendría de una base de datos.
     */
    private Set<String> generarAsientosOcupados(Sala sala) {
        Set<String> ocupados = new HashSet<>();
        List<String> todosAsientos = FuncionesGenerator.generarIdsAsientos(sala);
        
        // Ocupar aproximadamente el 30% de los asientos
        int cantidadOcupados = (int) (todosAsientos.size() * 0.3);
        java.util.Collections.shuffle(todosAsientos);
        
        for (int i = 0; i < cantidadOcupados; i++) {
            ocupados.add(todosAsientos.get(i));
        }
        
        return ocupados;
    }
    
    /**
     * Renderiza la grilla de asientos en el GridPane.
     */
    private void renderizarAsientos() {
        asientosGrid.getChildren().clear();
        
        int filas = sala.getCapacidadFilas();
        int columnas = sala.getCapacidadColumnas();
        
        for (int fila = 0; fila < filas; fila++) {
            for (int columna = 0; columna < columnas; columna++) {
                String idAsiento = FuncionesGenerator.generarIdAsiento(fila, columna);
                Button asientoBtn = crearBotonAsiento(idAsiento, fila, columna);
                asientosGrid.add(asientoBtn, columna, fila);
            }
        }
    }
    
    /**
     * Crea un botón para un asiento específico.
     */
    private Button crearBotonAsiento(String idAsiento, int fila, int columna) {
        Button btn = new Button(idAsiento);
        btn.getStyleClass().add("asiento");
        
        // Determinar estado del asiento
        if (asientosOcupados.contains(idAsiento)) {
            btn.getStyleClass().add("asiento-ocupado");
            btn.setDisable(true);
        } else if (sesionCompra.getAsientosSeleccionados().contains(idAsiento)) {
            btn.getStyleClass().add("asiento-seleccionado");
        } else {
            btn.getStyleClass().add("asiento-disponible");
        }
        
        // Manejar clic en asiento
        btn.setOnAction(event -> onToggleAsiento(idAsiento, btn));
        
        return btn;
    }
    
    /**
     * Maneja la selección/deselección de un asiento.
     */
    private void onToggleAsiento(String idAsiento, Button btn) {
        if (asientosOcupados.contains(idAsiento)) {
            return; // Asiento ocupado, no se puede seleccionar
        }
        
        if (sesionCompra.getAsientosSeleccionados().contains(idAsiento)) {
            // Deseleccionar
            sesionCompra.removerAsiento(idAsiento);
            btn.getStyleClass().remove("asiento-seleccionado");
            btn.getStyleClass().add("asiento-disponible");
        } else {
            // Seleccionar
            sesionCompra.agregarAsiento(idAsiento);
            btn.getStyleClass().remove("asiento-disponible");
            btn.getStyleClass().add("asiento-seleccionado");
        }
        
        actualizarResumen();
    }
    
    /**
     * Actualiza el resumen de selección.
     */
    private void actualizarResumen() {
        List<String> seleccionados = sesionCompra.getAsientosSeleccionados();
        int cantidad = seleccionados.size();
        double total = cantidad * precioEntrada;
        
        if (cantidad == 0) {
            asientosSeleccionadosLabel.setText("Ningún asiento seleccionado");
            btnContinuar.setDisable(true);
        } else {
            asientosSeleccionadosLabel.setText("Asientos: " + String.join(", ", seleccionados));
            btnContinuar.setDisable(false);
        }
        
        totalLabel.setText(String.format("Total: COP $%,.2f", total));
    }
    
    /**
     * Maneja el botón de continuar.
     * Navega a la pantalla de confitería.
     */
    @FXML
    private void onContinuar() {
        System.out.println("Continuar a confitería con asientos: " + sesionCompra.getAsientosSeleccionados());
        Navegador.navegarA("/ui/views/confiteria.fxml", "CinemaUQ - Confitería");
    }
    
    /**
     * Maneja el botón de volver.
     * Regresa a la pantalla de funciones.
     */
    @FXML
    private void onVolver() {
        // Limpiar asientos seleccionados al volver
        sesionCompra.limpiarAsientos();
        Navegador.navegarA("/ui/views/funciones.fxml", "CinemaUQ - Funciones");
    }
    
    /**
     * Obtiene la sesión de compra actual.
     */
    public SesionCompra getSesionCompra() {
        return sesionCompra;
    }
}
