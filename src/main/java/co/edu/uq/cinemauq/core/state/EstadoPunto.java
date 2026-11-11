package co.edu.uq.cinemauq.core.state;

import co.edu.uq.cinemauq.core.domain.PuntoCinemaUQ;

/**
 * Estado de un lote de puntos CinemaUQ.
 * Patrón de comportamiento: State.
 */
public interface EstadoPunto {
    /**
     * Indica si el lote puede utilizarse para una redención.
     */
    boolean esValido(PuntoCinemaUQ punto);

    /**
     * Determina cuántos puntos pueden consumirse del lote.
     * La actualización de la cantidad y la transición de estado las realiza el contexto PuntoCinemaUQ.
     */
    int redimir(PuntoCinemaUQ punto, int cantidadSolicitada);

    /**
     * Nombre legible del estado para UI y reportes.
     */
    String getNombre();
}
