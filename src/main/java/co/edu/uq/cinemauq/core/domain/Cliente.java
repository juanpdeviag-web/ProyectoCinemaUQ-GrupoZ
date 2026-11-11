package co.edu.uq.cinemauq.core.domain;
import co.edu.uq.cinemauq.core.exceptions.UsuarioException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representa un cliente del sistema.
 * Implementa: RF-001 (Registro de Usuarios), RF-006 (Gestión de Tarjeta Virtual)
 * Regla de Negocio: RN-001 (Asignación automática de tarjeta virtual)
 */

public class Cliente extends Usuario {
    private TarjetaVirtual tarjeta;
    private final List<PuntoCinemaUQ> puntosAcumulados;

    public Cliente(String id, String nombre, String correo) {
        super(id, nombre, correo);
        this.tarjeta = new TarjetaVirtual(); // RN-001: Tarjeta asociada por negocio
        this.puntosAcumulados = new ArrayList<>();
    }

    @Override
    public String getRol() { return "CLIENTE"; }



    @Override
    public void validar() {
        super.validar();
        if (this.tarjeta == null) {
            throw new UsuarioException("El cliente debe tener una tarjeta virtual asociada (RN-001)");
        }
        for (PuntoCinemaUQ lote : puntosAcumulados) {
            if (lote == null || lote.getCantidad() <= 0 || lote.getCantidadRestante() < 0
                    || lote.getCantidadRestante() > lote.getCantidad()) {
                throw new UsuarioException("El cliente tiene un lote de puntos inválido");
            }
        }
    }

    public void agregarPuntos(int cantidad) {
        if (cantidad < 0) {
            throw new UsuarioException("La cantidad de puntos para agregar deben ser positivas");
        }
        if (cantidad > 0) {
            agregarPuntos(new PuntoCinemaUQ(cantidad, java.time.LocalDate.now()));
        }
    }

    public void agregarPuntos(PuntoCinemaUQ lote) {
        if (lote == null || puntosAcumulados.contains(lote)) {
            throw new UsuarioException("El lote no puede ser nulo ni estar registrado previamente");
        }
        puntosAcumulados.add(lote);
    }

    public List<PuntoCinemaUQ> getPuntosAcumulados() {
        return Collections.unmodifiableList(puntosAcumulados);
    }

    public boolean verificarPuntosAcumulados(int puntosRequeridos) {
        if (puntosRequeridos < 0) {
            throw new UsuarioException("Los puntos requeridos no pueden ser negativos");
        }
        return getPuntos() >= puntosRequeridos;
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
        return puntosAcumulados.stream()
                .filter(PuntoCinemaUQ::estaDisponible)
                .mapToInt(PuntoCinemaUQ::getCantidadRestante)
                .reduce(0, Math::addExact);
    }

}
