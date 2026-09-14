package cl.duoc.bancoxyz.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// =========================================================================
// DTO DE SOLICITUD DE LOGIN (AUTENTICACIÓN JWT):
// Encapsula las credenciales enviadas por el usuario o terminal bancaria.
// Incluye el canal opcional ("WEB", "MOVIL", "ATM") para emitir el token adecuado.
// =========================================================================
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Solicitud de credenciales para obtención de token JWT por canal")
public class LoginRequestDto {

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Schema(description = "Nombre de usuario (ej: usuario_web, usuario_movil, operador_atm, admin_general)", example = "usuario_movil")
    private String username;

    @NotBlank(message = "La contraseña es obligatoria")
    @Schema(description = "Contraseña en texto plano para validación BCrypt", example = "movil123")
    private String password;

    @Schema(description = "Canal opcional para auditar la emisión del token: WEB, MOVIL, ATM (si se omite, se infiere del rol del usuario)", example = "MOVIL")
    private String canal;
}
