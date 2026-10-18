package co.edu.uq.cinemauq.ui.controllers;

import co.edu.uq.cinemauq.core.domain.Funcion;
import co.edu.uq.cinemauq.patterns.composite.ItemCompra;
import co.edu.uq.cinemauq.patterns.decorator.CompraBase;
import co.edu.uq.cinemauq.patterns.decorator.DescuentoFijo;
import co.edu.uq.cinemauq.patterns.decorator.Promocion;
import co.edu.uq.cinemauq.patterns.facade.CompraFacade;
import co.edu.uq.cinemauq.ui.models.SesionCompra;
import co.edu.uq.cinemauq.ui.utils.FuncionesGenerator;
import co.edu.uq.cinemauq.ui.utils.Navegador;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controlador para la pantalla de resumen de compra.
 * Muestra el resumen completo y permite confirmar la compra.
 */
public class CompraController implements Initializable {
    
    @FXML
    private Label peliculaLabel;
    
    @FXML
    private Label funcionLabel;
    
    @FXML
    private Label asientosLabel;
    
    @FXML
    private Label productosLabel;
    
    @FXML
    private Label subtotalLabel;
    
    @FXML
    private Label descuentoLabel;
    
    @FXML
    private Label totalLabel;
    
    @FXML
    private CheckBox chkDescuentoFijo;
    
    @FXML
    private CheckBox chkPromocion;
    
    @FXML
    private Button btnConfirmar;
    
    @FXML
    private Button btnVolver;
    
    @FXML
    private VBox resumenContainer;
    
    private SesionCompra sesionCompra;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        chkDescuentoFijo.setOnAction(event -> actualizarTotal());
        chkPromocion.setOnAction(event -> actualizarTotal());
    }
    
    public void cargarDatos(SesionCompra sesionCompra) {
        this.sesionCompra = sesionCompra;
        mostrarResumen();
    }
    
    private void mostrarResumen() {
        // Información de película y función
        if (sesionCompra.tienePeliculaSeleccionada()) {
            peliculaLabel.setText(sesionCompra.getPeliculaSeleccionada().getTitulo());
        } else {
            peliculaLabel.setText("-");
        }

        if (sesionCompra.tieneFuncionSeleccionada()) {
            Funcion funcion = sesionCompra.getFuncionSeleccionada();
            String fechaHora = FuncionesGenerator.formatearFechaHora(funcion.getFechaHora());
            funcionLabel.setText(fechaHora + " - Sala " + funcion.getSala().getNumero());
        } else {
            funcionLabel.setText("-");
        }

        // Asientos
        if (sesionCompra.tieneAsientosSeleccionados()) {
            asientosLabel.setText(String.join(", ", sesionCompra.getAsientosSeleccionados()));
        } else {
            asientosLabel.setText("Ninguno seleccionado");
        }

        // Productos de confitería
        if (!sesionCompra.getProductosSeleccionados().isEmpty()) {
            StringBuilder productosText = new StringBuilder();
            for (ItemCompra item : sesionCompra.getProductosSeleccionados()) {
                productosText.append(item.getNombre()).append(", ");
            }
            productosLabel.setText(productosText.substring(0, productosText.length() - 2));
        } else {
            productosLabel.setText("Ninguno");
        }

        actualizarTotal();
    }
    
    private void actualizarTotal() {
        double subtotal = sesionCompra.calcularTotal();
        subtotalLabel.setText(String.format("COP $%,.0f", subtotal));

        // Aplicar descuentos usando pattern Decorator
        co.edu.uq.cinemauq.patterns.decorator.Compra compra = new CompraBase("Compra de entradas y confitería", subtotal);

        double descuentoTotal = 0.0;

        if (chkDescuentoFijo.isSelected()) {
            compra = new DescuentoFijo(compra, 5000.0); // $5,000 COP de descuento fijo
            descuentoTotal += 5000.0;
        }

        if (chkPromocion.isSelected()) {
            compra = new Promocion(compra, "Estudiante", 10.0); // 10% de descuento
            descuentoTotal += subtotal * 0.10;
        }

        double totalFinal = compra.precio();

        descuentoLabel.setText(String.format("-COP $%,.0f", descuentoTotal));
        totalLabel.setText(String.format("COP $%,.0f", totalFinal));

        // Habilitar botón confirmar solo si hay asientos seleccionados
        btnConfirmar.setDisable(!sesionCompra.tieneAsientosSeleccionados());
    }
    
    @FXML
    private void onConfirmar() {
        System.out.println("=== DIAGNÓSTICO DE COMPRA ===");

        // Verificar sesión de compra
        if (sesionCompra == null) {
            System.err.println("❌ ERROR: Sesión de compra es null");
            return;
        }
        System.out.println("✅ Sesión de compra existe");

        // Verificar película seleccionada
        if (!sesionCompra.tienePeliculaSeleccionada()) {
            System.err.println("❌ ERROR: No hay película seleccionada");
            return;
        }
        System.out.println("✅ Película seleccionada: " + sesionCompra.getPeliculaSeleccionada().getTitulo());

        // Verificar función seleccionada
        if (!sesionCompra.tieneFuncionSeleccionada()) {
            System.err.println("❌ ERROR: No hay función seleccionada");
            return;
        }
        System.out.println("✅ Función seleccionada: " + sesionCompra.getFuncionSeleccionada().getFechaHora());

        // Verificar asientos seleccionados
        if (!sesionCompra.tieneAsientosSeleccionados()) {
            System.err.println("❌ ERROR: No hay asientos seleccionados");
            return;
        }
        System.out.println("✅ Asientos seleccionados: " + String.join(", ", sesionCompra.getAsientosSeleccionados()));

        // Verificar usuario actual
        if (Navegador.getUsuarioActual() == null) {
            System.err.println("❌ ERROR: No hay usuario logueado");
            return;
        }
        System.out.println("✅ Usuario logueado: " + Navegador.getUsuarioActual().getNombre());

        // Calcular totales
        double precioEntrada = sesionCompra.getPrecioEntrada();
        double precioProductos = sesionCompra.getPrecioProductos();
        double subtotal = sesionCompra.calcularTotal();
        System.out.println("💰 Precio entrada: $" + precioEntrada);
        System.out.println("💰 Precio productos: $" + precioProductos);
        System.out.println("💰 Subtotal: $" + subtotal);

        // Validar compra usando CompraFacade
        try {
            CompraFacade facade = new CompraFacade();
            String funcionInfo = sesionCompra.tieneFuncionSeleccionada() ?
                FuncionesGenerator.formatearFechaHora(sesionCompra.getFuncionSeleccionada().getFechaHora()) : "";

            CompraFacade.Resumen resumen = facade.prepararCompra(
                funcionInfo,
                sesionCompra.getAsientosSeleccionados(),
                sesionCompra.getProductosSeleccionados().stream().map(ItemCompra::getNombre).toList(),
                precioEntrada,
                precioProductos
            );

            System.out.println("✅ Compra validada exitosamente");
            System.out.println("📋 Detalle: " + resumen.detalle());
            System.out.println("💵 Total final: $" + resumen.total());

            // Navegar a tarjeta virtual para pago
            System.out.println("🔄 Navegando a tarjeta virtual...");
            Navegador.navegarA("/ui/views/tarjeta_virtual.fxml", "CinemaUQ - Tarjeta Virtual");
            System.out.println("✅ Navegación completada");
        } catch (IllegalArgumentException e) {
            System.err.println("❌ ERROR de validación: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ ERROR inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void onVolver() {
        Navegador.navegarA("/ui/views/confiteria.fxml", "CinemaUQ - Confitería");
    }
    
    public SesionCompra getSesionCompra() {
        return sesionCompra;
    }
}
