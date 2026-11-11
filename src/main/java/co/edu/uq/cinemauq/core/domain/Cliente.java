package co.edu.uq.cinemauq.core.domain;

import co.edu.uq.cinemauq.core.exceptions.UsuarioException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representa un cliente del sistema.
 * Implementa: RF-001 (Registro de Usuarios), RF-006 (Gestión de Tarjeta Virtual)
 * y el programa de puntos CinemaUQ con vigencia anual.
 * Regla de Negocio: RN-001 (Asignación automática de tarjeta virtual)
 */
public class Cliente extends Usuario {
    private TarjetaVirtual tarjeta;
    private final List<PuntoCinemaUQ> puntosAcumulados;

    public Cliente(String id, String nombre, String correo) {
        super(id, nombre, correo);
        this.tarjeta = new TarjetaVirtual(); // RN-001: Tarjeta asociada por negocio
        this.puntosAcumulados = new ArrayList<>();
    }

    @Override
    public String getRol() {
        return "CLIENTE";
    }

    @Override
    public void validar() {
        super.validar();
        if (this.tarjeta == null) {
            throw new UsuarioException("El cliente debe tener una tarjeta virtual asociada (RN-001)");
        }
        if (this.puntosAcumulados == null) {
            throw new UsuarioException("La colección de puntos del cliente no puede ser nula");
        }
        if (this.puntosAcumulados.stream().anyMatch(p -> p == null || p.getCantidad() < 0)) {
            throw new UsuarioException("Los lotes de puntos del cliente contienen datos inválidos");
        }
    }

    /**
     * Compatibilidad con el código anterior: agrega un lote con vigencia de un año desde hoy.
     * Para la lógica de negocio nueva se recomienda PuntosService.acumularPuntos(...).
     */
    public void agregarPuntos(int cantidad) {
        if (cantidad <= 0) {
            throw new UsuarioException("La cantidad de puntos para agregar debe ser mayor que cero");
        }
        agregarLotePuntos(new PuntoCinemaUQ(cantidad, LocalDate.now()));
    }

    public void agregarLotePuntos(PuntoCinemaUQ lote) {
        if (lote == null) {
            throw new UsuarioException("El lote de puntos no puede ser nulo");
        }
        this.puntosAcumulados.add(lote);
    }

    /**
     * Verifica únicamente puntos vigentes y todavía no consumidos.
     */
    public boolean verificarPuntosAcumulados(int puntosRequeridos) {
        if (puntosRequeridos < 0) {
            throw new UsuarioException("Los puntos requeridos no pueden ser negativos");
        }
        return getPuntos() >= puntosRequeridos;
    }

    public TarjetaVirtual getTarjeta() {
        return tarjeta;
    }

    public TarjetaVirtual getTarjetaVirtual() {
        return tarjeta;
    }

    public void setTarjeta(TarjetaVirtual tarjeta) {
        if (tarjeta == null) {
            throw new UsuarioException("La tarjeta virtual asociada no puede ser nula (RN-001)");
        }
        this.tarjeta = tarjeta;
    }

    /**
     * Retorna el saldo total de puntos vigentes. Se conserva el nombre getPuntos()
     * para no romper código previo del proyecto.
     */
    public int getPuntos() {
        return puntosAcumulados.stream()
                .filter(PuntoCinemaUQ::esValido)
                .mapToInt(PuntoCinemaUQ::getCantidad)
                .sum();
    }

    /**
     * Expone una vista de solo lectura. Los lotes conservan su comportamiento State.
     */
    public List<PuntoCinemaUQ> getPuntosAcumulados() {
        return Collections.unmodifiableList(puntosAcumulados);
    }
}
