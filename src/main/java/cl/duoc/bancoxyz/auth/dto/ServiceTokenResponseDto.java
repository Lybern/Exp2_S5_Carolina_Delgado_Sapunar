package cl.duoc.bancoxyz.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// =========================================================================
// DTO DE RESPUESTA DE TOKEN DE SERVICIO:
// Entrega el token de servicio firmado criptográficamente con la clave interna.
// =========================================================================
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta con Token Delegado de Servicio para comunicación interna BFF-Microservicio")
public class ServiceTokenResponseDto {

    @Schema(description = "Token JWT de Servicio firmado con clave compartida interna", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String serviceToken;

    @Schema(description = "Esquema de autenticación HTTP", example = "Bearer")
    @Builder.Default
    private String tipoToken = "Bearer";

    @Schema(description = "Canal BFF que actúa como emisor (iss)", example = "bff-movil")
    private String emisorBff;

    @Schema(description = "Audiencia del token de servicio (aud)", example = "core-bancario")
    @Builder.Default
    private String audiencia = "core-bancario";

    @Schema(description = "Usuario delegado asociado a la transacción", example = "usuario_movil")
    private String username;

    @Schema(description = "Tiempo de validez en milisegundos", example = "300000")
    private long expiracionMilisegundos;
}
