package test.services;
import co.edu.uq.cinemauq.core.domain.*;
import co.edu.uq.cinemauq.core.managers.*;
import co.edu.uq.cinemauq.services.*;
import co.edu.uq.cinemauq.ui.models.SesionCompra;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AdministracionFuncionesTest {
    CatalogoCinema catalogo=new CatalogoCinema();ReservasManager registro=new ReservasManager();
    AdministracionService admin=new AdministracionService(new Administrador("A","Admin","a@c.co"),catalogo,registro);
    PagoCompraService pagos=new PagoCompraService(new PuntosService(),registro,catalogo);
    SesionCompra sesion(String id,String asiento) {var f=catalogo.funcion(id);var s=new SesionCompra(catalogo);s.setPeliculaSeleccionada(f.getPelicula());s.setFuncionSeleccionada(f);s.setPrecioEntrada(catalogo.precio(f));s.agregarAsiento(asiento);return s;}
    Cliente cliente(String id){Cliente c=new Cliente(id,"Cliente "+id,"x@c.co");c.getTarjeta().recargar(200000);return c;}
    @Test void conflictoRechazaSinModificarFuncion() {
        var f=admin.listar("Funciones","").getFirst();var d=new HashMap<>(f.datos());
        assertThrows(IllegalArgumentException.class,()->admin.guardar("Funciones",null,d,true));
        d.put("Fecha y hora",LocalDateTime.now().plusDays(60).toString());var nueva=admin.guardar("Funciones",null,d,true);
        var editada=new HashMap<>(nueva.datos());editada.put("Fecha y hora",LocalDateTime.now().plusDays(61).toString());
        assertEquals(nueva.id(),admin.guardar("Funciones",nueva.id(),editada,true).id());
    }
    @Test void cancelarFuncionReembolsaTodasLasComprasUnaVezYBloqueaVenta() {
        var f=admin.listar("Funciones","").getFirst();Cliente a=cliente("C1"),b=cliente("C2");
        var pagoA=pagos.pagar(a,sesion(f.id(),"A1"));var pagoB=pagos.pagar(b,sesion(f.id(),"A2"));
        TarjetaVirtual original=a.getTarjeta();a.setTarjeta(new TarjetaVirtual());
        assertEquals(2,admin.cancelarFuncion(f.id(),"Mantenimiento"));
        assertEquals(200000,original.getSaldo());assertEquals(200000,b.getTarjeta().getSaldo());
        assertEquals(1,original.obtenerHistorial().stream().filter(t->t.tipo()==TipoTransaccion.REEMBOLSO).count());
        assertEquals(1,registro.buscarCompra(pagoA.idCompra()).reembolso().porcentajeAplicado());
        assertEquals(1,registro.buscarCompra(pagoB.idCompra()).reembolso().porcentajeAplicado());
        assertFalse(AsientosManager.getInstance().isAsientoOcupado(f.id(),"A1"));
        assertThrows(IllegalArgumentException.class,()->admin.cancelarFuncion(f.id(),"Otra vez"));
        assertThrows(RuntimeException.class,()->pagos.pagar(cliente("C3"),sesion(f.id(),"A3")));
        assertThrows(IllegalArgumentException.class,()->admin.guardar("Funciones",f.id(),f.datos(),true));
    }
    @Test void falloEnUnaTarjetaRevierteTodoElLote() {
        var f=admin.listar("Funciones","").getFirst();Cliente a=cliente("C1"),b=cliente("C2");
        b.setTarjeta(new TarjetaVirtual(){@Override public void registrarTransaccion(Transaccion t){super.registrarTransaccion(t);if(t.tipo()==TipoTransaccion.REEMBOLSO)throw new IllegalStateException("Fallo simulado");}});b.getTarjeta().recargar(200000);
        var pa=pagos.pagar(a,sesion(f.id(),"A1"));var pb=pagos.pagar(b,sesion(f.id(),"A2"));
        assertThrows(IllegalStateException.class,()->admin.cancelarFuncion(f.id(),"Fallo"));
        assertEquals(140000,a.getTarjeta().getSaldo());assertEquals(140000,b.getTarjeta().getSaldo());
        assertNull(registro.buscarCompra(pa.idCompra()).reembolso());assertNull(registro.buscarCompra(pb.idCompra()).reembolso());assertTrue(catalogo.ficha(f.id()).activa());
        assertTrue(AsientosManager.getInstance().isAsientoOcupado(f.id(),"A1"));
    }
    @Test void funcionVendidaNoSeReprogramaNiPermiteDobleAsiento() {
        var f=admin.listar("Funciones","").getFirst();pagos.pagar(cliente("A"),sesion(f.id(),"A1"));
        assertThrows(IllegalArgumentException.class,()->admin.guardar("Funciones",f.id(),f.datos(),true));
        Cliente b=cliente("B");assertThrows(RuntimeException.class,()->pagos.pagar(b,sesion(f.id(),"A1")));assertEquals(200000,b.getTarjeta().getSaldo());
    }
    @Test void asientoDeshabilitadoImpideCobro() {
        var f=admin.listar("Funciones","").getFirst();var a=admin.listar("Asientos","").stream().filter(x->x.valor("Sala").equals(f.valor("Sala"))&&x.valor("Código").equals("A1")).findFirst().orElseThrow();admin.activar(a.id(),false);
        Cliente c=cliente("C");assertThrows(RuntimeException.class,()->pagos.pagar(c,sesion(f.id(),"A1")));assertEquals(200000,c.getTarjeta().getSaldo());
    }
    @Test void productoYComboEditadosSeReflejanEnVenta() {
        var p=admin.guardar("Productos",null,Map.of("Nombre","Prueba","Descripción","Fresco","Precio","1234"),true);
        var combo=admin.guardar("Combos",null,Map.of("Nombre","Doble","Descripción","Dos unidades","Precio","2000","Productos y cantidades",p.id()+":2"),true);
        assertEquals(2000,catalogo.confiteria().stream().filter(x->x.getNombre().equals("Doble")).findFirst().orElseThrow().getPrecio());
        var d=new HashMap<>(p.datos());d.put("Precio","1500");admin.guardar("Productos",p.id(),d,true);
        assertEquals(1500,catalogo.confiteria().stream().filter(x->x.getNombre().equals("Prueba")).findFirst().orElseThrow().getPrecio());
        admin.activar(p.id(),false);assertFalse(catalogo.confiteria().stream().anyMatch(x->x.getNombre().equals("Doble")));assertTrue(catalogo.ficha(combo.id()).activa());
    }
}
