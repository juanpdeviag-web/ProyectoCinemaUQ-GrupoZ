package co.edu.uq.cinemauq.core.domain;

public enum TipoTransaccion {
    RECARGA,
    COMPRA,
    REEMBOLSO;

    public String getDescripcion() {
        return switch (this) {
            case RECARGA -> "Recarga de saldo";
            case COMPRA -> "Compra de boletos";
            case REEMBOLSO -> "Reembolso de compra";
        };
    }
}
