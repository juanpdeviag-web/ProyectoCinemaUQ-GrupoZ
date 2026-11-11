package co.edu.uq.cinemauq.core.domain;

import co.edu.uq.cinemauq.core.exceptions.PuntosException;
import co.edu.uq.cinemauq.core.state.EstadoPunto;
import co.edu.uq.cinemauq.core.state.PuntoDisponible;
import co.edu.uq.cinemauq.core.state.PuntoRedimido;
import co.edu.uq.cinemauq.core.state.PuntoVencido;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Representa un lote de puntos obtenido en una fecha específica.
 * Los puntos vencen un año después de su obtención.
 *
 * Contexto del patrón State: delega en EstadoPunto la posibilidad de redimir.
 */
public class PuntoCinemaUQ {
    private int cantidad;
    private final int cantidadOriginal;
    private final LocalDate fechaObtencion;
    private final LocalDate fechaVencimiento;
    private EstadoPunto estado;

    public PuntoCinemaUQ(int cantidad, LocalDate fechaObtencion) {
        this(cantidad, fechaObtencion, fechaObtencion.plusYears(1));
    }

    /**
     * Constructor explícito útil para carga de datos y pruebas.
     */
    public PuntoCinemaUQ(int cantidad, LocalDate fechaObtencion, LocalDate fechaVencimiento) {
        if (cantidad <= 0) {
            throw new PuntosException("La cantidad de puntos debe ser mayor que cero");
        }
        this.fechaObtencion = Objects.requireNonNull(fechaObtencion, "La fecha de obtención es obligatoria");
        this.fechaVencimiento = Objects.requireNonNull(fechaVencimiento, "La fecha de vencimiento es obligatoria");
        if (fechaVencimiento.isBefore(fechaObtencion)) {
            throw new PuntosException("La fecha de vencimiento no puede ser anterior a la fecha de obtención");
        }

        this.cantidad = cantidad;
        this.cantidadOriginal = cantidad;
        this.estado = new PuntoDisponible();
        actualizarEstado();
    }

    /**
     * Actualiza automáticamente el estado por vigencia.
     * Un lote completamente redimido conserva el estado REDIMIDO.
     */
    public void actualizarEstado() {
        if (estado instanceof PuntoRedimido) {
            return;
        }

        if (cantidad == 0) {
            estado = new PuntoRedimido();
        } else if (LocalDate.now().isAfter(fechaVencimiento)) {
            estado = new PuntoVencido();
        } else {
            estado = new PuntoDisponible();
        }
    }

    public boolean esValido() {
        actualizarEstado();
        return estado.esValido(this);
    }

    /**
     * Redime hasta la cantidad solicitada y retorna la cantidad realmente consumida.
     */
    public int redimir(int cantidadSolicitada) {
        actualizarEstado();
        int consumidos = estado.redimir(this, cantidadSolicitada);
        cantidad -= consumidos;
        if (cantidad == 0) {
            estado = new PuntoRedimido();
        }
        return consumidos;
    }

    public int getCantidad() {
        return cantidad;
    }

    public int getCantidadOriginal() {
        return cantidadOriginal;
    }

    public LocalDate getFechaObtencion() {
        return fechaObtencion;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public EstadoPunto getEstado() {
        actualizarEstado();
        return estado;
    }

    public String getNombreEstado() {
        return getEstado().getNombre();
    }

    @Override
    public String toString() {
        return "PuntoCinemaUQ{" +
                "cantidad=" + cantidad +
                ", fechaObtencion=" + fechaObtencion +
                ", fechaVencimiento=" + fechaVencimiento +
                ", estado=" + getNombreEstado() +
                '}';
    }
}
