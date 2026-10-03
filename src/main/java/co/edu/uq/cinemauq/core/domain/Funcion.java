package co.edu.uq.cinemauq.core.domain;

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
}