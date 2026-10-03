package co.edu.uq.cinemauq.core.domain;

/**
 * Representa una película del catálogo.
 * Implementa: RF-002 (Consulta de Cartelera), RF-004 (Programación de Funciones)
 */

public class Pelicula {
    private String titulo;
    private String genero;
    private int duracionMinutos;

    public Pelicula(String titulo, String genero, int duracionMinutos) {
        this.titulo = titulo;
        this.genero = genero;
        this.duracionMinutos = duracionMinutos;
    }

    // Getters y Setters básicos
    public String getTitulo() { return titulo; }
}