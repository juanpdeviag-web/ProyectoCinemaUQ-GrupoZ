package co.edu.uq.cinemauq.core.domain;
import co.edu.uq.cinemauq.core.exceptions.UsuarioException;

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



    @Override
    public void validar() {
        super.validar();
        if (this.tarjeta == null) {
            throw new UsuarioException("El cliente debe tener una tarjeta virtual asociada (RN-001)");
        }
        if (this.puntos < 0) {
            throw new UsuarioException("Los puntos del cliente no pueden ser negativos");
        }
    }

    public void agregarPuntos(int cantidad) {
        if (cantidad < 0) {
            throw new UsuarioException("La cantidad de puntos para agregar deben ser positivas");
        }
        this.puntos += cantidad;
    }

    public boolean verificarPuntosAcumulados(int puntosRequeridos) {
        if (puntosRequeridos < 0) {
            throw new UsuarioException("Los puntos requeridos no pueden ser negativos");
        }
        return this.puntos >= puntosRequeridos;
    }

    public TarjetaVirtual getTarjeta() {
        return tarjeta;
    }

    public TarjetaVirtual getTarjetaVirtual() {
        return tarjeta;
    }

    public void setTarjeta(TarjetaVirtual tarjeta) {
        if (tarjeta == null) {
            throw new UsuarioException("La tarjeta virtual asociada no puede ser nula (RN-001)");
        }
        this.tarjeta = tarjeta;
    }

    public int getPuntos() {
        return puntos;
    }

}
