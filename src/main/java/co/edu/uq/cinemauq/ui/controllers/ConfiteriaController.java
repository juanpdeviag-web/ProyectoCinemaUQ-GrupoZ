package co.edu.uq.cinemauq.ui.controllers;

import co.edu.uq.cinemauq.patterns.composite.Combo;
import co.edu.uq.cinemauq.patterns.composite.ItemCompra;
import co.edu.uq.cinemauq.patterns.composite.Producto;
import co.edu.uq.cinemauq.ui.models.SesionCompra;
import co.edu.uq.cinemauq.ui.utils.Navegador;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Controlador para la pantalla de confitería.
 * Muestra productos y combos usando el patrón Composite.
 */
public class ConfiteriaController implements Initializable {
    
    @FXML
    private TilePane productosTilePane;
    
    @FXML
    private Label productosSeleccionadosLabel;
    
    @FXML
    private Label totalLabel;
    
    @FXML
    private Button btnContinuar;
    
    @FXML
    private Button btnVolver;
    
    @FXML
    private Button btnSaltar;
    
    private SesionCompra sesionCompra;
    private List<ItemCompra> productosDisponibles;
    private Map<ItemCompra, Integer> cantidadesSeleccionadas;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.cantidadesSeleccionadas = new HashMap<>();
    }
    
    public void cargarDatos(SesionCompra sesionCompra) {
        this.sesionCompra = sesionCompra;
        cargarProductos();
        actualizarResumen();
    }
    
    private void cargarProductos() {
        productosDisponibles = new ArrayList<>();
        
        // Productos individuales (precios en COP)
        productosDisponibles.add(new Producto("Palomitas", 32000.0));
        productosDisponibles.add(new Producto("Refresco", 20000.0));
        productosDisponibles.add(new Producto("Nachos", 40000.0));
        productosDisponibles.add(new Producto("Hot Dog", 48000.0));
        productosDisponibles.add(new Producto("Agua", 12000.0));
        productosDisponibles.add(new Producto("Chocolates", 24000.0));

        // Combos
        Combo combo1 = new Combo("Combo Cine")
            .agregar(new Producto("Palomitas", 32000.0))
            .agregar(new Producto("Refresco", 20000.0));
        productosDisponibles.add(combo1);

        Combo combo2 = new Combo("Combo Familiar")
            .agregar(new Producto("Palomitas", 32000.0))
            .agregar(new Producto("Palomitas", 32000.0))
            .agregar(new Producto("Refresco", 20000.0))
            .agregar(new Producto("Nachos", 40000.0));
        productosDisponibles.add(combo2);

        Combo combo3 = new Combo("Combo Deluxe")
            .agregar(new Producto("Hot Dog", 48000.0))
            .agregar(new Producto("Refresco", 20000.0))
            .agregar(new Producto("Chocolates", 24000.0));
        productosDisponibles.add(combo3);
        
        renderizarProductos();
    }
    
    private void renderizarProductos() {
        productosTilePane.getChildren().clear();
        
        for (ItemCompra producto : productosDisponibles) {
            VBox productoCard = crearTarjetaProducto(producto);
            productosTilePane.getChildren().add(productoCard);
        }
    }
    
    private VBox crearTarjetaProducto(ItemCompra producto) {
        VBox card = new VBox();
        card.getStyleClass().add("producto-card");
        card.setSpacing(12);
        card.setAlignment(javafx.geometry.Pos.CENTER);

        Label iconoLabel = new Label(obtenerIconoProducto(producto.getNombre()));
        iconoLabel.getStyleClass().add("producto-icono");

        Label nombreLabel = new Label(producto.getNombre());
        nombreLabel.getStyleClass().add("producto-nombre");
        nombreLabel.setAlignment(javafx.geometry.Pos.CENTER);

        Label precioLabel = new Label(String.format("$%,.0f", producto.getPrecio()));
        precioLabel.getStyleClass().add("producto-precio");
        precioLabel.setAlignment(javafx.geometry.Pos.CENTER);

        // Contenedor de cantidad con botones +/-
        HBox cantidadBox = new HBox();
        cantidadBox.setSpacing(10);
        cantidadBox.setAlignment(javafx.geometry.Pos.CENTER);

        Button btnMenos = new Button("-");
        btnMenos.getStyleClass().add("button-cantidad");
        btnMenos.setPrefSize(35, 35);
        btnMenos.setOnAction(event -> onRemoverProducto(producto));

        Label cantidadLabel = new Label("0");
        cantidadLabel.getStyleClass().add("producto-cantidad-display");
        cantidadLabel.setId("cantidad-" + producto.getNombre().replace(" ", "_"));

        Button btnMas = new Button("+");
        btnMas.getStyleClass().add("button-cantidad");
        btnMas.setPrefSize(35, 35);
        btnMas.setOnAction(event -> onAgregarProducto(producto));

        cantidadBox.getChildren().addAll(btnMenos, cantidadLabel, btnMas);

        card.getChildren().addAll(iconoLabel, nombreLabel, precioLabel, cantidadBox);

        return card;
    }

    private String obtenerIconoProducto(String nombre) {
        if (nombre.contains("Palomitas")) return "🍿";
        if (nombre.contains("Refresco")) return "🥤";
        if (nombre.contains("Nachos")) return "🧀";
        if (nombre.contains("Hot Dog")) return "🌭";
        if (nombre.contains("Agua")) return "💧";
        if (nombre.contains("Chocolates")) return "🍫";
        if (nombre.contains("Combo")) return "🎁";
        return "🍴";
    }
    
    private void onAgregarProducto(ItemCompra producto) {
        int cantidad = cantidadesSeleccionadas.getOrDefault(producto, 0);
        cantidad++;
        cantidadesSeleccionadas.put(producto, cantidad);

        // Agregar a sesión de compra
        for (int i = 0; i < 1; i++) {
            sesionCompra.agregarProducto(producto);
        }

        actualizarResumen();
        actualizarCantidadLabel(producto, cantidad);
    }

    private void onRemoverProducto(ItemCompra producto) {
        int cantidad = cantidadesSeleccionadas.getOrDefault(producto, 0);
        if (cantidad > 0) {
            cantidad--;
            cantidadesSeleccionadas.put(producto, cantidad);
            sesionCompra.removerProducto(producto);
        }

        actualizarResumen();
        actualizarCantidadLabel(producto, cantidad);
    }

    private void actualizarCantidadLabel(ItemCompra producto, int cantidad) {
        String id = "cantidad-" + producto.getNombre().replace(" ", "_");
        for (javafx.scene.Node node : productosTilePane.getChildren()) {
            if (node instanceof VBox) {
                VBox card = (VBox) node;
                for (javafx.scene.Node child : card.getChildren()) {
                    if (child instanceof HBox) {
                        HBox cantidadBox = (HBox) child;
                        for (javafx.scene.Node labelNode : cantidadBox.getChildren()) {
                            if (labelNode instanceof Label && labelNode.getId() != null && labelNode.getId().equals(id)) {
                                ((Label) labelNode).setText(String.valueOf(cantidad));
                            }
                        }
                    }
                }
            }
        }
    }

    private void actualizarResumen() {
        List<String> nombres = new ArrayList<>();
        for (Map.Entry<ItemCompra, Integer> entry : cantidadesSeleccionadas.entrySet()) {
            if (entry.getValue() > 0) {
                nombres.add(entry.getKey().getNombre() + " x" + entry.getValue());
            }
        }

        if (nombres.isEmpty()) {
            productosSeleccionadosLabel.setText("Ningún producto seleccionado");
        } else {
            productosSeleccionadosLabel.setText("Productos: " + String.join(", ", nombres));
        }

        totalLabel.setText(String.format("Total confitería: COP $%,.2f", sesionCompra.getPrecioProductos()));

        btnContinuar.setDisable(false); // Se puede continuar sin productos
    }
    
    @FXML
    private void onContinuar() {
        System.out.println("Continuar a resumen de compra");
        Navegador.navegarA("/ui/views/compra.fxml", "CinemaUQ - Resumen de Compra");
    }
    
    @FXML
    private void onVolver() {
        sesionCompra.limpiarProductos();
        cantidadesSeleccionadas.clear();
        Navegador.navegarA("/ui/views/asientos.fxml", "CinemaUQ - Selección de Asientos");
    }
    
    @FXML
    private void onSaltar() {
        System.out.println("Saltar confitería");
        Navegador.navegarA("/ui/views/compra.fxml", "CinemaUQ - Resumen de Compra");
    }
    
    public SesionCompra getSesionCompra() {
        return sesionCompra;
    }
}
