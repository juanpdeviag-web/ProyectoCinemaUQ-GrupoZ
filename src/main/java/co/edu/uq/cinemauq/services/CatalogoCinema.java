package co.edu.uq.cinemauq.services;

import co.edu.uq.cinemauq.core.domain.*;
import co.edu.uq.cinemauq.core.seed.DataSeeder;
import co.edu.uq.cinemauq.patterns.composite.*;
import java.math.*;
import java.time.*;
import java.util.*;

/** Catálogo único en memoria. Las fichas no contienen historial financiero. */
public final class CatalogoCinema {
    public record Ficha(String id, String tipo, Map<String,String> datos, boolean activa) {
        public Ficha { datos = Collections.unmodifiableMap(new LinkedHashMap<>(datos)); }
        public String valor(String campo) { return datos.getOrDefault(campo, ""); }
        @Override public String toString() { return datos.values().stream().findFirst().orElse(id); }
    }
    private static final CatalogoCinema INSTANCE = new CatalogoCinema();
    public static CatalogoCinema getInstance() { return INSTANCE; }
    private final Map<String,Ficha> fichas = new LinkedHashMap<>();
    private final Map<String,Pelicula> peliculas = new LinkedHashMap<>();
    private final Map<String,Sala> salas = new LinkedHashMap<>();
    private final Map<String,Funcion> funciones = new LinkedHashMap<>();
    public CatalogoCinema() {
        for (Pelicula p : DataSeeder.cargarPeliculas()) {
            String id = UUID.randomUUID().toString(); peliculas.put(id,p);
            poner(id,"Películas",Map.of("Título",p.getTitulo(),"Género",p.getGenero(),"Duración", ""+p.getDuracionMinutos()),true);
        }
        for (Sala s : DataSeeder.cargarSalas()) {
            String id = UUID.randomUUID().toString(); salas.put(id,s);
            poner(id,"Salas",Map.of("Nombre",s.getNumero(),"Filas",""+s.getCapacidadFilas(),"Columnas",""+s.getCapacidadColumnas()),true);
            crearPlano(id,s);
        }
        int i=0;
        for (String pid : peliculas.keySet()) {
            for(int j=0;j<3;j++) {
                String sid = new ArrayList<>(salas.keySet()).get(i++%salas.size());
                LocalDateTime inicio = LocalDate.now().plusDays(1+j).atTime(14,0);
                while (conflicto(null,peliculas.get(pid),salas.get(sid),inicio)) inicio=inicio.plusHours(4);
                Funcion f=new Funcion(peliculas.get(pid),salas.get(sid),inicio);
                funciones.put(f.getId(),f);
                poner(f.getId(),"Funciones",Map.of("Película",pid,"Sala",sid,"Fecha y hora",inicio.toString(),"Precio","60000"),true);
            }
        }
        String[] nombres={"Palomitas","Refresco","Nachos","Hot Dog","Agua","Chocolates"};
        int[] precios={32000,20000,40000,48000,12000,24000};
        List<String> ids=new ArrayList<>();
        for(int n=0;n<nombres.length;n++) { String id=UUID.randomUUID().toString(); ids.add(id); poner(id,"Productos",Map.of("Nombre",nombres[n],"Descripción","Producto individual","Precio",""+precios[n]),true); }
        poner(UUID.randomUUID().toString(),"Combos",Map.of("Nombre","Combo Cine","Descripción","Palomitas y refresco","Precio","52000","Productos y cantidades",ids.get(0)+":1,"+ids.get(1)+":1"),true);
        poner(UUID.randomUUID().toString(),"Combos",Map.of("Nombre","Combo Familiar","Descripción","Dos palomitas, refresco y nachos","Precio","124000","Productos y cantidades",ids.get(0)+":2,"+ids.get(1)+":1,"+ids.get(2)+":1"),true);
        poner(UUID.randomUUID().toString(),"Combos",Map.of("Nombre","Combo Deluxe","Descripción","Hot Dog, refresco y chocolates","Precio","92000","Productos y cantidades",ids.get(3)+":1,"+ids.get(1)+":1,"+ids.get(5)+":1"),true);
        poner("FIJO","Promociones",Map.of("Nombre","Descuento fijo","Tipo","FIJO","Valor","5000","Desde","2000-01-01","Hasta","2099-12-31"),true);
        poner("ESTUDIANTE","Promociones",Map.of("Nombre","Estudiante","Tipo","PORCENTAJE","Valor","10","Desde","2000-01-01","Hasta","2099-12-31"),true);
    }
    public static List<String> campos(String tipo) {
        return switch(tipo) {
            case "Películas" -> List.of("Título","Género","Duración");
            case "Salas" -> List.of("Nombre","Filas","Columnas");
            case "Asientos" -> List.of("Sala","Código","Accesible");
            case "Funciones" -> List.of("Película","Sala","Fecha y hora","Precio");
            case "Productos" -> List.of("Nombre","Descripción","Precio");
            case "Combos" -> List.of("Nombre","Descripción","Precio","Productos y cantidades");
            case "Promociones" -> List.of("Nombre","Tipo","Valor","Desde","Hasta");
            default -> List.of();
        };
    }
    private void poner(String id,String tipo,Map<String,String> d,boolean activa) {
        Map<String,String> orden=new LinkedHashMap<>(); campos(tipo).forEach(c->orden.put(c,d.getOrDefault(c,"")));
        fichas.put(id,new Ficha(id,tipo,orden,activa));
    }
    private void crearPlano(String sid,Sala s) {
        for(int f=0;f<s.getCapacidadFilas();f++) for(int c=1;c<=s.getCapacidadColumnas();c++)
            poner(UUID.randomUUID().toString(),"Asientos",Map.of("Sala",sid,"Código",""+(char)('A'+f)+c,"Accesible","false"),true);
    }
    public synchronized List<Ficha> listar(String tipo) { return fichas.values().stream().filter(f->f.tipo().equals(tipo)).toList(); }
    public synchronized Ficha ficha(String id) { Ficha f=fichas.get(id); if(f==null) throw new IllegalArgumentException("Registro inexistente"); return f; }
    public synchronized List<Pelicula> peliculasActivas() { return peliculas.entrySet().stream().filter(e->ficha(e.getKey()).activa()).map(Map.Entry::getValue).toList(); }
    public synchronized List<Funcion> funcionesDisponibles() { return funciones.values().stream().filter(this::vendible).toList(); }
    public synchronized boolean vendible(Funcion f) {
        if(!funciones.containsKey(f.getId())) return true; // Funciones externas de pruebas/servicios heredados.
        Ficha d=ficha(f.getId());
        return d.activa() && ficha(d.valor("Película")).activa() && ficha(d.valor("Sala")).activa() && f.getFechaHora().isAfter(LocalDateTime.now());
    }
    public synchronized boolean gestionada(Funcion f) { return funciones.containsKey(f.getId()); }
    public synchronized boolean asientoHabilitado(Funcion f,String codigo) {
        if(!gestionada(f)) return true;
        String sala=ficha(f.getId()).valor("Sala");
        return listar("Asientos").stream().anyMatch(a->a.valor("Sala").equals(sala)&&a.valor("Código").equals(codigo)&&a.activa());
    }
    public synchronized boolean accesible(Funcion f,String codigo) {
        if(!gestionada(f)) return false;
        String sala=ficha(f.getId()).valor("Sala");
        return listar("Asientos").stream().anyMatch(a->a.valor("Sala").equals(sala)&&a.valor("Código").equals(codigo)&&Boolean.parseBoolean(a.valor("Accesible")));
    }
    public synchronized double precio(Funcion f) { return gestionada(f)?Double.parseDouble(ficha(f.getId()).valor("Precio")):60000; }
    public synchronized Funcion funcion(String id) { return Objects.requireNonNull(funciones.get(id),"Función inexistente"); }
    public synchronized List<ItemCompra> confiteria() {
        List<ItemCompra> r=new ArrayList<>();
        for(Ficha f:listar("Productos")) if(f.activa()) r.add(new Producto(f.valor("Nombre"),Double.parseDouble(f.valor("Precio"))));
        for(Ficha f:listar("Combos")) if(f.activa()&&componentes(f).keySet().stream().allMatch(id->ficha(id).activa())) {
            Combo combo=new Combo(f.valor("Nombre"),Double.parseDouble(f.valor("Precio")));
            componentes(f).forEach((id,cantidad)->{ Ficha p=ficha(id); for(int i=0;i<cantidad;i++) combo.agregar(new Producto(p.valor("Nombre"),Double.parseDouble(p.valor("Precio")))); });
            r.add(combo);
        }
        return List.copyOf(r);
    }
    private Map<String,Integer> componentes(Ficha f) {
        Map<String,Integer> r=new LinkedHashMap<>();
        for(String parte:f.valor("Productos y cantidades").split(",")) {
            String[] v=parte.trim().split(":"); if(v.length!=2) throw new IllegalArgumentException("Selecciona productos y cantidades");
            Ficha p=ficha(v[0]); if(!p.tipo().equals("Productos")) throw new IllegalArgumentException("Producto inválido");
            int n=Integer.parseInt(v[1]); if(n<1||n>100) throw new IllegalArgumentException("Cantidad entre 1 y 100"); r.merge(v[0],n,Integer::sum);
        } return r;
    }
    private boolean conflicto(String id,Pelicula p,Sala s,LocalDateTime inicio) {
        return funciones.values().stream().filter(f->!f.getId().equals(id)&&ficha(f.getId()).activa()&&f.getSala()==s)
            .anyMatch(f->inicio.isBefore(f.getFechaHora().plusMinutes(f.getPelicula().getDuracionMinutos()))&&f.getFechaHora().isBefore(inicio.plusMinutes(p.getDuracionMinutos())));
    }
    synchronized Ficha guardar(String tipo,String id,Map<String,String> valores,boolean activa) {
        Map<String,String> d=new LinkedHashMap<>();
        for(String c:campos(tipo)) { String v=valores.getOrDefault(c,"").trim(); if(v.isEmpty()) throw new IllegalArgumentException("Completa: "+c); d.put(c,v); }
        if(d.isEmpty()) throw new IllegalArgumentException("Área no editable");
        boolean nuevo=id==null; if(nuevo) id=UUID.randomUUID().toString(); else if(!ficha(id).tipo().equals(tipo)) throw new IllegalArgumentException("Tipo incorrecto");
        Ficha propuesta=new Ficha(id,tipo,d,activa);
        String nombreCampo=tipo.equals("Películas")?"Título":"Nombre";
        if(d.containsKey(nombreCampo)) {
            final String registroId=id;
            if(fichas.values().stream().anyMatch(f->!f.id().equals(registroId)
                    && (f.tipo().equals(tipo)||((tipo.equals("Productos")||tipo.equals("Combos"))&&(f.tipo().equals("Productos")||f.tipo().equals("Combos"))))
                    &&f.valor(nombreCampo).equalsIgnoreCase(d.get(nombreCampo))))throw new IllegalArgumentException("Ya existe ese nombre en el catálogo");
        }
        switch(tipo) {
            case "Películas" -> { int dur=entero(d,"Duración",1,600); Pelicula p=peliculas.get(id);
                if(p==null) peliculas.put(id,new Pelicula(d.get("Título"),d.get("Género"),dur));
                else { validarDuracion(p,dur);
                    p.setTitulo(d.get("Título")); p.setGenero(d.get("Género")); p.setDuracionMinutos(dur); }
            }
            case "Salas" -> { int filas=entero(d,"Filas",1,26),columnas=entero(d,"Columnas",1,50); Sala s=salas.get(id);
                if(s==null) { s=new Sala(d.get("Nombre"),filas,columnas); salas.put(id,s); }
                else { boolean cambioPlano=filas!=s.getCapacidadFilas()||columnas!=s.getCapacidadColumnas();
                    s.configurar(d.get("Nombre"),filas,columnas);
                    if(cambioPlano) actualizarPlano(id,s); }
            }
            case "Asientos" -> { Sala s=salas.get(d.get("Sala")); if(s==null) throw new IllegalArgumentException("Sala inválida");
                String codigo=d.get("Código").toUpperCase(Locale.ROOT); d.put("Código",codigo);
                if(!codigo.matches("[A-Z][1-9][0-9]*")||codigo.charAt(0)-'A'>=s.getCapacidadFilas()||Integer.parseInt(codigo.substring(1))>s.getCapacidadColumnas()) throw new IllegalArgumentException("Asiento fuera de la distribución de la sala");
                final String aid=id; if(listar(tipo).stream().anyMatch(a->!a.id().equals(aid)&&a.valor("Sala").equals(d.get("Sala"))&&a.valor("Código").equals(codigo))) throw new IllegalArgumentException("El asiento ya existe; selecciónalo para editarlo");
                if(!Set.of("true","false").contains(d.get("Accesible"))) throw new IllegalArgumentException("Accesible debe ser true o false");
            }
            case "Funciones" -> { Pelicula p=peliculas.get(d.get("Película")); Sala s=salas.get(d.get("Sala"));
                if(p==null||s==null||!ficha(d.get("Película")).activa()||!ficha(d.get("Sala")).activa()) throw new IllegalArgumentException("Selecciona película y sala activas");
                if(listar("Asientos").stream().noneMatch(a->a.valor("Sala").equals(d.get("Sala"))&&a.activa()))throw new IllegalArgumentException("Configura al menos un asiento habilitado en la sala");
                LocalDateTime fecha=LocalDateTime.parse(d.get("Fecha y hora")); if(!fecha.isAfter(LocalDateTime.now())) throw new IllegalArgumentException("Solo se programan funciones futuras");
                dinero(d,"Precio"); if(conflicto(id,p,s,fecha)) throw new IllegalArgumentException("Superposición en la misma sala");
                Funcion f=funciones.get(id); if(f==null) { f=new Funcion(p,s,fecha); id=f.getId(); funciones.put(id,f); } else f.reprogramar(p,s,fecha);
            }
            case "Productos" -> dinero(d,"Precio");
            case "Combos" -> { dinero(d,"Precio"); componentes(propuesta); }
            case "Promociones" -> { BigDecimal valor=dinero(d,"Valor"); if(!Set.of("FIJO","PORCENTAJE").contains(d.get("Tipo"))) throw new IllegalArgumentException("Tipo: FIJO o PORCENTAJE");
                if(d.get("Tipo").equals("PORCENTAJE")&&valor.compareTo(BigDecimal.valueOf(100))>0) throw new IllegalArgumentException("Porcentaje máximo 100");
                if(LocalDate.parse(d.get("Desde")).isAfter(LocalDate.parse(d.get("Hasta")))) throw new IllegalArgumentException("Vigencia inválida"); }
            default -> throw new IllegalArgumentException("Área inválida");
        }
        poner(id,tipo,d,activa); return ficha(id);
    }
    private void validarDuracion(Pelicula pelicula,int minutos) {
        var activas=funciones.values().stream().filter(f->ficha(f.getId()).activa()).toList();
        for(int i=0;i<activas.size();i++)for(int j=i+1;j<activas.size();j++) {
            Funcion a=activas.get(i),b=activas.get(j);
            if(a.getSala()!=b.getSala()||(a.getPelicula()!=pelicula&&b.getPelicula()!=pelicula))continue;
            int da=a.getPelicula()==pelicula?minutos:a.getPelicula().getDuracionMinutos();
            int db=b.getPelicula()==pelicula?minutos:b.getPelicula().getDuracionMinutos();
            if(a.getFechaHora().isBefore(b.getFechaHora().plusMinutes(db))&&b.getFechaHora().isBefore(a.getFechaHora().plusMinutes(da)))throw new IllegalArgumentException("La duración genera superposición de funciones");
        }
    }
    private void actualizarPlano(String id,Sala sala) {
        fichas.values().removeIf(f->f.tipo().equals("Asientos")&&f.valor("Sala").equals(id)
                &&(f.valor("Código").charAt(0)-'A'>=sala.getCapacidadFilas()||Integer.parseInt(f.valor("Código").substring(1))>sala.getCapacidadColumnas()));
    }
    synchronized void activar(String id,boolean activa) { Ficha f=ficha(id); poner(id,f.tipo(),f.datos(),activa); }
    private int entero(Map<String,String>d,String c,int min,int max) { int n=Integer.parseInt(d.get(c)); if(n<min||n>max) throw new IllegalArgumentException(c+": "+min+"–"+max); return n; }
    private BigDecimal dinero(Map<String,String>d,String c) { BigDecimal n=new BigDecimal(d.get(c)); if(n.signum()<=0||n.scale()>2||!Double.isFinite(n.doubleValue())) throw new IllegalArgumentException(c+" debe ser positivo, máximo 2 decimales"); return n; }
    public synchronized BigDecimal aplicarPromociones(BigDecimal base,Collection<String> seleccionadas) {
        BigDecimal total=base;
        for(String id:seleccionadas) { Ficha p=fichas.get(id); if(p==null||!p.tipo().equals("Promociones")||!p.activa()) continue;
            LocalDate hoy=LocalDate.now(); if(hoy.isBefore(LocalDate.parse(p.valor("Desde")))||hoy.isAfter(LocalDate.parse(p.valor("Hasta")))) continue;
            BigDecimal valor=new BigDecimal(p.valor("Valor")); total=total.subtract(p.valor("Tipo").equals("FIJO")?valor:total.multiply(valor).divide(BigDecimal.valueOf(100))).max(BigDecimal.ZERO);
        } return total.setScale(2,RoundingMode.HALF_UP);
    }
    public synchronized List<Ficha> promocionesVigentes() {
        LocalDate hoy=LocalDate.now();
        return listar("Promociones").stream().filter(p->p.activa()&&!hoy.isBefore(LocalDate.parse(p.valor("Desde")))&&!hoy.isAfter(LocalDate.parse(p.valor("Hasta")))).toList();
    }
}
