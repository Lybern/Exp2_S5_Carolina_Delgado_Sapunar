package cl.duoc.bancoxyz.bff.movil.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Representación mínima de un movimiento bancario para la app móvil")
public class TransaccionMovilDto {

    @Schema(description = "ID de la transacción", example = "5001")
    private Long id;

    // =========================================================================
    // NORMALIZACIÓN TEMPORAL (java.time.LocalDate):
    // Serialización consistente en formato ISO-8601 (yyyy-MM-dd) para app móvil
    // =========================================================================
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Schema(description = "Fecha de la operación en formato ISO-8601", example = "2024-03-01")
    private LocalDate fecha;

    @Schema(description = "Monto en pesos (positivo para abono, negativo para cargo)", example = "-15000")
    private Long monto;

    @Schema(description = "Tipo de transacción", example = "debito")
    private String tipo;

    @Schema(description = "Glosa breve del movimiento", example = "Supermercado")
    private String descripcion;

    public TransaccionMovilDto() {
    }

    public TransaccionMovilDto(Long id, LocalDate fecha, Long monto, String tipo, String descripcion) {
        this.id = id;
        this.fecha = fecha;
        this.monto = monto;
        this.tipo = tipo;
        this.descripcion = descripcion;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
}
