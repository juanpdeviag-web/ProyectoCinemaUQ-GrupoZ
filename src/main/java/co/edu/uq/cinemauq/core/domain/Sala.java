package co.edu.uq.cinemauq.core.domain;

/**
 * Representa una sala de cine.
 * Implementa: RF-003 (Gestión de Salas), RF-004 (Programación de Funciones)
 * Regla de Negocio: RN-004 (Cálculo de capacidad de sala)
 */

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