package co.edu.uq.cinemauq.core.domain;

public class Cliente extends Usuario {
    private TarjetaVirtual tarjeta;
    private int puntos;

    public Cliente(String id, String nombre, String correo) {
        super(id, nombre, correo);
        this.tarjeta = new TarjetaVirtual(); // Tarjeta asociada por negocio
        this.puntos = 0;
    }

    @Override
    public String getRol() { return "CLIENTE"; }
}
