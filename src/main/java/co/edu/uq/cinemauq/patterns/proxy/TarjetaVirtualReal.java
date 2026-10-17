package co.edu.uq.cinemauq.patterns.proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Objeto real respaldado por el patron proxy.

public class TarjetaVirtualReal implements TarjetaVirtual {
    private final Map<String, Double> saldos = new ConcurrentHashMap<>();

    public TarjetaVirtualReal(Map<String, Double> saldosIniciales) { saldos.putAll(saldosIniciales); }

    @Override
    public double consultarSaldo(String usuario) {
        if (!saldos.containsKey(usuario)) throw new IllegalArgumentException("Usuario sin tarjeta");
        return saldos.get(usuario);
    }

    @Override
    public boolean pagar(String usuario, double monto) {
        if (monto <= 0 || !saldos.containsKey(usuario) || saldos.get(usuario) < monto) return false;
        saldos.put(usuario, saldos.get(usuario) - monto); return true;
    }
}
