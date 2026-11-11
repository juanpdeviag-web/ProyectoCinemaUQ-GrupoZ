package co.edu.uq.cinemauq.ui.controllers;

import co.edu.uq.cinemauq.core.domain.Funcion;
import co.edu.uq.cinemauq.patterns.composite.ItemCompra;



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

    @FXML private Label combosLabel;
    @FXML private Label mensajeLabel;
    @FXML private Label disponiblesPuntosLabel;
    @FXML private Label equivalenciaPuntosLabel;
    @FXML private Label descuentoPuntosLabel;
    @FXML private Label redencionPreparadaLabel;
    @FXML private javafx.scene.control.TextField puntosField;
    @FXML private Button btnAplicarPuntos;
    @FXML private Button btnQuitarPuntos;
    private final co.edu.uq.cinemauq.services.PagoCompraService pagoCompra = new co.edu.uq.cinemauq.services.PagoCompraService();

    @FXML
    private Label subtotalLabel;

    @FXML
    private Label descuentoLabel;

    @FXML
    private Label totalLabel;

    @FXML private VBox promocionesContainer;

    @FXML
    private Button btnConfirmar;

    @FXML
    private Button btnVolver;

    @FXML
    private VBox resumenContainer;

    private SesionCompra sesionCompra;

    @Override
    public void initialize(URL location, ResourceBundle resources) {


    }

    public void cargarDatos(SesionCompra sesionCompra) {
        this.sesionCompra = sesionCompra;
        cargarPromociones();
        mostrarResumen();
    }

    private void cargarPromociones() {
        promocionesContainer.getChildren().clear();
        for (var p : co.edu.uq.cinemauq.services.CatalogoCinema.getInstance().promocionesVigentes()) {
            CheckBox opcion = new CheckBox(p.valor("Nombre") + " · " + p.valor("Valor")
                    + (p.valor("Tipo").equals("FIJO") ? " COP" : " %"));
            opcion.setWrapText(true);
            opcion.setSelected(sesionCompra.getPromocionesSeleccionadas().contains(p.id()));
            opcion.setOnAction(e -> { sesionCompra.seleccionarPromocion(p.id(), opcion.isSelected()); actualizarTotal(); });
            promocionesContainer.getChildren().add(opcion);
        }
        if (promocionesContainer.getChildren().isEmpty()) promocionesContainer.getChildren().add(new Label("No hay promociones vigentes."));
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
        productosLabel.setText(sesionCompra.getProductosSeleccionados().stream()
                .filter(item -> !(item instanceof co.edu.uq.cinemauq.patterns.composite.Combo))
                .map(ItemCompra::getNombre).collect(java.util.stream.Collectors.joining(", ")));
        combosLabel.setText(sesionCompra.getProductosSeleccionados().stream()
                .filter(item -> item instanceof co.edu.uq.cinemauq.patterns.composite.Combo)
                .map(ItemCompra::getNombre).collect(java.util.stream.Collectors.joining(", ")));
        if (productosLabel.getText().isEmpty()) productosLabel.setText("Ninguno");
        if (combosLabel.getText().isEmpty()) combosLabel.setText("Ninguno");
    }

    private void actualizarTotal() {
        if (sesionCompra == null) return;

        if (sesionCompra.getPuntosAplicados() > 0 && Navegador.getUsuarioActual() instanceof co.edu.uq.cinemauq.core.domain.Cliente cliente) {
            try {
                pagoCompra.prepararPuntos(cliente, sesionCompra, sesionCompra.getPuntosAplicados());
            } catch (RuntimeException e) {
                sesionCompra.quitarPuntos();
                mostrarError(e.getMessage());
            }
        }
        java.math.BigDecimal subtotal = java.math.BigDecimal.valueOf(sesionCompra.calcularTotal());
        subtotalLabel.setText(moneda(subtotal));
        descuentoLabel.setText("-" + moneda(subtotal.subtract(sesionCompra.calcularTotalConPromociones())));
        descuentoPuntosLabel.setText("-" + moneda(sesionCompra.getValorDescuentoPuntos()));
        totalLabel.setText(moneda(sesionCompra.calcularTotalAPagar()));
        boolean esCliente = Navegador.getUsuarioActual() instanceof co.edu.uq.cinemauq.core.domain.Cliente;
        if (esCliente) {
            var cliente = (co.edu.uq.cinemauq.core.domain.Cliente) Navegador.getUsuarioActual();
            disponiblesPuntosLabel.setText("Disponibles: " + cliente.getPuntos() + " puntos");
            equivalenciaPuntosLabel.setText("Equivalencia disponible: " + moneda(co.edu.uq.cinemauq.services.PagoCompraService.equivalencia(cliente.getPuntos())));
        } else {
            disponiblesPuntosLabel.setText("Inicia sesión como cliente para utilizar puntos.");
            equivalenciaPuntosLabel.setText("");
        }
        puntosField.setDisable(!esCliente);
        btnAplicarPuntos.setDisable(!esCliente || !sesionCompra.tieneAsientosSeleccionados());
        btnQuitarPuntos.setVisible(sesionCompra.getPuntosAplicados() > 0);
        btnQuitarPuntos.setManaged(btnQuitarPuntos.isVisible());
        redencionPreparadaLabel.setText(sesionCompra.getPuntosAplicados() > 0
                ? "Redención pendiente: " + sesionCompra.getPuntosAplicados() + " puntos · " + moneda(sesionCompra.getValorDescuentoPuntos())
                : "Los puntos se consumen únicamente al confirmar un pago exitoso.");
        btnConfirmar.setDisable(!sesionCompra.tieneAsientosSeleccionados() || !esCliente);
    }

    private String moneda(java.math.BigDecimal valor) {
        return String.format(java.util.Locale.forLanguageTag("es-CO"), "COP $%,.2f", valor);
    }

    @FXML
    private void onAplicarPuntos() {
        try {
            if (!(Navegador.getUsuarioActual() instanceof co.edu.uq.cinemauq.core.domain.Cliente cliente)) {
                throw new IllegalArgumentException("Inicia sesión como cliente para aplicar puntos");
            }
            String texto = puntosField.getText().trim();
            if (!texto.matches("[0-9]+")) throw new IllegalArgumentException("Ingresa una cantidad entera de puntos mayor que cero");
            int solicitados = Integer.parseInt(texto);
            int aplicados = pagoCompra.prepararPuntos(cliente, sesionCompra, solicitados);
            puntosField.setText(String.valueOf(aplicados));
            actualizarTotal();
            mensajeLabel.getStyleClass().setAll("mensaje-info");
            mensajeLabel.setText(aplicados < solicitados
                    ? "Se prepararon " + aplicados + " puntos: es el máximo utilizable para esta compra. Tus lotes aún no se han consumido."
                    : "Se prepararon " + aplicados + " puntos. Tus lotes aún no se han consumido.");
            mensajeLabel.setVisible(true);
            mensajeLabel.setManaged(true);
        } catch (NumberFormatException e) {
            mostrarError("La cantidad de puntos debe ser un entero válido.");
        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    @FXML
    private void onQuitarPuntos() {
        sesionCompra.quitarPuntos();
        puntosField.clear();
        actualizarTotal();
        mensajeLabel.setVisible(false);
        mensajeLabel.setManaged(false);
    }
    @FXML
    private void onConfirmar() {
        System.out.println("=== DIAGNÓSTICO DE COMPRA ===");

        // Verificar sesión de compra
        if (sesionCompra == null) {
            System.err.println("❌ ERROR: Sesión de compra es null");
            mostrarError("❌ ERROR: Sesión de compra es null");
            return;
        }
        System.out.println("✅ Sesión de compra existe");

        // Verificar película seleccionada
        if (!sesionCompra.tienePeliculaSeleccionada()) {
            System.err.println("❌ ERROR: No hay película seleccionada");
            mostrarError("❌ ERROR: No hay película seleccionada");
            return;
        }
        System.out.println("✅ Película seleccionada: " + sesionCompra.getPeliculaSeleccionada().getTitulo());

        // Verificar función seleccionada
        if (!sesionCompra.tieneFuncionSeleccionada()) {
            System.err.println("❌ ERROR: No hay función seleccionada");
            mostrarError("❌ ERROR: No hay función seleccionada");
            return;
        }
        System.out.println("✅ Función seleccionada: " + sesionCompra.getFuncionSeleccionada().getFechaHora());

        // Verificar asientos seleccionados
        if (!sesionCompra.tieneAsientosSeleccionados()) {
            System.err.println("❌ ERROR: No hay asientos seleccionados");
            mostrarError("❌ ERROR: No hay asientos seleccionados");
            return;
        }
        System.out.println("✅ Asientos seleccionados: " + String.join(", ", sesionCompra.getAsientosSeleccionados()));

        // Verificar usuario actual
        if (Navegador.getUsuarioActual() == null) {
            System.err.println("❌ ERROR: No hay usuario logueado");
            mostrarError("❌ ERROR: No hay usuario logueado");
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
            mostrarError("❌ ERROR de validación: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ ERROR inesperado: " + e.getMessage());
            mostrarError("❌ ERROR inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onVolver() {
        Navegador.navegarA("/ui/views/confiteria.fxml", "CinemaUQ - Confitería");
    }

    private void mostrarError(String texto) {
        mensajeLabel.setText(texto);
        mensajeLabel.getStyleClass().setAll("mensaje-error");
        mensajeLabel.setVisible(true);
        mensajeLabel.setManaged(true);
    }
    public SesionCompra getSesionCompra() {
        return sesionCompra;
    }
}
