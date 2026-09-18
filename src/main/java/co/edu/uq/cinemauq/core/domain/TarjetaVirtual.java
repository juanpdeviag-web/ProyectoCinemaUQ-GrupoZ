package co.edu.uq.cinemauq.core.domain;

public class TarjetaVirtual {
    private double saldo;
    private boolean activa;

    public TarjetaVirtual() {
        this.saldo = 0.0; // Inicia en 0 según la regla de negocio
        this.activa = true;
    }

    public double getSaldo() { return saldo; }
    public boolean isActiva() { return activa; }

    public void recargar(double monto) {
        if(monto > 0) this.saldo += monto;
    }
}