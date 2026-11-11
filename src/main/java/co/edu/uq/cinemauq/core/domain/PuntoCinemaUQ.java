package co.edu.uq.cinemauq.core.domain;

import co.edu.uq.cinemauq.core.exceptions.UsuarioException;
import co.edu.uq.cinemauq.patterns.state.EstadoPunto;
import co.edu.uq.cinemauq.patterns.state.PuntoDisponible;
import java.time.LocalDate;

/** Contexto State de un lote con fecha de vencimiento propia. */
public final class PuntoCinemaUQ {
    private final int cantidad;
    private int cantidadRestante;
    private final LocalDate fechaObtencion;
    private final LocalDate fechaVencimiento;
    private EstadoPunto estado;

    public PuntoCinemaUQ(int cantidad, LocalDate fechaObtencion) {
        if (cantidad <= 0) {
            throw new UsuarioException("La cantidad del lote debe ser positiva");
        }
        if (fechaObtencion == null || fechaObtencion.isAfter(LocalDate.now())) {
            throw new UsuarioException("La fecha de obtención es obligatoria y no puede ser futura");
        }
        this.cantidad = cantidad;
        this.cantidadRestante = cantidad;
        this.fechaObtencion = fechaObtencion;
        this.fechaVencimiento = fechaObtencion.plusYears(1);
        this.estado = new PuntoDisponible();
        actualizarEstado();
    }

    private void actualizarEstado() {
        estado = estado.actualizarEstado(this);
    }

    public boolean estaDisponible() {
        actualizarEstado();
        return estado.estaDisponible();
    }

    public void redimir(int cantidad) {
        actualizarEstado();
        EstadoPunto siguiente = estado.redimir(this, cantidad);
        cantidadRestante -= cantidad;
        estado = siguiente;
    }

    public int getCantidad() { return cantidad; }
    public int getCantidadRestante() { return cantidadRestante; }
    public LocalDate getFechaObtencion() { return fechaObtencion; }
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }

    public EstadoPunto getEstado() {
        actualizarEstado();
        return estado;
    }

    // Soporte interno de transacción; no altera las transiciones del patrón State.
    record Respaldo(int cantidadRestante, EstadoPunto estado) { }

    Respaldo respaldar() {
        return new Respaldo(cantidadRestante, estado);
    }

    void restaurar(Respaldo respaldo) {
        cantidadRestante = respaldo.cantidadRestante();
        estado = respaldo.estado();
    }
}
