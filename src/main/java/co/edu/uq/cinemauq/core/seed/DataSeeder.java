package co.edu.uq.cinemauq.core.seed;

import co.edu.uq.cinemauq.core.domain.*;
import co.edu.uq.cinemauq.patterns.composite.*;
import java.time.LocalDateTime;
import java.util.*;

/** Datos demostrativos centralizados para películas, salas, funciones y confitería. */
public final class DataSeeder {
    private static volatile DatosPrueba datosActuales;
    private DataSeeder() {}
    public static List<Pelicula> cargarPeliculas() {
        // Se conserva el catálogo histórico de cinco películas para compatibilidad con pruebas existentes.
        return List.copyOf(cargarPeliculasCompletas().subList(0,5));
    }
    public static List<Pelicula> cargarPeliculasCompletas() {
        return List.of(new Pelicula("Interestelar", "Ciencia ficción", 169), new Pelicula("El origen", "Ciencia ficción", 148),
                new Pelicula("Toy Story", "Animación", 81), new Pelicula("El padrino", "Drama", 175),
                new Pelicula("Avengers: Endgame", "Acción", 181), new Pelicula("Coco", "Animación", 105),
                new Pelicula("Dune: Parte Dos", "Ciencia ficción", 166));
    }
    public static List<Sala> cargarSalas() {
        return List.of(new Sala("Sala 1",10,12),new Sala("Sala 2",8,10),new Sala("Sala 3",12,15),new Sala("Sala VIP",6,8));
    }
    public static List<Funcion> cargarFunciones() {
        List<Funcion> result=new ArrayList<>(); List<Pelicula> peliculas=cargarPeliculasCompletas(); List<Sala> salas=cargarSalas();
        for(int d=1;d<=7;d++) for(int s=0;s<3;s++) {
            LocalDateTime fecha=LocalDateTime.now().plusDays(d).withHour(s==0?14:s==1?17:20).withMinute(0).withSecond(0).withNano(0);
            result.add(new Funcion(peliculas.get((d+s)%peliculas.size()),salas.get(s),fecha));
        }
        return List.copyOf(result);
    }
    public static List<ItemCompra> cargarProductosYCombos() {
        Producto palomitas=new Producto("Palomitas grandes",18000), gaseosa=new Producto("Gaseosa",9000), nachos=new Producto("Nachos con queso",16000);
        Producto perro=new Producto("Perro caliente",14000), agua=new Producto("Agua",5000), chocolate=new Producto("Chocolate",7000);
        Producto gomitas=new Producto("Gomitas",6500);
        Combo pareja=new Combo("Combo pareja").agregar(palomitas).agregar(gaseosa).agregar(gaseosa);
        Combo familiar=new Combo("Combo familiar").agregar(palomitas).agregar(palomitas).agregar(gaseosa).agregar(gaseosa);
        return List.of(palomitas,gaseosa,nachos,perro,agua,chocolate,gomitas,pareja,familiar);
    }
    public static List<PromocionSemilla> cargarPromociones() {
        return List.of(new PromocionSemilla("Martes de cine",20),new PromocionSemilla("Combo pareja",15),
                new PromocionSemilla("Estudiante",10),new PromocionSemilla("Madrugadores",12));
    }
    public static DatosSemilla cargarDatos() { return new DatosSemilla(cargarPeliculas(),cargarSalas()); }
    public static synchronized DatosPrueba cargarDatosPrueba() {
        datosActuales = new DatosPrueba(cargarPeliculasCompletas(), cargarSalas(), cargarFunciones(), cargarProductosYCombos(), cargarPromociones());
        return datosActuales;
    }
    public static DatosPrueba obtenerDatosActuales() { return datosActuales; }
    public record PromocionSemilla(String nombre,double descuento) {}
    public record DatosPrueba(List<Pelicula> peliculas,List<Sala> salas,List<Funcion> funciones,List<ItemCompra> productos,List<PromocionSemilla> promociones) {
        public DatosPrueba { peliculas=List.copyOf(peliculas); salas=List.copyOf(salas); funciones=List.copyOf(funciones); productos=List.copyOf(productos); promociones=List.copyOf(promociones); }
    }
    public record DatosSemilla(List<Pelicula> peliculas,List<Sala> salas) {
        public DatosSemilla { peliculas=List.copyOf(peliculas); salas=List.copyOf(salas); }
    }
}
