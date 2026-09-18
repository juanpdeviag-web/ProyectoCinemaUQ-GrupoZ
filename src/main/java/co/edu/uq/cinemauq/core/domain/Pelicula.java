package co.edu.uq.cinemauq.core.domain;

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