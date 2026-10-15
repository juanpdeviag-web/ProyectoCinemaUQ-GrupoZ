package co.edu.uq.cinemauq.core.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class TarjetaVirtual {
    private double saldo;
    private boolean activa;
    final private String numeroTarjeta;
    final private LocalDateTime fechaCreacion;
    private LocalDateTime fechaUltimaActividad;
    final private List<Transaccion> historialTransacciones;

    public TarjetaVirtual() {
        this.saldo = 0.0;
        this.activa = true;
        this.numeroTarjeta = generarNumeroTarjeta();
        this.fechaCreacion = LocalDateTime.now();
        this.fechaUltimaActividad = LocalDateTime.now();
        this.historialTransacciones = new ArrayList<>();
    }

    public double getSaldo() { return saldo; }
    public boolean isActiva() { return activa; }
    public String getNumeroTarjeta() { return numeroTarjeta; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public LocalDateTime getFechaUltimaActividad() { return fechaUltimaActividad; }

    public void recargar(double monto) {
        if (monto <= 0) {
            throw new IllegalArgumentException("El monto de recarga debe ser positivo");
        }
        double saldoAnterior = this.saldo;
        this.saldo += monto;
        actualizarActividad();
        Transaccion transaccion = new Transaccion(
            UUID.randomUUID().toString(),
            TipoTransaccion.RECARGA,
            monto,
            saldoAnterior,
            this.saldo,
            LocalDateTime.now(),
            TipoTransaccion.RECARGA.getDescripcion()
        );
        registrarTransaccion(transaccion);
    }

    public void deducir(double monto) {
        if (monto <= 0) {
            throw new IllegalArgumentException("El monto a deducir debe ser positivo");
        }
        if (!validarSaldoSuficiente(monto)) {
            throw new IllegalStateException("Saldo insuficiente");
        }
        double saldoAnterior = this.saldo;
        this.saldo -= monto;
        actualizarActividad();
        Transaccion transaccion = new Transaccion(
            UUID.randomUUID().toString(),
            TipoTransaccion.COMPRA,
            monto,
            saldoAnterior,
            this.saldo,
            LocalDateTime.now(),
            TipoTransaccion.COMPRA.getDescripcion()
        );
        registrarTransaccion(transaccion);
    }

    public boolean validarSaldoSuficiente(double monto) {
        return this.saldo >= monto && monto > 0;
    }

    public List<Transaccion> obtenerHistorial() {
        return Collections.unmodifiableList(historialTransacciones);
    }

    public Transaccion obtenerUltimaTransaccion() {
        if (historialTransacciones.isEmpty()) {
            return null;
        }
        return historialTransacciones.get(historialTransacciones.size() - 1);
    }

    public void registrarTransaccion(Transaccion t) {
        historialTransacciones.add(t);
        actualizarActividad();
    }

    public void desactivar() {
        this.activa = false;
        actualizarActividad();
    }

    public void activar() {
        this.activa = true;
        actualizarActividad();
    }

    public String obtenerResumenTransacciones() {
        if (historialTransacciones.isEmpty()) {
            return "No hay transacciones registradas";
        }
        StringBuilder resumen = new StringBuilder();
        resumen.append("Resumen de Transacciones:\n");
        resumen.append("Total transacciones: ").append(historialTransacciones.size()).append("\n");
        double totalRecargas = historialTransacciones.stream()
            .filter(t -> t.tipo() == TipoTransaccion.RECARGA)
            .mapToDouble(Transaccion::monto)
            .sum();
        double totalCompras = historialTransacciones.stream()
            .filter(t -> t.tipo() == TipoTransaccion.COMPRA)
            .mapToDouble(Transaccion::monto)
            .sum();
        double totalReembolsos = historialTransacciones.stream()
            .filter(t -> t.tipo() == TipoTransaccion.REEMBOLSO)
            .mapToDouble(Transaccion::monto)
            .sum();
        resumen.append("Total recargas: $").append(totalRecargas).append("\n");
        resumen.append("Total compras: $").append(totalCompras).append("\n");
        resumen.append("Total reembolsos: $").append(totalReembolsos).append("\n");
        resumen.append("Saldo actual: $").append(saldo).append("\n");
        return resumen.toString();
    }

    public void agregarSaldoSinTransaccion(double monto) {
        this.saldo += monto;
        actualizarActividad();
    }

    private void actualizarActividad() {
        this.fechaUltimaActividad = LocalDateTime.now();
    }

    private String generarNumeroTarjeta() {
        return "TV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}