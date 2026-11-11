package co.edu.uq.cinemauq.ui.controllers;

import co.edu.uq.cinemauq.core.domain.Administrador;
import co.edu.uq.cinemauq.core.domain.Cliente;
import co.edu.uq.cinemauq.core.domain.Usuario;
import co.edu.uq.cinemauq.ui.utils.Navegador;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

/** Componente de presentación: encabezado y rutas visibles según el usuario. */
public class EncabezadoCinema extends StackPane {
    private final Label tituloLabel = new Label();
    private final Label subtituloLabel = new Label();
    private final FlowPane enlaces = new FlowPane(8, 8);
    private String activa = "";

    public EncabezadoCinema() {
        getStyleClass().add("shared-header");
        VBox contenido = new VBox(12);
        contenido.setMaxWidth(1300);
        tituloLabel.getStyleClass().add("app-title");
        tituloLabel.setWrapText(true);
        tituloLabel.setMinWidth(0);
        tituloLabel.setMaxWidth(Double.MAX_VALUE);
        subtituloLabel.getStyleClass().add("app-subtitle");
        subtituloLabel.setWrapText(true);
        VBox marca = new VBox(5, tituloLabel, subtituloLabel);
        marca.setMinWidth(0);
        HBox.setHgrow(marca, Priority.ALWAYS);
        VBox perfil = new VBox(5);
        perfil.setAlignment(Pos.TOP_RIGHT);
        perfil.setMinWidth(140);
        perfil.setPrefWidth(160);
        perfil.setMaxWidth(200);
        Usuario usuario = Navegador.getUsuarioActual();
        if (usuario != null) {
            Label nombre = new Label(usuario.getNombre());
            nombre.setWrapText(true);
            nombre.getStyleClass().add("user-name");
            Label rol = new Label(usuario instanceof Administrador ? "Administrador" : "Cliente");
            rol.getStyleClass().add("user-role");
            Button salir = new Button("Cerrar sesión");
            salir.getStyleClass().add("logout-button");
            salir.setOnAction(e -> Navegador.cerrarSesion());
            perfil.getChildren().addAll(nombre, rol, salir);
        } else {
            Button ingresar = new Button("Iniciar sesión");
            ingresar.getStyleClass().add("logout-button");
            ingresar.setOnAction(e -> Navegador.navegarA("/ui/views/login.fxml", "CinemaUQ - Iniciar sesión"));
            perfil.getChildren().add(ingresar);
        }
        HBox linea = new HBox(16, marca, perfil);
        enlaces.getStyleClass().add("main-navigation");
        enlace("Cartelera", "cartelera");
        if (usuario instanceof Cliente) {
            enlace("Mi cuenta", "mi_cuenta");
            enlace("Mis puntos", "mis_puntos");
            enlace("Tarjeta virtual", "tarjeta_virtual");
            enlace("Mis tickets", "mis_tickets");
        } else if (usuario instanceof Administrador) {
            enlace("Panel administrador", "admin_panel");
        }
        contenido.getChildren().addAll(linea, enlaces);
        getChildren().add(contenido);
    }

    private void enlace(String texto, String vista) {
        Button boton = new Button(texto);
        boton.setUserData(vista);
        boton.getStyleClass().add("nav-item");
        boton.setOnAction(e -> Navegador.navegarA("/ui/views/" + vista + ".fxml", "CinemaUQ - " + texto));
        enlaces.getChildren().add(boton);
    }

    public String getTitulo() { return tituloLabel.getText(); }
    public void setTitulo(String titulo) { tituloLabel.setText(titulo); }
    public String getSubtitulo() { return subtituloLabel.getText(); }
    public void setSubtitulo(String subtitulo) { subtituloLabel.setText(subtitulo); }
    public String getActiva() { return activa; }
    public void setActiva(String activa) {
        this.activa = activa;
        enlaces.getChildren().forEach(n -> {
            n.getStyleClass().remove("nav-item-active");
            if (activa.equals(n.getUserData())) {
                n.getStyleClass().add("nav-item-active");
                n.setAccessibleText(((Button) n).getText() + ", pantalla actual");
            }
        });
    }
}
