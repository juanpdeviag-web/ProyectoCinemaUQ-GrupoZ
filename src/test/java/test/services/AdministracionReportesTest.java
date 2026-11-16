package test.services;
import co.edu.uq.cinemauq.core.domain.*;
import co.edu.uq.cinemauq.core.managers.ReservasManager;
import co.edu.uq.cinemauq.services.*;
import co.edu.uq.cinemauq.ui.models.SesionCompra;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class AdministracionReportesTest {
    CatalogoCinema catalogo=new CatalogoCinema();ReservasManager registro=new ReservasManager();
    AdministracionService admin=new AdministracionService(new Administrador("A","Admin","a@c.co"),catalogo,registro);
    LocalDate hoy=LocalDate.now();
    @TempDir Path carpeta;
    private SesionCompra vender() {
        var f=catalogo.funcionesDisponibles().getFirst();var s=new SesionCompra(catalogo);s.setPeliculaSeleccionada(f.getPelicula());s.setFuncionSeleccionada(f);s.setPrecioEntrada(catalogo.precio(f));s.agregarAsiento("A1");s.agregarProducto(catalogo.confiteria().getFirst());s.agregarProducto(catalogo.confiteria().getFirst());
        Cliente c=new Cliente("C","Cliente","c@c.co");c.getTarjeta().recargar(200000);new PagoCompraService(new PuntosService(),registro,catalogo).pagar(c,s);return s;
    }
    @Test void exportacionTxtIncluyeDatosRealesYEsUtf8() throws Exception {
        var s=vender();Path salida=carpeta.resolve("reporte.txt");admin.exportarTxt(salida,hoy,hoy.plusDays(5));String texto=Files.readString(salida);
        assertTrue(texto.contains("CinemaUQ"));assertTrue(texto.contains(s.getIdCompra()));assertTrue(texto.contains("CONFITERÍA"));assertTrue(texto.contains("Palomitas"));assertTrue(texto.contains("Cantidad=2"));assertTrue(texto.contains("ENTRADAS Y OCUPACIÓN"));
    }
    @Test void filtrosExcluyenVentasFueraDeFecha() {
        vender();assertEquals(1,admin.indicadores(hoy,hoy).get("Compras"));assertEquals(0,admin.indicadores(hoy.minusDays(5),hoy.minusDays(1)).get("Compras"));
        assertTrue(admin.consultar("Compras",hoy.minusDays(5),hoy.minusDays(1),"").isEmpty());
    }
    @Test void ocupacionSeMidePorFechaDeFuncionYRespetaCancelacion() {
        var s=vender();LocalDate funcion=s.getFuncionSeleccionada().getFechaHora().toLocalDate();
        assertTrue(admin.indicadores(funcion,funcion).get("Ocupación %")>0);
        admin.cancelarFuncion(s.getFuncionSeleccionada().getId(),"Mantenimiento");
        assertEquals(0,admin.indicadores(funcion,funcion).get("Ocupación %"));assertEquals(124000,admin.indicadores(hoy,hoy).get("Reembolsos"));
    }
    @Test void detalleConfiteriaSeConservaAlEditarPrecioDelCatalogo() {
        vender();var p=admin.listar("Productos","").getFirst();var d=new HashMap<>(p.datos());d.put("Precio","1");admin.guardar("Productos",p.id(),d,true);
        var fila=admin.consultar("Confitería",hoy,hoy,"").getFirst();assertEquals("32000.0",fila.get("Precio unitario"));assertEquals("2",fila.get("Cantidad"));
    }
}
