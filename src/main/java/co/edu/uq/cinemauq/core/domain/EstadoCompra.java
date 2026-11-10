package co.edu.uq.cinemauq.core.domain;

/**
 * Enumeración que representa el estado de una compra.
 * Implementa: RF-0XX (Gestión de Estados de Compra)
 * Regla de Negocio: RN-011 (Estados de compra: ACTIVA, CANCELADA)
 */
public enum EstadoCompra {
    ACTIVA("Compra activa y válida"),
    CANCELADA("Compra cancelada por el cliente o el cine");

    private final String descripcion;

    EstadoCompra(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean esActiva() {
        return this == ACTIVA;
    }

    public boolean estaCancelada() {
        return this == CANCELADA;
    }
}
