package cl.duoc.bancoxyz.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public class MovimientoAnual {

    private Long cuentaId;

    // =========================================================================
    // NORMALIZACIÓN TEMPORAL (java.time.LocalDate):
    // Formato homogéneo ISO-8601 para movimientos anuales e históricos.
    // =========================================================================
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Schema(description = "Fecha del movimiento en formato ISO-8601", example = "2024-01-15")
    private LocalDate fecha;

    private String transaccion;
    private Long monto;
    private String descripcion;

    public MovimientoAnual() {
    }

    public MovimientoAnual(Long cuentaId, LocalDate fecha, String transaccion, Long monto, String descripcion) {
        this.cuentaId = cuentaId;
        this.fecha = fecha;
        this.transaccion = transaccion;
        this.monto = monto;
        this.descripcion = descripcion;
    }

    public Long getCuentaId() {
        return cuentaId;
    }

    public void setCuentaId(Long cuentaId) {
        this.cuentaId = cuentaId;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getTransaccion() {
        return transaccion;
    }

    public void setTransaccion(String transaccion) {
        this.transaccion = transaccion;
    }

    public Long getMonto() {
        return monto;
    }

    public void setMonto(Long monto) {
        this.monto = monto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
