package co.edu.uq.cinemauq.core.domain;

/**
 * Representa un cliente del sistema.
 * Implementa: RF-001 (Registro de Usuarios), RF-006 (Gestión de Tarjeta Virtual)
 * Regla de Negocio: RN-001 (Asignación automática de tarjeta virtual)
 */

public class Cliente extends Usuario {
    private TarjetaVirtual tarjeta;
    private int puntos;

    public Cliente(String id, String nombre, String correo) {
        super(id, nombre, correo);
        this.tarjeta = new TarjetaVirtual(); // RN-001: Tarjeta asociada por negocio
        this.puntos = 0;
    }

    @Override
    public String getRol() { return "CLIENTE"; }
}
