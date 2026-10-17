package co.edu.uq.cinemauq.core.exceptions;

public class SaldoInsuficienteException extends TarjetaException {
    public SaldoInsuficienteException(double saldoActual, double montoRequerido) {
        super(String.format("Saldo insuficiente. Saldo actual: $%.2f, Monto requerido: $%.2f", saldoActual, montoRequerido));
    }

    public SaldoInsuficienteException(String message) {
        super(message);
    }
}