package co.edu.uq.cinemauq.ui.controllers;

import co.edu.uq.cinemauq.core.domain.Usuario;
import co.edu.uq.cinemauq.services.*;
import co.edu.uq.cinemauq.ui.utils.Navegador;
import javafx.fxml.FXML;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.chart.*;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import java.time.*;
import java.math.BigDecimal;
import java.util.*;

/** Presentación administrativa: formularios de catálogo y consultas financieras de solo lectura. */
public class AdminPanelController {
    @FXML private VBox contenido;
    @FXML private FlowPane navegacion;
    @FXML private Label mensaje;
    @FXML private Label titulo;
    @FXML private TextField busqueda;
    @FXML private DatePicker desde;
    @FXML private DatePicker hasta;
    @FXML private VBox layout;
    @FXML private HBox distribucion;
    @FXML private VBox zonaTrabajo;
    private AdministracionService servicio;
    private String area="Dashboard";
    private TableView<Map<String,String>> tabla;
    private final List<String> areas=List.of("Dashboard","Películas","Salas","Asientos","Funciones","Productos","Combos","Promociones","Clientes","Tarjetas","Recargas","Compras","Cancelaciones","Puntos","Reportes","Gráficos");
    @FXML private void initialize() {
        desde.setValue(LocalDate.now().withDayOfMonth(1)); hasta.setValue(LocalDate.now().plusMonths(1));
        contenido.setMinWidth(0);
        busqueda.textProperty().addListener((o,a,b)->actualizar());
        layout.widthProperty().addListener((o,a,b)->ajustarNavegacion(b.doubleValue()));
    }
    private void ajustarNavegacion(double ancho) {
        boolean lateral=ancho>=900;
        if(lateral&&navegacion.getParent()!=distribucion) {layout.getChildren().remove(navegacion);distribucion.getChildren().addFirst(navegacion);}
        if(!lateral&&navegacion.getParent()!=layout) {distribucion.getChildren().remove(navegacion);layout.getChildren().addFirst(navegacion);}
        navegacion.setOrientation(lateral?javafx.geometry.Orientation.VERTICAL:javafx.geometry.Orientation.HORIZONTAL);
        navegacion.setPrefWrapLength(lateral?2000:600);
        navegacion.setMinWidth(lateral?180:0);navegacion.setPrefWidth(lateral?190:Region.USE_COMPUTED_SIZE);navegacion.setMaxWidth(lateral?200:Double.MAX_VALUE);
        HBox.setHgrow(zonaTrabajo,Priority.ALWAYS);
    }
    public void cargarDatos(Usuario usuario) {
        try {
            // Se verifica también la identidad autenticada del navegador; no se confía en un argumento externo.
            AdministracionService.autorizar(Navegador.getUsuarioActual());
            if(usuario!=Navegador.getUsuarioActual())throw new SecurityException("Acceso denegado");
            servicio=new AdministracionService(usuario);
            navegacion.getChildren().clear();
            for(String nombre:areas) {Button b=new Button(nombre);b.getStyleClass().add("nav-item");b.setMaxWidth(Double.MAX_VALUE);b.setOnAction(e->{area=nombre;busqueda.clear();actualizar();});navegacion.getChildren().add(b);}
            actualizar();
            ajustarNavegacion(layout.getWidth());
        } catch(SecurityException e) {servicio=null;navegacion.getChildren().clear();contenido.getChildren().clear();titulo.setText("Acceso denegado");mostrar(e.getMessage(),false);}
    }
    private void mostrar(String texto,boolean exito) { mensaje.setText(texto);mensaje.getStyleClass().removeAll("mensaje-error","mensaje-exito","mensaje-info");mensaje.getStyleClass().add(exito?"mensaje-exito":"mensaje-error"); }
    private void ejecutar(Runnable tarea) {try{tarea.run();}catch(Exception e){mostrar(Objects.toString(e.getMessage(),"No se pudo completar la operación"),false);}}
    private Button boton(String texto,String clase,Runnable tarea) {Button b=new Button(texto);b.getStyleClass().add(clase);b.setOnAction(e->ejecutar(tarea));return b;}
    private Label etiqueta(String t,String clase) {Label l=new Label(t);l.setWrapText(true);l.getStyleClass().add(clase);return l;}
    @FXML private void actualizar() {
        if(servicio==null)return;
        ejecutar(()->{
            if(desde.getValue()==null||hasta.getValue()==null||desde.getValue().isAfter(hasta.getValue()))throw new IllegalArgumentException("Selecciona un intervalo válido");
            titulo.setText(area);contenido.getChildren().clear();
            navegacion.getChildren().forEach(n->{n.getStyleClass().remove("nav-item-active");if(n instanceof Button b&&b.getText().equals(area))n.getStyleClass().add("nav-item-active");});
            if(!CatalogoCinema.campos(area).isEmpty())catalogo();
            else if(area.equals("Dashboard")||area.equals("Reportes")||area.equals("Gráficos"))resumen();
            else consulta();
        });
    }
    private void catalogo() {
        List<Map<String,String>> filas=new ArrayList<>();
        for(var f:servicio.listar(area,"")) {
            if(area.equals("Funciones")) {var fecha=LocalDateTime.parse(f.valor("Fecha y hora")).toLocalDate();if(fecha.isBefore(desde.getValue())||fecha.isAfter(hasta.getValue()))continue;}
            Map<String,String> r=new LinkedHashMap<>();r.put("ID",f.id());
            f.datos().forEach((k,v)->r.put(k,referencia(k,v)));r.put("Estado",f.activa()?"ACTIVO":"INACTIVO");
            if(area.equals("Salas"))r.put("Capacidad",""+(Integer.parseInt(f.valor("Filas"))*Integer.parseInt(f.valor("Columnas"))));
            if(area.equals("Salas"))r.put("Habilitados",""+servicio.listar("Asientos","").stream().filter(a->a.valor("Sala").equals(f.id())&&a.activa()).count());
            if(r.values().stream().anyMatch(v->v.toLowerCase(Locale.ROOT).contains(busqueda.getText().toLowerCase(Locale.ROOT))))filas.add(r);
        }
        tabla=crearTabla(filas);contenido.getChildren().add(tabla);
        FlowPane acciones=new FlowPane(10,10);
        acciones.getChildren().addAll(boton("Crear "+area.toLowerCase(Locale.ROOT),"boton-principal",()->formulario(null)),boton("Editar selección","boton-secundario",()->formulario(seleccion().get("ID"))));
        if(area.equals("Funciones"))acciones.getChildren().add(boton("Cancelar función · reembolso 100 %","boton-peligro",()->{
            String id=seleccion().get("ID"); TextInputDialog d=new TextInputDialog();d.setTitle("Cancelar función");d.setHeaderText("Bloquear ventas y reembolsar todas sus compras a la tarjeta original");d.setContentText("Motivo:");
            d.showAndWait().ifPresent(m->ejecutar(()->{int n=servicio.cancelarFuncion(id,m);actualizar();mostrar("Función cancelada. Compras reembolsadas: "+n,true);}));
        }));
        else acciones.getChildren().add(boton("Activar / desactivar","boton-secundario",()->{String id=seleccion().get("ID");var f=CatalogoCinema.getInstance().ficha(id);servicio.activar(id,!f.activa());actualizar();mostrar("Estado actualizado",true);}));
        contenido.getChildren().addAll(acciones,etiqueta("Selecciona una fila para editar. Los identificadores son de solo lectura.","app-subtitle"));
        if(area.equals("Salas")) {
            acciones.getChildren().add(boton("Completar distribución","boton-secundario",()->{int n=servicio.completarAsientos(seleccion().get("ID"));actualizar();mostrar("Asientos creados: "+n,true);}));
            contenido.getChildren().add(etiqueta("Las salas nuevas permiten crear asientos individualmente en Asientos o completar toda su distribución. Capacidad indica posiciones; Habilitados indica los asientos utilizables.","mensaje-info"));
        }
        if(area.equals("Asientos"))plano();
    }
    private String referencia(String campo,String valor) {
        if(campo.equals("Sala")||campo.equals("Película")) {try{return CatalogoCinema.getInstance().ficha(valor).toString();}catch(IllegalArgumentException e){return valor;}}
        if(campo.equals("Accesible"))return valor.equals("true")?"Sí":"No";
        if(campo.equals("Productos y cantidades")) return java.util.Arrays.stream(valor.split(",")).map(parte->{String[] par=parte.split(":");return CatalogoCinema.getInstance().ficha(par[0]).valor("Nombre")+" ×"+par[1];}).collect(java.util.stream.Collectors.joining(", "));
        return valor;
    }
    private void plano() {
        ComboBox<CatalogoCinema.Ficha> salas=new ComboBox<>();salas.getItems().setAll(servicio.listar("Salas",""));salas.setPromptText("Consultar distribución de sala");
        TilePane plano=new TilePane(6,6);plano.setPrefColumns(10);plano.getStyleClass().add("admin-seat-plan");
        salas.setOnAction(e->{plano.getChildren().clear();if(salas.getValue()==null)return;
            for(var a:servicio.listar("Asientos",""))if(a.valor("Sala").equals(salas.getValue().id())) {
                Button b=boton(a.valor("Código")+(a.activa()?"":" ×")+(a.valor("Accesible").equals("true")?" ♿":""),a.activa()?"boton-secundario":"boton-peligro",()->formulario(a.id()));b.setAccessibleText("Asiento "+a.valor("Código")+" "+(a.activa()?"habilitado":"deshabilitado"));plano.getChildren().add(b);
            }});
        contenido.getChildren().addAll(etiqueta("Distribución · × deshabilitado · ♿ accesible","section-title"),salas,plano);
    }
    private Map<String,String> seleccion() {var r=tabla.getSelectionModel().getSelectedItem();if(r==null)throw new IllegalArgumentException("Selecciona una fila");return r;}
    private void formulario(String id) {
        var existente=id==null?null:CatalogoCinema.getInstance().ficha(id);
        Dialog<ButtonType> dialog=new Dialog<>();dialog.setTitle(id==null?"Crear · "+area:"Editar · "+area);dialog.setHeaderText("Completa los datos del catálogo");
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/ui/styles/estilos.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().addAll("root-app","uniform-page");
        VBox form=new VBox(10);form.getStyleClass().add("form-card");Map<String,SupplierValor> campos=new LinkedHashMap<>();
        for(String campo:CatalogoCinema.campos(area)) {
            String valor=existente==null?"":existente.valor(campo);form.getChildren().add(etiqueta(campo,"form-label"));
            if(campo.equals("Sala")||campo.equals("Película")) {
                ComboBox<CatalogoCinema.Ficha> cb=new ComboBox<>();cb.setMaxWidth(Double.MAX_VALUE);cb.getItems().setAll(servicio.listar(campo.equals("Sala")?"Salas":"Películas",""));cb.getItems().stream().filter(f->f.id().equals(valor)).findFirst().ifPresent(cb::setValue);form.getChildren().add(cb);campos.put(campo,()->cb.getValue()==null?"":cb.getValue().id());
            } else if(campo.equals("Productos y cantidades")) {
                VBox productos=new VBox(6);Map<String,Spinner<Integer>> cantidades=new LinkedHashMap<>();
                for(var p:servicio.listar("Productos","")) {int cantidad=0;for(String parte:valor.split(",")){String[] v=parte.split(":");if(v.length==2&&v[0].equals(p.id()))cantidad=Integer.parseInt(v[1]);}
                    Spinner<Integer> n=new Spinner<>(0,100,cantidad);n.setPrefWidth(100);cantidades.put(p.id(),n);HBox fila=new HBox(10,etiqueta(p.valor("Nombre"),"form-label"),n);productos.getChildren().add(fila);}
                form.getChildren().add(productos);campos.put(campo,()->cantidades.entrySet().stream().filter(e->e.getValue().getValue()>0).map(e->e.getKey()+":"+e.getValue().getValue()).collect(java.util.stream.Collectors.joining(",")));
            } else if(campo.equals("Accesible")||campo.equals("Tipo")) {
                ComboBox<String> cb=new ComboBox<>();cb.getItems().setAll(campo.equals("Tipo")?List.of("FIJO","PORCENTAJE"):List.of("false","true"));cb.setValue(valor.isEmpty()?cb.getItems().getFirst():valor);form.getChildren().add(cb);campos.put(campo,cb::getValue);
            } else if(campo.equals("Desde")||campo.equals("Hasta")) {
                DatePicker dp=new DatePicker(valor.isEmpty()?LocalDate.now():LocalDate.parse(valor));form.getChildren().add(dp);campos.put(campo,()->dp.getValue()==null?"":dp.getValue().toString());
            } else if(campo.equals("Fecha y hora")) {
                LocalDateTime inicio=valor.isEmpty()?LocalDate.now().plusDays(1).atTime(18,0):LocalDateTime.parse(valor);
                DatePicker fecha=new DatePicker(inicio.toLocalDate());TextField hora=new TextField(inicio.toLocalTime().toString());hora.setPromptText("HH:mm");hora.setPrefColumnCount(6);form.getChildren().add(new HBox(10,fecha,hora));
                campos.put(campo,()->{if(fecha.getValue()==null)throw new IllegalArgumentException("Selecciona una fecha");try{return fecha.getValue().atTime(LocalTime.parse(hora.getText())).toString();}catch(java.time.format.DateTimeParseException e){throw new IllegalArgumentException("Hora en formato HH:mm");}});
            } else {TextField tf=new TextField(valor);tf.setPromptText(campo);form.getChildren().add(tf);campos.put(campo,tf::getText);}
        }
        CheckBox activo=new CheckBox("Activo / habilitado");activo.setSelected(existente==null||existente.activa());form.getChildren().add(activo);
        if(area.equals("Funciones")) {activo.setDisable(true);activo.setSelected(true);}
        Label error=etiqueta("","mensaje-error");form.getChildren().add(error);
        ScrollPane scroll=new ScrollPane(form);scroll.setFitToWidth(true);scroll.setPrefViewportHeight(440);scroll.setPrefViewportWidth(430);dialog.getDialogPane().setContent(scroll);
        ButtonType guardar=new ButtonType("Guardar",ButtonBar.ButtonData.OK_DONE);dialog.getDialogPane().getButtonTypes().addAll(guardar,ButtonType.CANCEL);
        dialog.getDialogPane().lookupButton(guardar).addEventFilter(javafx.event.ActionEvent.ACTION,e->{try{Map<String,String>d=new LinkedHashMap<>();campos.forEach((k,v)->d.put(k,v.get()));servicio.guardar(area,id,d,activo.isSelected());actualizar();mostrar("Registro guardado",true);}catch(Exception ex){error.setText(ex.getMessage());e.consume();}});
        dialog.showAndWait();
    }
    @FunctionalInterface private interface SupplierValor {String get();}
    private TableView<Map<String,String>> crearTabla(List<Map<String,String>> filas) {
        TableView<Map<String,String>> t=new TableView<>();t.setMinWidth(0);t.setPrefHeight(390);t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);t.setPlaceholder(etiqueta("No hay registros para estos filtros.","empty-state"));
        Set<String> columnas=new LinkedHashSet<>();filas.forEach(r->columnas.addAll(r.keySet()));
        List<String> visibles=switch(area) {
            case "Compras" -> List.of("ID","Cliente","Película","Pagado","Fecha","Estado");
            case "Cancelaciones" -> List.of("ID","Cliente","Pagado","Porcentaje","Reembolso","Estado");
            case "Clientes" -> List.of("ID","Nombre","Correo","Saldo","Puntos disponibles");
            case "Puntos" -> List.of("Cliente","Originales","Restantes","Obtención","Vencimiento","Estado");
            default -> new ArrayList<>(columnas);
        };
        for(String c:visibles){TableColumn<Map<String,String>,String> col=new TableColumn<>(c);col.setCellValueFactory(v->new SimpleStringProperty(v.getValue().getOrDefault(c,"")));col.setMinWidth(45);col.setPrefWidth(c.equals("ID")?100:135);t.getColumns().add(col);}
        t.getItems().setAll(filas);return t;
    }
    private void consulta() {
        tabla=crearTabla(servicio.consultar(area,desde.getValue(),hasta.getValue(),busqueda.getText()));contenido.getChildren().add(tabla);
        contenido.getChildren().add(etiqueta("Historial de solo lectura. Selecciona una fila para consultar sus detalles completos.","app-subtitle"));
        contenido.getChildren().add(boton("Ver detalle","boton-secundario",()->{
            var fila=seleccion();String texto=fila.entrySet().stream().map(e->e.getKey()+": "+e.getValue()).collect(java.util.stream.Collectors.joining("\n"));
            if(area.equals("Clientes"))texto=servicio.detalleCliente(fila.get("ID"));
            if(area.equals("Tarjetas"))texto=servicio.detalleCliente(fila.get("Cliente ID"));
            TextArea detalle=new TextArea(texto);detalle.setEditable(false);detalle.setWrapText(true);Alert a=new Alert(Alert.AlertType.INFORMATION);a.setTitle("Detalle · "+area);a.setHeaderText("Consulta administrativa");a.getDialogPane().setContent(detalle);a.showAndWait();
        }));
        if(area.equals("Tarjetas")||area.equals("Recargas")) {
            ComboBox<co.edu.uq.cinemauq.core.domain.Cliente> cliente=new ComboBox<>();cliente.getItems().setAll(servicio.clientes(""));cliente.setPromptText("Cliente a recargar");cliente.setConverter(new StringConverter<>(){public String toString(co.edu.uq.cinemauq.core.domain.Cliente c){return c==null?"":c.getNombre()+" · "+c.getId();}public co.edu.uq.cinemauq.core.domain.Cliente fromString(String s){return null;}});
            TextField monto=new TextField();monto.setPromptText("Importe COP");FlowPane f=new FlowPane(10,10,cliente,monto,boton("Recarga administrativa","boton-principal",()->{if(cliente.getValue()==null)throw new IllegalArgumentException("Selecciona un cliente");servicio.recargar(cliente.getValue().getId(),new BigDecimal(monto.getText()));actualizar();mostrar("Recarga registrada",true);}));contenido.getChildren().add(f);
        }
    }
    private void resumen() {
        var datos=servicio.indicadores(desde.getValue(),hasta.getValue());
        contenido.getChildren().add(etiqueta("Catálogo y puntos: estado actual. Operaciones: intervalo seleccionado. Ocupación: funciones del intervalo.","app-subtitle"));
        if(area.equals("Dashboard")) {TilePane tarjetas=new TilePane(14,14);tarjetas.setPrefColumns(3);
            datos.forEach((k,v)->{VBox card=new VBox(8,etiqueta(k,"stat-label"),etiqueta(String.format(java.util.Locale.forLanguageTag("es-CO"),"%,.2f",v),"stat-value"));card.getStyleClass().add("stat-card");card.setPrefWidth(215);tarjetas.getChildren().add(card);});contenido.getChildren().add(tarjetas);}
        if(area.equals("Gráficos")) {
            CategoryAxis x=new CategoryAxis();NumberAxis y=new NumberAxis();BarChart<String,Number> grafico=new BarChart<>(x,y);grafico.setTitle("Ventas y reembolsos · COP");grafico.setLegendVisible(false);grafico.setMinWidth(0);grafico.setAnimated(false);XYChart.Series<String,Number> serie=new XYChart.Series<>();for(String k:List.of("Ventas cobradas","Reembolsos","Recargas"))serie.getData().add(new XYChart.Data<>(k,datos.get(k)));grafico.getData().add(serie);contenido.getChildren().add(grafico);
            PieChart pie=new PieChart();pie.setTitle("Estado de puntos · situación actual");pie.setAnimated(false);for(String k:List.of("Puntos disponibles","Puntos vencidos","Puntos redimidos"))if(datos.get(k)>0)pie.getData().add(new PieChart.Data(k,datos.get(k)));if(pie.getData().isEmpty())contenido.getChildren().add(etiqueta("Todavía no hay puntos para graficar.","empty-state"));else contenido.getChildren().add(pie);
        }
        if(area.equals("Reportes")) {TextArea texto=new TextArea(servicio.reporte(desde.getValue(),hasta.getValue()));texto.setEditable(false);texto.setWrapText(true);texto.setPrefRowCount(22);contenido.getChildren().addAll(texto,boton("Exportar TXT","boton-principal",()->{
            FileChooser fc=new FileChooser();fc.setTitle("Exportar reporte");fc.setInitialFileName("cinemauq-reporte.txt");fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Texto UTF-8","*.txt"));var archivo=fc.showSaveDialog(contenido.getScene().getWindow());if(archivo!=null){try{servicio.exportarTxt(archivo.toPath(),desde.getValue(),hasta.getValue());mostrar("Reporte exportado: "+archivo.getName(),true);}catch(java.io.IOException e){throw new IllegalStateException("No fue posible exportar: "+e.getMessage(),e);}}
        }));}
    }
}
