package co.edu.uq.cinemauq.core.domain;
import co.edu.uq.cinemauq.core.exceptions.MontoInvalidoException;
import co.edu.uq.cinemauq.core.exceptions.SaldoInsuficienteException;
import co.edu.uq.cinemauq.core.exceptions.TarjetaInactivaException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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


    private final List<String> historialTransacciones = new ArrayList<>();

    public void validarRecarga(double monto) {
        if (!this.activa) {
            throw new TarjetaInactivaException("No se puede recargar a una tarjeta inactiva");
        }
        if (monto <= 0) {
            throw new MontoInvalidoException("El monto de recarga debe ser positivo (RN-002)");
        }
        this.saldo += monto;
        this.historialTransacciones.add("RECARGA: +" + monto + " | Saldo: " + this.saldo);
    }

    public boolean validarSaldoSuficiente(double monto) {
        if (!this.activa) {
            throw new TarjetaInactivaException("La tarjeta virtual esta inactiva");
        }
        if (monto <= 0) {
            throw new MontoInvalidoException("El monto a validar debe ser positivo (RN-002)");
        }
        return this.saldo >= monto;
    }

    public void deducir(double monto) {
        if (!validarSaldoSuficiente(monto)) {
            throw new SaldoInsuficienteException(this.saldo, monto);
        }
        this.saldo -= monto;
        this.historialTransacciones.add("DEDUCCION: -" + monto + " | Saldo: " + this.saldo);
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }

    public List<String> getHistorialTransacciones() {
        return Collections.unmodifiableList(historialTransacciones);
    }

}