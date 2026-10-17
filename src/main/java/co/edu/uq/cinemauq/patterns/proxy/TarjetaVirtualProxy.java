package co.edu.uq.cinemauq.patterns.proxy;
import java.util.Map;
import java.util.Set;

//Controla autorización antes de permitir acceso a la tarjeta real.

public class TarjetaVirtualProxy implements TarjetaVirtual {
    private final TarjetaVirtualReal tarjeta;
    private final Map<String, Set<String>> permisos;

    public TarjetaVirtualProxy(TarjetaVirtualReal tarjeta, Map<String, Set<String>> permisos) {
        if (tarjeta == null || permisos == null) throw new IllegalArgumentException("Dependencias requeridas");
        this.tarjeta = tarjeta; this.permisos = Map.copyOf(permisos);
    }

    private void autorizar(String usuario, String accion) {
        if (usuario == null || !permisos.getOrDefault(usuario, Set.of()).contains(accion))
            throw new SecurityException("Acceso denegado: " + accion);
    }

    @Override
    public double consultarSaldo(String usuario) {
        autorizar(usuario, "CONSULTAR");
        return tarjeta.consultarSaldo(usuario);
    }

    @Override
    public boolean pagar(String usuario, double monto) {
        autorizar(usuario, "PAGAR");
        return tarjeta.pagar(usuario, monto);
    }
}
