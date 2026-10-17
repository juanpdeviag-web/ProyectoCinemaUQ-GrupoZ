package co.edu.uq.cinemauq.core.domain;
import co.edu.uq.cinemauq.core.exceptions.SalaException;

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


    public void validar() {
        if (numero == null || numero.trim().isEmpty()) {
            throw new SalaException("El número o identificador de la sala no puede estar vacío (RF-003)");
        }


        if (capacidadFilas <= 0 || capacidadColumnas <= 0) {
            throw new SalaException("Las filas y columnas de la sala deben ser mayores a cero (RF-003)");
        }
        if (calcularCapacidad() <= 0) {
            throw new SalaException("La capacidad calculada de la sala debe ser mayor a cero (RN-004)");
        }
    }

    public int calcularCapacidad() {
        if (capacidadFilas <= 0 || capacidadColumnas <= 0) {
            throw new SalaException("Las filas y columnas deben ser positivas para calcular su capacidad (RN-004)");
        }
        return this.capacidadFilas * this.capacidadColumnas;
    }

    public String getNumero() {
        return numero;
    }

    public int getCapacidadFilas() {
        return capacidadFilas;
    }

    public int getCapacidadColumnas() {
        return capacidadColumnas;
    }

}