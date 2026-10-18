package co.edu.uq.cinemauq.ui.controllers;

import co.edu.uq.cinemauq.core.domain.Administrador;
import co.edu.uq.cinemauq.core.domain.Cliente;
import co.edu.uq.cinemauq.core.domain.Pelicula;
import co.edu.uq.cinemauq.core.domain.Usuario;
import co.edu.uq.cinemauq.core.seed.DataSeeder;
import co.edu.uq.cinemauq.core.seed.UserSeeder;
import co.edu.uq.cinemauq.ui.utils.Navegador;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador para el panel de administrador.
 * Permite gestionar películas, precios y ver clientes.
 */
public class AdminPanelController implements Initializable {
    
    @FXML
    private Label adminLabel;
    
    @FXML
    private TabPane tabPane;
    
    @FXML
    private Tab tabPeliculas;
    
    @FXML
    private Tab tabPrecios;
    
    @FXML
    private Tab tabClientes;
    
    // Películas
    @FXML
    private ListView<Pelicula> peliculasListView;
    
    @FXML
    private TextField tituloTextField;
    
    @FXML
    private TextField generoTextField;
    
    @FXML
    private TextField duracionTextField;
    
    @FXML
    private Button btnAgregarPelicula;
    
    @FXML
    private Button btnEliminarPelicula;
    
    // Precios
    @FXML
    private TextField precioEntradaTextField;
    
    @FXML
    private Button btnActualizarPrecioEntrada;
    
    @FXML
    private TextField precioPalomitasTextField;
    
    @FXML
    private Button btnActualizarPrecioPalomitas;
    
    @FXML
    private TextField precioRefrescoTextField;
    
    @FXML
    private Button btnActualizarPrecioRefresco;
    
    // Clientes
    @FXML
    private ListView<Cliente> clientesListView;
    
    @FXML
    private VBox clienteInfoBox;
    
    @FXML
    private Label clienteNombreLabel;
    
    @FXML
    private Label clienteEmailLabel;
    
    @FXML
    private Label clienteSaldoLabel;
    
    @FXML
    private ListView<String> comprasListView;
    
    @FXML
    private Button btnCerrarSesion;
    
    @FXML
    private Button btnVolver;
    
    private Usuario usuarioActual;
    private List<Pelicula> peliculas;
    private List<Cliente> clientes;

    // Precios actuales en COP
    private double precioEntrada = 60000.0; // $60,000 COP
    private double precioPalomitas = 32000.0; // $32,000 COP
    private double precioRefresco = 20000.0; // $20,000 COP
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cargarPeliculas();
        cargarClientes();
        cargarPrecios();
        configurarListeners();
    }
    
    public void cargarDatos(Usuario usuario) {
        this.usuarioActual = usuario;
        if (usuario instanceof Administrador) {
            adminLabel.setText("Panel de Administrador: " + usuario.getNombre());
        }
    }
    
    private void cargarPeliculas() {
        peliculas = DataSeeder.cargarPeliculas();
        peliculasListView.getItems().clear();
        peliculasListView.getItems().addAll(peliculas);
    }
    
    private void cargarClientes() {
        clientes = UserSeeder.obtenerClientes();
        clientesListView.getItems().clear();
        clientesListView.getItems().addAll(clientes);
    }
    
    private void cargarPrecios() {
        precioEntradaTextField.setText(String.valueOf(precioEntrada));
        precioPalomitasTextField.setText(String.valueOf(precioPalomitas));
        precioRefrescoTextField.setText(String.valueOf(precioRefresco));
    }
    
    private void configurarListeners() {
        // Listener para selección de película
        peliculasListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                tituloTextField.setText(newVal.getTitulo());
                generoTextField.setText(newVal.getGenero());
                duracionTextField.setText(String.valueOf(newVal.getDuracionMinutos()));
            }
        });
        
        // Listener para selección de cliente
        clientesListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                mostrarInfoCliente(newVal);
            }
        });
    }
    
    private void mostrarInfoCliente(Cliente cliente) {
        clienteNombreLabel.setText("Nombre: " + cliente.getNombre());
        clienteEmailLabel.setText("Email: " + cliente.getCorreo());
        clienteSaldoLabel.setText("Saldo: COP $" + String.format("%.2f", cliente.getTarjeta().getSaldo()));

        // Mostrar compras simuladas
        comprasListView.getItems().clear();
        comprasListView.getItems().add("Interestelar - Sala 1 - Asientos: A1, A2 - COP $120,000.00");
        comprasListView.getItems().add("El origen - Sala 2 - Asientos: B3 - COP $60,000.00");
        comprasListView.getItems().add("Confitería: Palomitas x2 - COP $64,000.00");
    }
    
    @FXML
    private void onAgregarPelicula() {
        String titulo = tituloTextField.getText();
        String genero = generoTextField.getText();
        String duracionStr = duracionTextField.getText();
        
        if (titulo.isEmpty() || genero.isEmpty() || duracionStr.isEmpty()) {
            System.err.println("Por favor completa todos los campos");
            return;
        }
        
        try {
            int duracion = Integer.parseInt(duracionStr);
            Pelicula nuevaPelicula = new Pelicula(titulo, genero, duracion);
            peliculas.add(nuevaPelicula);
            peliculasListView.getItems().add(nuevaPelicula);
            
            // Limpiar campos
            tituloTextField.clear();
            generoTextField.clear();
            duracionTextField.clear();
            
            System.out.println("Película agregada: " + titulo);
        } catch (NumberFormatException e) {
            System.err.println("La duración debe ser un número");
        }
    }
    
    @FXML
    private void onEliminarPelicula() {
        Pelicula seleccionada = peliculasListView.getSelectionModel().getSelectedItem();
        if (seleccionada != null) {
            peliculas.remove(seleccionada);
            peliculasListView.getItems().remove(seleccionada);
            System.out.println("Película eliminada: " + seleccionada.getTitulo());
            
            // Limpiar campos
            tituloTextField.clear();
            generoTextField.clear();
            duracionTextField.clear();
        } else {
            System.err.println("Selecciona una película para eliminar");
        }
    }
    
    @FXML
    private void onActualizarPrecioEntrada() {
        try {
            precioEntrada = Double.parseDouble(precioEntradaTextField.getText());
            System.out.println("Precio de entrada actualizado: $" + precioEntrada);
        } catch (NumberFormatException e) {
            System.err.println("Precio inválido");
        }
    }
    
    @FXML
    private void onActualizarPrecioPalomitas() {
        try {
            precioPalomitas = Double.parseDouble(precioPalomitasTextField.getText());
            System.out.println("Precio de palomitas actualizado: $" + precioPalomitas);
        } catch (NumberFormatException e) {
            System.err.println("Precio inválido");
        }
    }
    
    @FXML
    private void onActualizarPrecioRefresco() {
        try {
            precioRefresco = Double.parseDouble(precioRefrescoTextField.getText());
            System.out.println("Precio de refresco actualizado: $" + precioRefresco);
        } catch (NumberFormatException e) {
            System.err.println("Precio inválido");
        }
    }
    
    @FXML
    private void onCerrarSesion() {
        Navegador.cerrarSesion();
    }
    
    @FXML
    private void onVolver() {
        Navegador.navegarA("/ui/views/cartelera.fxml", "CinemaUQ - Cartelera de Películas");
    }
}
