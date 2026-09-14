package cl.duoc.bancoxyz.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// =========================================================================
// DTO DE RESPUESTA DE LOGIN:
// Retorna el token JWT generado, el tipo Bearer, el usuario, canal y expiración.
// =========================================================================
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta exitosa de autenticación con Token JWT")
public class LoginResponseDto {

    @Schema(description = "Token JWT firmado con HMAC-SHA256", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;

    @Schema(description = "Esquema de autenticación HTTP", example = "Bearer")
    @Builder.Default
    private String tipoToken = "Bearer";

    @Schema(description = "Nombre de usuario autenticado", example = "usuario_movil")
    private String username;

    @Schema(description = "Canal asignado / audiencia del token (WEB, MOVIL, ATM)", example = "MOVIL")
    private String canal;

    @Schema(description = "Rol de seguridad asignado en Spring Security", example = "ROLE_MOVIL")
    private String rol;

    @Schema(description = "Tiempo de expiración del token en milisegundos", example = "3600000")
    private long expiracionMilisegundos;
}
