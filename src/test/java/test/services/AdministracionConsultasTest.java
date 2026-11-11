package test.services;
import co.edu.uq.cinemauq.core.domain.*;
import co.edu.uq.cinemauq.core.managers.ReservasManager;
import co.edu.uq.cinemauq.core.seed.UserSeeder;
import co.edu.uq.cinemauq.services.*;
import co.edu.uq.cinemauq.ui.models.SesionCompra;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AdministracionConsultasTest {
    CatalogoCinema catalogo=new CatalogoCinema();ReservasManager registro=new ReservasManager();
    AdministracionService admin=new AdministracionService(new Administrador("A","Admin","a@c.co"),catalogo,registro);
    LocalDate desde=LocalDate.now().minusDays(1),hasta=LocalDate.now().plusDays(5);
    @Test void compraPagoConsultaYReembolsoMantienenIdEImporte() {
        Cliente c=new Cliente(UUID.randomUUID().toString(),"Compra administrativa","c@c.co");c.getTarjeta().recargar(100000);c.agregarPuntos(500);
        var f=catalogo.funcionesDisponibles().getFirst();var s=new SesionCompra(catalogo);s.setPeliculaSeleccionada(f.getPelicula());s.setFuncionSeleccionada(f);s.setPrecioEntrada(catalogo.precio(f));s.agregarAsiento("A1");
        var pagos=new PagoCompraService(new PuntosService(),registro,catalogo);pagos.prepararPuntos(c,s,200);var recibo=pagos.pagar(c,s);
        var fila=admin.consultar("Compras",desde,hasta,recibo.idCompra()).getFirst();assertEquals(recibo.idCompra(),fila.get("ID"));assertEquals("59990.00",fila.get("Pagado"));assertEquals("200",fila.get("Puntos usados"));
        int puntos=c.getPuntos();admin.cancelarFuncion(f.getId(),"Cancelada por el cine");
        var cancelada=admin.consultar("Cancelaciones",desde,hasta,recibo.idCompra()).getFirst();assertEquals("REEMBOLSADA",cancelada.get("Estado"));assertEquals("59990.0",cancelada.get("Reembolso"));assertEquals(puntos,c.getPuntos());
        assertEquals("REEMBOLSADA",new AdministracionService(new Administrador("B","Otro","b@c.co"),catalogo,registro).consultar("Compras",desde,hasta,recibo.idCompra()).getFirst().get("Estado"));
    }
    @Test void consultaLotesIncluyeTodosLosEstadosSinEditarlos() {
        Cliente c=new Cliente(UUID.randomUUID().toString(),"Lotes admin","l@c.co");UserSeeder.agregarUsuario(c,"test");
        c.agregarPuntos(50);c.agregarPuntos(new PuntoCinemaUQ(30,LocalDate.now().minusYears(2)));PuntoCinemaUQ r=new PuntoCinemaUQ(20,LocalDate.now());r.redimir(20);c.agregarPuntos(r);
        var filas=admin.consultar("Puntos",LocalDate.now().minusYears(3),hasta,"Lotes admin");
        assertEquals(Set.of("DISPONIBLE","VENCIDO","REDIMIDO"),filas.stream().map(x->x.get("Estado")).collect(java.util.stream.Collectors.toSet()));
        assertEquals(50,c.getPuntos());assertEquals(3,c.getPuntosAcumulados().size());
    }
    @Test void fechasYBusquedaFiltranSinInventarCompras() {
        assertTrue(admin.consultar("Compras",desde,hasta,"inexistente").isEmpty());
        assertThrows(IllegalArgumentException.class,()->admin.consultar("Compras",hasta,desde,""));
        assertThrows(IllegalArgumentException.class,()->admin.guardar("Compras",null,Map.of(),true));
    }
}
