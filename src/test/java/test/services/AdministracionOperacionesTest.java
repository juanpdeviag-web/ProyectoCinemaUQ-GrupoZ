package test.services;
import co.edu.uq.cinemauq.core.domain.*;
import co.edu.uq.cinemauq.core.managers.ReservasManager;
import co.edu.uq.cinemauq.core.seed.UserSeeder;
import co.edu.uq.cinemauq.services.*;
import co.edu.uq.cinemauq.ui.models.SesionCompra;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AdministracionOperacionesTest {
    CatalogoCinema catalogo=new CatalogoCinema();ReservasManager registro=new ReservasManager();
    AdministracionService admin=new AdministracionService(new Administrador("A","Admin","a@c.co"),catalogo,registro);
    @Test void promocionAdministrativaAfectaCobroRealYPuntosGenerados() {
        var p=admin.guardar("Promociones",null,Map.of("Nombre","Especial","Tipo","PORCENTAJE","Valor","20","Desde",LocalDate.now().toString(),"Hasta",LocalDate.now().plusDays(2).toString()),true);
        Cliente c=new Cliente("C","Ana","c@c.co");c.getTarjeta().recargar(100000);c.agregarPuntos(200);
        var f=catalogo.funcionesDisponibles().getFirst();var s=new SesionCompra(catalogo);s.setPeliculaSeleccionada(f.getPelicula());s.setFuncionSeleccionada(f);s.setPrecioEntrada(catalogo.precio(f));s.agregarAsiento("A1");s.seleccionarPromocion(p.id(),true);
        var pagos=new PagoCompraService(new PuntosService(),registro,catalogo);pagos.prepararPuntos(c,s,200);var resultado=pagos.pagar(c,s);
        assertEquals(new BigDecimal("47990.00"),resultado.cobrado());assertEquals(479,resultado.puntosGenerados());assertEquals(200,registro.buscarCompra(resultado.idCompra()).detallePago().puntos());
        assertTrue(registro.buscarCompra(resultado.idCompra()).detallePago().promociones().contains("Especial"));
    }
    @Test void vigenciaYDesactivacionDePromociones() {
        var p=admin.guardar("Promociones",null,Map.of("Nombre","Futuro","Tipo","FIJO","Valor","100","Desde",LocalDate.now().plusDays(2).toString(),"Hasta",LocalDate.now().plusDays(3).toString()),true);
        assertEquals(new BigDecimal("1000.00"),catalogo.aplicarPromociones(BigDecimal.valueOf(1000),List.of(p.id())));
        var d=new HashMap<>(p.datos());d.put("Desde",LocalDate.now().toString());admin.guardar("Promociones",p.id(),d,true);
        assertEquals(new BigDecimal("900.00"),catalogo.aplicarPromociones(BigDecimal.valueOf(1000),List.of(p.id())));
        admin.activar(p.id(),false);assertEquals(new BigDecimal("1000.00"),catalogo.aplicarPromociones(BigDecimal.valueOf(1000),List.of(p.id())));
    }
    @Test void recargaAdministrativaRegistraMovimientoYRechazaImporteInvalido() {
        var c=UserSeeder.obtenerClientes().getFirst();double antes=c.getTarjeta().getSaldo();int n=c.getTarjeta().obtenerHistorial().size();
        admin.recargar(c.getId(),new BigDecimal("1234.50"));assertEquals(antes+1234.5,c.getTarjeta().getSaldo());assertEquals(n+1,c.getTarjeta().obtenerHistorial().size());assertEquals(TipoTransaccion.RECARGA,c.getTarjeta().obtenerUltimaTransaccion().tipo());
        assertThrows(IllegalArgumentException.class,()->admin.recargar(c.getId(),BigDecimal.valueOf(-1)));assertEquals(antes+1234.5,c.getTarjeta().getSaldo());
        assertTrue(admin.detalleCliente(c.getId()).contains("MOVIMIENTOS"));
    }
    @Test void limitesDePromocionRechazanSinMutar() {
        int n=admin.listar("Promociones","").size();assertThrows(IllegalArgumentException.class,()->admin.guardar("Promociones",null,Map.of("Nombre","X","Tipo","PORCENTAJE","Valor","101","Desde","2026-01-01","Hasta","2027-01-01"),true));assertEquals(n,admin.listar("Promociones","").size());
    }
}
