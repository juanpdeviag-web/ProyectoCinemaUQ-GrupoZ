package co.edu.uq.cinemauq.core.domain;

import java.time.LocalDateTime;

/**
 * Representa una reserva de asiento realizada por un usuario.
 * Guarda información detallada sobre quién compró el asiento, cuándo y para qué función.
 */
public class ReservaAsiento {

    private final String id;
    private final String usuarioId;
    private final String usuarioNombre;
    private final String funcionId;
    private final String peliculaTitulo;
    private final String salaNumero;
    private final LocalDateTime fechaFuncion;
    private final String asientoId;
    private final double montoPagado;
    private final LocalDateTime fechaCompra;

    public ReservaAsiento(String usuarioId, String usuarioNombre, String funcionId,
                          String peliculaTitulo, String salaNumero, LocalDateTime fechaFuncion,
                          String asientoId, double montoPagado) {
        this.id = java.util.UUID.randomUUID().toString();
        this.usuarioId = usuarioId;
        this.usuarioNombre = usuarioNombre;
        this.funcionId = funcionId;
        this.peliculaTitulo = peliculaTitulo;
        this.salaNumero = salaNumero;
        this.fechaFuncion = fechaFuncion;
        this.asientoId = asientoId;
        this.montoPagado = montoPagado;
        this.fechaCompra = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public String getUsuarioNombre() {
        return usuarioNombre;
    }

    public String getFuncionId() {
        return funcionId;
    }

    public String getPeliculaTitulo() {
        return peliculaTitulo;
    }

    public String getSalaNumero() {
        return salaNumero;
    }

    public LocalDateTime getFechaFuncion() {
        return fechaFuncion;
    }

    public String getAsientoId() {
        return asientoId;
    }

    public double getMontoPagado() {
        return montoPagado;
    }

    public LocalDateTime getFechaCompra() {
        return fechaCompra;
    }

    @Override
    public String toString() {
        return String.format("Reserva[%s - %s - Asiento: %s - %s - $%.0f]",
                peliculaTitulo, fechaFuncion.toLocalDate(), asientoId, usuarioNombre, montoPagado);
    }
}
