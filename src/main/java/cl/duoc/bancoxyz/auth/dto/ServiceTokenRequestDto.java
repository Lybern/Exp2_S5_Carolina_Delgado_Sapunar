package cl.duoc.bancoxyz.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// =========================================================================
// DTO DE SOLICITUD DE TOKEN DELEGADO DE SERVICIO:
// Utilizado por las capas BFF para solicitar un Service Token que autentique
// las peticiones hacia microservicios del Core Bancario (comunicación cifrada BFF <-> Microservicios).
// =========================================================================
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Solicitud para emisión de Token Delegado de Servicio")
public class ServiceTokenRequestDto {

    @NotBlank(message = "El identificador del BFF emisor es obligatorio")
    @Schema(description = "Identificador del canal BFF que solicita delegación (ej: bff-web, bff-movil, bff-cajero)", example = "bff-movil")
    private String emisorBff;

    @NotBlank(message = "El usuario en contexto es obligatorio")
    @Schema(description = "Usuario en nombre del cual se realiza la delegación transaccional", example = "usuario_movil")
    private String username;
}
