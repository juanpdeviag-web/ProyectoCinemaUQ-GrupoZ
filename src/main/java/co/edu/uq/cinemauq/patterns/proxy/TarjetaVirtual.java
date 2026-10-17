package co.edu.uq.cinemauq.patterns.proxy;

public interface TarjetaVirtual {
    double consultarSaldo(String usuario);
    boolean pagar(String usuario, double monto);
}
