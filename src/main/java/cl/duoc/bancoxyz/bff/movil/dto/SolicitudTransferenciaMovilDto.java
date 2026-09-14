package cl.duoc.bancoxyz.bff.movil.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "Cuerpo de solicitud para realizar transferencias desde la app móvil")
public class SolicitudTransferenciaMovilDto {

    // =========================================================================
    // VALIDACIÓN BEAN VALIDATION:
    // @NotNull: Exige que se especifique el número de cuenta de destino.
    // @Positive: La cuenta de destino debe ser un identificador numérico positivo.
    // =========================================================================
    @NotNull(message = "El ID de la cuenta destinataria es obligatorio")
    @Positive(message = "El ID de la cuenta destinataria debe ser un identificador positivo")
    @Schema(description = "ID de la cuenta destinataria", example = "102", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long cuentaDestinoId;

    // =========================================================================
    // VALIDACIÓN BEAN VALIDATION:
    // @NotNull: El monto es obligatorio.
    // @Positive: Solo se permiten transferencias con montos mayores a $0.
    // =========================================================================
    @NotNull(message = "El monto a transferir es obligatorio")
    @Positive(message = "El monto a transferir debe ser estrictamente mayor a 0")
    @Schema(description = "Monto a transferir en pesos chilenos", example = "25000", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long monto;

    // =========================================================================
    // VALIDACIÓN BEAN VALIDATION:
    // @Size: Limita la longitud del comentario opcional para proteger la base de datos.
    // =========================================================================
    @Size(max = 100, message = "El comentario no puede exceder los 100 caracteres")
    @Schema(description = "Comentario o asunto opcional de la transferencia", example = "Pago almuerzo")
    private String comentario;

    public SolicitudTransferenciaMovilDto() {
    }

    public SolicitudTransferenciaMovilDto(Long cuentaDestinoId, Long monto, String comentario) {
        this.cuentaDestinoId = cuentaDestinoId;
        this.monto = monto;
        this.comentario = comentario;
    }

    public Long getCuentaDestinoId() {
        return cuentaDestinoId;
    }

    public void setCuentaDestinoId(Long cuentaDestinoId) {
        this.cuentaDestinoId = cuentaDestinoId;
    }

    public Long getMonto() {
        return monto;
    }

    public void setMonto(Long monto) {
        this.monto = monto;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }
}
