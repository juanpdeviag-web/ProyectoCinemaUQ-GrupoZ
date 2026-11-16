package test.services;
import co.edu.uq.cinemauq.core.domain.*;
import co.edu.uq.cinemauq.core.managers.ReservasManager;
import co.edu.uq.cinemauq.services.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AdministracionCatalogoTest {
    CatalogoCinema catalogo=new CatalogoCinema();
    AdministracionService admin=new AdministracionService(new Administrador("A","Admin","a@c.co"),catalogo,new ReservasManager());
    @Test void clienteYAnonimoBloqueadosEnServicio() {
        assertThrows(SecurityException.class,()->new AdministracionService(new Cliente("C","Cliente","c@c.co")));
        assertThrows(SecurityException.class,()->new AdministracionService(null));
    }
    @Test void peliculaCreadaEditadaBuscadaYDesactivadaAfectaCatalogoCliente() {
        var p=admin.guardar("Películas",null,Map.of("Título","Nueva","Género","Drama","Duración","90"),true);
        assertTrue(catalogo.peliculasActivas().stream().anyMatch(x->x.getTitulo().equals("Nueva")));
        admin.guardar("Películas",p.id(),Map.of("Título","Editada","Género","Comedia","Duración","95"),true);
        assertEquals(1,admin.listar("Películas","Editada").size());admin.activar(p.id(),false);
        assertFalse(catalogo.peliculasActivas().stream().anyMatch(x->x.getTitulo().equals("Editada")));
    }
    @Test void salaYPlanoSeCreanYEditanSinCambiarId() {
        var s=admin.guardar("Salas",null,Map.of("Nombre","Nueva","Filas","2","Columnas","3"),true);
        assertEquals(0,admin.listar("Asientos","").stream().filter(x->x.valor("Sala").equals(s.id())).count());
        var asiento=admin.guardar("Asientos",null,Map.of("Sala",s.id(),"Código","A1","Accesible","true"),true);
        assertEquals(5,admin.completarAsientos(s.id()));
        assertEquals(6,admin.listar("Asientos","").stream().filter(x->x.valor("Sala").equals(s.id())).count());
        admin.guardar("Salas",s.id(),Map.of("Nombre","Renombrada","Filas","3","Columnas","4"),true);
        assertEquals(6,admin.completarAsientos(s.id()));
        assertEquals(12,admin.listar("Asientos","").stream().filter(x->x.valor("Sala").equals(s.id())).count());
        assertEquals("true",catalogo.ficha(asiento.id()).valor("Accesible"));
        admin.activar(s.id(),false);assertFalse(catalogo.ficha(s.id()).activa());
    }
    @Test void asientoAccesibleDeshabilitadoYDuplicado() {
        var a=admin.listar("Asientos","").getFirst();var d=new HashMap<>(a.datos());d.put("Accesible","true");
        admin.guardar("Asientos",a.id(),d,false);assertFalse(catalogo.ficha(a.id()).activa());assertEquals("true",catalogo.ficha(a.id()).valor("Accesible"));
        assertThrows(IllegalArgumentException.class,()->admin.guardar("Asientos",null,d,true));
    }
    @Test void datosInvalidosNoMutanCatalogo() {
        int n=admin.listar("Películas","").size();assertThrows(IllegalArgumentException.class,()->admin.guardar("Películas",null,Map.of("Título","X","Género","D","Duración","0"),true));assertEquals(n,admin.listar("Películas","").size());
    }
}
