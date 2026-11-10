package co.edu.uq.cinemauq.core.domain;
import co.edu.uq.cinemauq.core.exceptions.PeliculaException;

/**
 * Representa una película del catálogo.
 * Implementa: RF-002 (Consulta de Cartelera), RF-004 (Programación de Funciones)
 */

public class Pelicula {
    private String titulo;
    private String genero;
    private int duracionMinutos;
    private double precio;

    public Pelicula(String titulo, String genero, int duracionMinutos) {
        this.titulo = titulo;
        this.genero = genero;
        this.duracionMinutos = duracionMinutos;
        this.precio = 15.0;
    }

    public Pelicula(String titulo, String genero, int duracionMinutos, double precio) {
        this.titulo = titulo;
        this.genero = genero;
        this.duracionMinutos = duracionMinutos;
        this.precio = precio;
    }

    // Getters y Setters básicos
    public String getTitulo() { return titulo; }


    public void validar() {
        if (titulo == null || titulo.trim().isEmpty()) {
            throw new PeliculaException("El título de la película no puede estar vacío (RF-002)");
        }
        if (genero == null || genero.trim().isEmpty()) {
            throw new PeliculaException("El género de la película no puede estar vacío (RF-002)");
        }
        if (duracionMinutos <= 0) {
            throw new PeliculaException("La duración de la película debe ser mayor a cero (RF-002)");
        }
    }

    public String getGenero() {
        return genero;
    }

    public int getDuracionMinutos() {
        return duracionMinutos;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        if (precio <= 0) {
            throw new PeliculaException("El precio de la película debe ser positivo");
        }
        this.precio = precio;
    }

    public void setTitulo(String titulo) {
        if (titulo == null || titulo.trim().isEmpty()) {
            throw new PeliculaException("El título de la película no puede estar vacío");
        }
        this.titulo = titulo;
    }

    public void setGenero(String genero) {
        if (genero == null || genero.trim().isEmpty()) {
            throw new PeliculaException("El género de la película no puede estar vacío");
        }
        this.genero = genero;
    }

    public void setDuracionMinutos(int duracionMinutos) {
        if (duracionMinutos <= 0) {
            throw new PeliculaException("La duración de la película debe ser positiva");
        }
        this.duracionMinutos = duracionMinutos;
    }

    @Override
    public String toString() {
        return titulo;
    }

}