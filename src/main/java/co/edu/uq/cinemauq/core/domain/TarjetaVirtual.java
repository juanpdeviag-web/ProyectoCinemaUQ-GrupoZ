package co.edu.uq.cinemauq.core.domain;

/**
 * Representa la tarjeta virtual de saldo de un cliente.
 * Implementa: RF-006 (Gestión de Tarjeta Virtual), RF-007 (Procesamiento de Pagos)
 * Reglas de Negocio: RN-001 (Saldo inicial $0), RN-002 (Validación de recargas positivas)
 */

public class TarjetaVirtual {
    private double saldo;
    private boolean activa;

    public TarjetaVirtual() {
        this.saldo = 0.0; // RN-001: Inicia en 0 según la regla de negocio
        this.activa = true;
    }

    public double getSaldo() { return saldo; }
    public boolean isActiva() { return activa; }

    /**
     * Recarga la tarjeta con el monto especificado.
     * RN-002: Solo permite montos positivos
     */
    public void recargar(double monto) {
        if(monto > 0) this.saldo += monto;
    }
}