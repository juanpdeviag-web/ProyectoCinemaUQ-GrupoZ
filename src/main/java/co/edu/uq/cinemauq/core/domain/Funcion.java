package co.edu.uq.cinemauq.core.domain;
import co.edu.uq.cinemauq.core.exceptions.FuncionException;
import co.edu.uq.cinemauq.core.exceptions.FuncionSuperpuestaException;

import java.time.LocalDateTime;

/**
 * Representa una función de cine (proyección).
 * Implementa: RF-004 (Programación de Funciones), RF-005 (Compra de Boletos)
 */

public class Funcion {
    private Pelicula pelicula;
    private Sala sala;
    private LocalDateTime fechaHora;

    public Funcion(Pelicula pelicula, Sala sala, LocalDateTime fechaHora) {
        this.pelicula = pelicula;
        this.sala = sala;
        this.fechaHora = fechaHora;
    }

    public Pelicula getPelicula() { return pelicula; }


    public void validar() {
        if (pelicula == null) {
            throw new FuncionException("La película asociada a la función no puede ser nula (RF-004)");
        }
        if (sala == null) {
            throw new FuncionException("La sala asociada a la función no puede ser nula (RF-004)");
        }
        if (fechaHora == null || !fechaHora.isAfter(LocalDateTime.now())) {
            throw new FuncionException("La fecha y hora de la función debe ser futura (RF-004)");
        }
    }

    public void validarNoSuperpuesta(Funcion otra) {
        if (otra == null || otra.getSala() == null || this.sala == null) {
            return;
        }
        if (!this.sala.getNumero().equals(otra.getSala().getNumero())) {
            return;
        }
        LocalDateTime inicioEsta = this.fechaHora;
        LocalDateTime finEsta = inicioEsta.plusMinutes(this.pelicula.getDuracionMinutos());

        LocalDateTime inicioOtra = otra.getFechaHora();
        LocalDateTime finOtra = inicioOtra.plusMinutes(otra.getPelicula().getDuracionMinutos());

        if (inicioEsta.isBefore(finOtra) && inicioOtra.isBefore(finEsta)) {
            throw new FuncionSuperpuestaException(
                    "Conflicto de horarios en la sala " + sala.getNumero() + " para la película " + pelicula.getTitulo()
            );
        }
    }

    public Sala getSala() {
        return sala;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

}