package cl.duoc.bancoxyz.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public class Transaccion {

    private Long id;
    private Long cuentaId;

    // =========================================================================
    // NORMALIZACIÓN TEMPORAL (java.time.LocalDate):
    // Garantiza representación temporal homogénea e inmutable.
    // Serialización uniforme en formato estándar internacional ISO-8601 (yyyy-MM-dd).
    // =========================================================================
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Schema(description = "Fecha de la transacción en formato ISO-8601", example = "2024-06-15")
    private LocalDate fecha;

    private Long monto;
    private String tipo;
    private String descripcion;
    private String canal;

    public Transaccion() {
    }

    public Transaccion(Long id, Long cuentaId, LocalDate fecha, Long monto, String tipo, String descripcion, String canal) {
        this.id = id;
        this.cuentaId = cuentaId;
        this.fecha = fecha;
        this.monto = monto;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.canal = canal;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getMonto() {
        return monto;
    }

    public void setMonto(Long monto) {
        this.monto = monto;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getCanal() {
        return canal;
    }

    public void setCanal(String canal) {
        this.canal = canal;
    }
}
