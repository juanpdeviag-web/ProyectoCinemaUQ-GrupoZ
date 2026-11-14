package co.edu.uq.cinemauq.services;

import co.edu.uq.cinemauq.core.domain.*;
import co.edu.uq.cinemauq.core.managers.*;
import co.edu.uq.cinemauq.core.seed.UserSeeder;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.function.Supplier;
import java.nio.file.*;
import java.io.IOException;
import co.edu.uq.cinemauq.patterns.strategy.*;

/** Fachada administrativa: autorización en cada operación, nunca en la apariencia de un botón. */
public final class AdministracionService {
    private final Usuario usuario;
    private final CatalogoCinema catalogo;
    private final ReservasManager registro;
    public AdministracionService(Usuario usuario) { this(usuario,CatalogoCinema.getInstance(),ReservasManager.getInstance()); }
    public AdministracionService(Usuario usuario,CatalogoCinema catalogo,ReservasManager registro) {
        autorizar(usuario); this.usuario=usuario; this.catalogo=catalogo; this.registro=registro;
    }
    public static void autorizar(Usuario usuario) { if(!(usuario instanceof Administrador)) throw new SecurityException("Acceso denegado: se requiere el rol ADMINISTRADOR"); }
    private void permiso() { autorizar(usuario); }
    public List<CatalogoCinema.Ficha> listar(String tipo,String filtro) { permiso(); String q=filtro.toLowerCase(Locale.ROOT); return catalogo.listar(tipo).stream().filter(f->f.datos().values().stream().anyMatch(v->v.toLowerCase(Locale.ROOT).contains(q))||f.id().contains(q)).toList(); }
    public CatalogoCinema.Ficha guardar(String tipo,String id,Map<String,String> datos,boolean activa) {
        permiso(); synchronized(catalogo) {
            if(id!=null&&tipo.equals("Funciones")&&!catalogo.funcion(id).getFechaHora().isAfter(LocalDateTime.now()))throw new IllegalArgumentException("Solo se editan funciones futuras");
            if(id!=null&&tipo.equals("Funciones")&&registro.listarCompras().stream().anyMatch(c->c.compra().funcion().getId().equals(id))) throw new IllegalArgumentException("Función con ventas: no se puede reprogramar");
            if(tipo.equals("Funciones")&&(!activa||(id!=null&&!catalogo.ficha(id).activa()))) throw new IllegalArgumentException("Las funciones canceladas no se reactivan; utiliza Cancelar función");
            if(id!=null&&(tipo.equals("Películas")||tipo.equals("Salas"))) {
                String campo=tipo.equals("Películas")?"Película":"Sala";
                Set<String> vinculadas=new HashSet<>();for(var f:catalogo.listar("Funciones"))if(f.valor(campo).equals(id))vinculadas.add(f.id());
                if(registro.listarCompras().stream().anyMatch(c->vinculadas.contains(c.compra().funcion().getId())))throw new IllegalArgumentException("Registro vinculado a compras confirmadas; conserva sus datos históricos");
            }
            if(id!=null&&tipo.equals("Asientos")) {
                var anterior=catalogo.ficha(id);
                if(!Objects.equals(anterior.valor("Código"),datos.get("Código"))||!Objects.equals(anterior.valor("Sala"),datos.get("Sala"))) {
                    var funciones=catalogo.listar("Funciones").stream().filter(f->f.valor("Sala").equals(anterior.valor("Sala"))).map(CatalogoCinema.Ficha::id).toList();
                    if(registro.listarCompras().stream().anyMatch(c->funciones.contains(c.compra().funcion().getId())&&c.compra().asientos().contains(anterior.valor("Código"))))throw new IllegalArgumentException("El asiento forma parte de una compra confirmada");
                }
            }
            return catalogo.guardar(tipo,id,datos,activa);
        }
    }
    /** Completa solo posiciones inexistentes; conserva IDs, accesibilidad y asientos deshabilitados. */
    public int completarAsientos(String salaId) {
        permiso(); synchronized(catalogo) {
            var sala=catalogo.ficha(salaId);if(!sala.tipo().equals("Salas"))throw new IllegalArgumentException("Selecciona una sala");
            Set<String> existentes=new HashSet<>();catalogo.listar("Asientos").stream().filter(a->a.valor("Sala").equals(salaId)).forEach(a->existentes.add(a.valor("Código")));
            int creados=0;
            for(int f=0;f<Integer.parseInt(sala.valor("Filas"));f++)for(int c=1;c<=Integer.parseInt(sala.valor("Columnas"));c++) {
                String codigo=""+(char)('A'+f)+c;
                if(!existentes.contains(codigo)){catalogo.guardar("Asientos",null,Map.of("Sala",salaId,"Código",codigo,"Accesible","false"),true);creados++;}
            } return creados;
        }
    }
    public void activar(String id,boolean activa) {
        permiso(); synchronized(catalogo) {
            if(catalogo.ficha(id).tipo().equals("Funciones")) throw new IllegalArgumentException("Utiliza Cancelar función para procesar sus reembolsos");
            catalogo.activar(id,activa);
        }
    }
    public List<Cliente> clientes(String filtro) { permiso(); String q=filtro.toLowerCase(Locale.ROOT); return UserSeeder.obtenerClientes().stream().filter(c->(c.getNombre()+" "+c.getCorreo()+" "+c.getId()).toLowerCase(Locale.ROOT).contains(q)).toList(); }
    public List<CompraConfirmada> compras() { permiso(); return registro.listarCompras(); }
    public void recargar(String clienteId,BigDecimal monto) {
        permiso(); if(monto==null||monto.signum()<=0||monto.scale()>2||!Double.isFinite(monto.doubleValue())) throw new IllegalArgumentException("Recarga positiva, máximo dos decimales");
        Cliente c=clientes("").stream().filter(x->x.getId().equals(clienteId)).findFirst().orElseThrow(()->new IllegalArgumentException("Cliente inexistente"));
        c.getTarjeta().ejecutarTransaccion(()->{
            if(!Double.isFinite(c.getTarjeta().getSaldo())||!Double.isFinite(c.getTarjeta().getSaldo()+monto.doubleValue()))throw new IllegalArgumentException("La recarga excede el saldo admitido");
            c.getTarjeta().recargar(monto.doubleValue());return null;
        });
    }
    /** Un único cerrojo de catálogo bloquea ventas mientras se cancela. Respalda todas las tarjetas del lote. */
    public int cancelarFuncion(String id,String motivo) {
        permiso(); if(motivo==null||motivo.isBlank()) throw new IllegalArgumentException("Indica el motivo");
        synchronized(catalogo) {
            var f=catalogo.ficha(id); if(!f.tipo().equals("Funciones")||!f.activa()) throw new IllegalArgumentException("La función ya está cancelada");
            int resultado=registro.ejecutarTransaccion(()->{
                var compras=compras().stream().filter(c->c.compra().funcion().getId().equals(id)&&c.reembolso()==null&&!c.compra().estaCancelada()).toList();
                List<TarjetaVirtual> tarjetas=compras.stream().map(CompraConfirmada::tarjetaPago).distinct().sorted(Comparator.comparing(TarjetaVirtual::getNumeroTarjeta)).toList();
                return conTarjetas(tarjetas,0,()->{ var servicio=new CancelacionService(registro,Clock.systemDefaultZone());
                    for(var c:compras) servicio.solicitarCancelacionPorCine(c.compra().id(),motivo);
                    return compras.size(); });
            });
            catalogo.activar(id,false); return resultado;
        }
    }
    private <T>T conTarjetas(List<TarjetaVirtual> tarjetas,int i,Supplier<T> tarea) { return i==tarjetas.size()?tarea.get():tarjetas.get(i).ejecutarTransaccion(()->conTarjetas(tarjetas,i+1,tarea)); }
    private Map<String,String> fila(String... valores) { Map<String,String> r=new LinkedHashMap<>(); for(int i=0;i<valores.length;i+=2)r.put(valores[i],valores[i+1]); return r; }
    private boolean fecha(LocalDateTime f,LocalDate desde,LocalDate hasta) { return !f.toLocalDate().isBefore(desde)&&!f.toLocalDate().isAfter(hasta); }
    public List<Map<String,String>> consultar(String area,LocalDate desde,LocalDate hasta,String busqueda) {
        permiso(); if(desde==null||hasta==null||desde.isAfter(hasta)) throw new IllegalArgumentException("Rango de fechas inválido");
        List<Map<String,String>> filas=new ArrayList<>();
        switch(area) {
            case "Confitería" -> {for(var c:compras())if(fecha(c.compra().fechaCompra(),desde,hasta))for(var item:c.detallePago().items())filas.add(fila("Compra",c.compra().id(),"Fecha",c.compra().fechaCompra().toString(),"Artículo",item.nombre(),"Tipo",item.combo()?"COMBO":"PRODUCTO","Cantidad",""+item.cantidad(),"Precio unitario",item.precioUnitario().toPlainString(),"Subtotal",item.precioUnitario().multiply(BigDecimal.valueOf(item.cantidad())).toPlainString(),"Estado",c.reembolso()!=null?"REEMBOLSADA":c.compra().estaCancelada()?"CANCELADA":"CONFIRMADA"));}
            case "Compras", "Cancelaciones" -> { for(var c:compras()) {
                if(area.equals("Cancelaciones")&&c.reembolso()==null)continue;
                var fecha=area.equals("Cancelaciones")?c.reembolso().fechaReembolso():c.compra().fechaCompra(); if(!fecha(fecha,desde,hasta))continue;
                var r=fila("ID",c.compra().id(),"Cliente",c.cliente().getNombre(),"Película",c.compra().funcion().getPelicula().getTitulo(),"Función",c.compra().funcion().getFechaHora().toString(),"Sala",c.compra().funcion().getSala().getNumero(),"Asientos",String.join(", ",c.compra().asientos()),"Confitería",String.join(", ",c.compra().snacks()),"Pagado",c.importePagado().toPlainString(),"Fecha",fecha.toString(),"Estado",c.reembolso()!=null?"REEMBOLSADA":c.compra().estaCancelada()?"CANCELADA":"CONFIRMADA");
                r.put("Promociones",c.detallePago().promociones()); r.put("Puntos usados",""+c.detallePago().puntos());
                r.put("Descuento puntos",c.detallePago().descuentoPuntos().toPlainString());
                r.put("Entradas",c.detallePago().entradas().toPlainString());r.put("Confitería subtotal",c.detallePago().confiteria().toPlainString());r.put("Descuento promociones",c.detallePago().descuentoPromociones().toPlainString());
                if(c.reembolso()!=null) {r.put("Porcentaje",""+(c.reembolso().porcentajeAplicado()*100));r.put("Reembolso",""+c.reembolso().montoReembolsado());}
                filas.add(r);
            }}
            case "Clientes", "Tarjetas", "Recargas", "Puntos" -> { for(Cliente c:clientes("")) {
                if(area.equals("Clientes")) filas.add(fila("ID",c.getId(),"Nombre",c.getNombre(),"Correo",c.getCorreo(),"Compras",""+compras().stream().filter(x->x.cliente().getId().equals(c.getId())).count(),"Tarjeta",c.getTarjeta().getNumeroTarjeta(),"Saldo",""+c.getTarjeta().getSaldo(),"Puntos disponibles",""+c.getPuntos()));
                if(area.equals("Tarjetas")) filas.add(fila("Cliente ID",c.getId(),"Cliente",c.getNombre(),"Tarjeta",c.getTarjeta().getNumeroTarjeta(),"Saldo",""+c.getTarjeta().getSaldo(),"Estado",c.getTarjeta().isActiva()?"ACTIVA":"INACTIVA"));
                if(area.equals("Recargas")) for(var t:c.getTarjeta().obtenerHistorial()) if(t.tipo()==TipoTransaccion.RECARGA&&fecha(t.fecha(),desde,hasta)) filas.add(fila("Cliente",c.getNombre(),"ID",t.id(),"Fecha",t.fecha().toString(),"Monto",""+t.monto(),"Descripción",t.descripcion()));
                if(area.equals("Puntos")) for(var p:c.getPuntosAcumulados()) if(!p.getFechaObtencion().isBefore(desde)&&!p.getFechaObtencion().isAfter(hasta)) filas.add(fila("Cliente",c.getNombre(),"Originales",""+p.getCantidad(),"Restantes",""+p.getCantidadRestante(),"Obtención",p.getFechaObtencion().toString(),"Vencimiento",p.getFechaVencimiento().toString(),"Estado",p.getEstado().getClass().getSimpleName().replace("Punto","").toUpperCase(Locale.ROOT)));
            }}
            default -> throw new IllegalArgumentException("Consulta inválida");
        }
        String q=busqueda.toLowerCase(Locale.ROOT); return filas.stream().filter(r->r.values().stream().anyMatch(v->v.toLowerCase(Locale.ROOT).contains(q))).toList();
    }
    public String detalleCliente(String id) {
        permiso(); Cliente c=clientes("").stream().filter(x->x.getId().equals(id)).findFirst().orElseThrow();
        StringBuilder b=new StringBuilder(c.getNombre()+" · "+c.getCorreo()+"\nTarjeta: "+c.getTarjeta().getNumeroTarjeta()+" · saldo: "+c.getTarjeta().getSaldo()+"\n\nCOMPRAS / TICKETS\n");
        compras().stream().filter(x->x.cliente().getId().equals(id)).forEach(x->b.append(x.compra().id()).append(" · ").append(x.compra().funcion().getPelicula()).append(" · ").append(x.importePagado()).append(" · ").append(x.reembolso()==null?"CONFIRMADA":"REEMBOLSADA").append('\n'));
        b.append("\nMOVIMIENTOS\n"); c.getTarjeta().obtenerHistorial().forEach(t->b.append(t.fecha()).append(" · ").append(t.tipo()).append(" · ").append(t.monto()).append('\n'));
        b.append("\nLOTES DE PUNTOS\n"); c.getPuntosAcumulados().forEach(p->b.append(p.getCantidad()).append(" / ").append(p.getCantidadRestante()).append(" · ").append(p.getFechaObtencion()).append(" → ").append(p.getFechaVencimiento()).append(" · ").append(p.getEstado().getClass().getSimpleName()).append('\n')); return b.toString();
    }
    public Map<String,Double> indicadores(LocalDate desde,LocalDate hasta) {
        permiso(); if(desde.isAfter(hasta)) throw new IllegalArgumentException("Rango de fechas inválido");
        var cs=compras().stream().filter(c->fecha(c.compra().fechaCompra(),desde,hasta)).toList();
        Map<String,Double> r=new LinkedHashMap<>();
        for(String t:List.of("Películas","Funciones","Salas"))r.put(t,(double)catalogo.listar(t).size());
        r.put("Clientes",(double)clientes("").size()); r.put("Compras",(double)cs.size()); r.put("Ventas cobradas",cs.stream().mapToDouble(c->c.importePagado().doubleValue()).sum());
        r.put("Entradas",cs.stream().mapToDouble(c->c.compra().asientos().size()).sum());
        double capacidad=catalogo.listar("Funciones").stream().map(f->catalogo.funcion(f.id())).filter(f->fecha(f.getFechaHora(),desde,hasta)).mapToDouble(f->f.getSala().calcularCapacidad()).sum();
        double ocupados=compras().stream().filter(c->!c.compra().estaCancelada()&&fecha(c.compra().funcion().getFechaHora(),desde,hasta)).mapToDouble(c->c.compra().asientos().size()).sum();
        r.put("Ocupación %",capacidad==0?0:100*ocupados/capacidad);
        r.put("Confitería (unidades)",cs.stream().mapToDouble(c->c.compra().snacks().size()).sum());
        r.put("Confitería (subtotal)",cs.stream().mapToDouble(c->c.detallePago().confiteria().doubleValue()).sum());
        var refunds=compras().stream().filter(c->c.reembolso()!=null&&fecha(c.reembolso().fechaReembolso(),desde,hasta)).toList();
        r.put("Cancelaciones",(double)refunds.size()); r.put("Reembolsos",refunds.stream().mapToDouble(c->c.reembolso().montoReembolsado()).sum());
        r.put("Recargas",clientes("").stream().flatMap(c->c.getTarjeta().obtenerHistorial().stream()).filter(t->t.tipo()==TipoTransaccion.RECARGA&&fecha(t.fecha(),desde,hasta)).mapToDouble(Transaccion::monto).sum());
        long disponibles=0,vencidos=0,redimidos=0;
        for(var c:clientes(""))for(var p:c.getPuntosAcumulados()) {String estado=p.getEstado().getClass().getSimpleName();redimidos+=(long)p.getCantidad()-p.getCantidadRestante();if(estado.equals("PuntoVencido"))vencidos+=p.getCantidadRestante();else if(estado.equals("PuntoDisponible"))disponibles+=p.getCantidadRestante();}
        r.put("Puntos disponibles",(double)disponibles);r.put("Puntos vencidos",(double)vencidos);r.put("Puntos redimidos",(double)redimidos);return r;
    }
    public String reporte(LocalDate desde,LocalDate hasta) {
        permiso();StringBuilder b=new StringBuilder("CinemaUQ · Reporte administrativo\nDesde "+desde+" hasta "+hasta+"\nCatálogo y saldo de puntos: situación actual. Otros indicadores: intervalo indicado.\n\n");
        indicadores(desde,hasta).forEach((k,v)->b.append(k).append(": ").append(v).append('\n'));
        b.append("\nENTRADAS Y OCUPACIÓN POR FUNCIÓN\n");
        for(var ficha:catalogo.listar("Funciones")) {var f=catalogo.funcion(ficha.id());if(!fecha(f.getFechaHora(),desde,hasta))continue;
            long entradas=compras().stream().filter(c->c.compra().funcion().getId().equals(f.getId())&&!c.compra().estaCancelada()).mapToLong(c->c.compra().asientos().size()).sum();
            b.append(f.getId()).append(" · ").append(f.getPelicula()).append(" · ").append(f.getSala().getNumero()).append(" · ").append(f.getFechaHora()).append(" · entradas activas: ").append(entradas).append(" / ").append(f.getSala().calcularCapacidad()).append('\n');
        }
        for(String area:List.of("Compras","Confitería","Cancelaciones","Recargas","Puntos")) {b.append('\n').append(area.toUpperCase(Locale.ROOT)).append('\n'); consultar(area,desde,hasta,"").forEach(f->b.append(f).append('\n'));}return b.toString();
    }

    // Método principal Strategy: recibe cualquier exportador (TXT o PDF) y genera el archivo
    public void exportar(ExportadorReporte estrategia, Path destino, LocalDate desde, LocalDate hasta) throws IOException {
        permiso();
        if (estrategia == null) {
            throw new IllegalArgumentException("Se requiere una estrategia de exportación");
        }
        estrategia.exportar(reporte(desde, hasta), destino);
    }

    // Exporta en TXT usando la estrategia de texto
    public void exportarTxt(Path destino, LocalDate desde, LocalDate hasta) throws IOException {
        exportar(new ExportadorTxtStrategy(), destino, desde, hasta);
    }

    // Exporta en PDF usando la estrategia de documento PDF
    public void exportarPdf(Path destino, LocalDate desde, LocalDate hasta) throws IOException {
        exportar(new ExportadorPdfStrategy(), destino, desde, hasta);
    }

}
