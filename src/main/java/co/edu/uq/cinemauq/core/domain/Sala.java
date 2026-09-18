package co.edu.uq.cinemauq.core.domain;

public class Sala {
    private String numero;
    private int capacidadFilas;
    private int capacidadColumnas;

    public Sala(String numero, int capacidadFilas, int capacidadColumnas) {
        this.numero = numero;
        this.capacidadFilas = capacidadFilas;
        this.capacidadColumnas = capacidadColumnas;
    }
}