package cl.duoc.bancoxyz.bff.cajero.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

@Schema(description = "Solicitud de giro de dinero en cajero automático (ATM)")
public class SolicitudRetiroCajeroDto {

    // =========================================================================
    // VALIDACIÓN BEAN VALIDATION:
    // @NotNull: Evita que el monto sea nulo.
    // @Positive: Exige que el monto sea estrictamente mayor a cero ($ > 0).
    // =========================================================================
    @NotNull(message = "El monto a retirar es obligatorio")
    @Positive(message = "El monto a retirar debe ser estrictamente mayor a 0")
    @Schema(description = "Monto en pesos a retirar (múltiplo de $5.000)", example = "40000", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long monto;

    // =========================================================================
    // VALIDACIÓN BEAN VALIDATION:
    // @NotNull: Evita que el PIN sea nulo.
    // @Pattern: Valida el formato exacto mediante Expresión Regular (Regex):
    //    ^     -> Marca el inicio del texto.
    //    \\d   -> Acepta únicamente dígitos numéricos (del 0 al 9).
    //    {4}   -> Exige exactamente 4 dígitos de longitud (longitud fija).
    //    $     -> Marca el fin del texto.
    // =========================================================================
    @NotNull(message = "El PIN de seguridad es obligatorio")
    @Pattern(regexp = "^\\d{4}$", message = "El PIN de seguridad debe contener exactamente 4 dígitos numéricos")
    @Schema(description = "PIN de seguridad de 4 dígitos", example = "1234", requiredMode = Schema.RequiredMode.REQUIRED)
    private String pin;

    // =========================================================================
    // VALIDACIÓN BEAN VALIDATION:
    // @NotBlank: Exige que no sea nulo ni esté compuesto solo por espacios en blanco.
    // =========================================================================
    @NotBlank(message = "El identificador del terminal ATM es obligatorio")
    @Schema(description = "Identificador del terminal ATM", example = "ATM-SCL-01", requiredMode = Schema.RequiredMode.REQUIRED)
    private String terminalId;

    public SolicitudRetiroCajeroDto() {
    }

    public SolicitudRetiroCajeroDto(Long monto, String pin, String terminalId) {
        this.monto = monto;
        this.pin = pin;
        this.terminalId = terminalId;
    }

    public Long getMonto() {
        return monto;
    }

    public void setMonto(Long monto) {
        this.monto = monto;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }
}
