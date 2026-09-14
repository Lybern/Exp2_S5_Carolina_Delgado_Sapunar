package cl.duoc.bancoxyz.controller;

import cl.duoc.bancoxyz.auth.dto.LoginRequestDto;
import cl.duoc.bancoxyz.auth.dto.LoginResponseDto;
import cl.duoc.bancoxyz.auth.dto.ServiceTokenRequestDto;
import cl.duoc.bancoxyz.auth.dto.ServiceTokenResponseDto;
import cl.duoc.bancoxyz.config.JwtProperties;
import cl.duoc.bancoxyz.security.JwtTokenUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

// =========================================================================
// CONTROLADOR DE AUTENTICACIÓN CENTRALIZADA (AUTH CONTROLLER):
// Expone los endpoints de seguridad requeridos para:
// 1. Emisión de Tokens JWT de Usuario final diferenciados por canal (WEB, MOVIL, ATM).
// 2. Emisión de Tokens Delegados de Servicio (Service Tokens) para comunicación segura BFF <-> Microservicios.
// =========================================================================
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación y Seguridad", description = "Endpoints para emisión de tokens JWT de clientes y Tokens Delegados de Servicio")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final JwtProperties jwtProperties;
    private final UserDetailsService userDetailsService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtTokenUtil jwtTokenUtil,
                          JwtProperties jwtProperties,
                          UserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.jwtProperties = jwtProperties;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Endpoint de inicio de sesión para obtener un token JWT firmado.
     * Valida credenciales contra Spring Security (BCrypt) y vincula el token al canal correspondiente.
     */
    @Operation(summary = "Autenticación de usuario / terminal y emisión de Token JWT",
            description = "Valida las credenciales con BCrypt y emite un JWT con audiencia (aud) específica para WEB, MOVIL o ATM.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Autenticación exitosa y token JWT generado",
                    content = @Content(schema = @Schema(implementation = LoginResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Parámetros de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Credenciales incorrectas o canal no autorizado")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto loginRequest) {
        try {
            // 1. Autenticar credenciales con Spring Security
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // 2. Determinar el canal adecuado (validando o infiriendo según los roles del usuario)
            String canalAsignado = determinarCanal(userDetails, loginRequest.getCanal());

            // 3. Extraer rol principal
            String rol = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst()
                    .orElse("ROLE_USER");

            // 4. Generar Token JWT firmado con HMAC-SHA256 y audiencia por canal
            String token = jwtTokenUtil.generateToken(userDetails, canalAsignado);

            log.info("[AUTH] Token JWT emitido exitosamente para usuario '{}' en canal '{}' con rol '{}'",
                    userDetails.getUsername(), canalAsignado, rol);

            LoginResponseDto response = LoginResponseDto.builder()
                    .token(token)
                    .tipoToken("Bearer")
                    .username(userDetails.getUsername())
                    .canal(canalAsignado)
                    .rol(rol)
                    .expiracionMilisegundos(jwtProperties.getExpiration())
                    .build();

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            log.warn("[AUTH-ERROR] Intento fallido de autenticación para usuario: {}", loginRequest.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "error", "UNAUTHORIZED",
                            "mensaje", "Credenciales inválidas o canal no autorizado para este usuario."
                    ));
        }
    }

    /**
     * Endpoint para generar un Token Delegado de Servicio (Service Token).
     * Permite a los módulos BFF autenticarse de forma segura y cifrada ante microservicios de backend.
     */
    @Operation(summary = "Emisión de Token Delegado de Servicio (Service Token)",
            description = "Genera un JWT firmado con la clave compartida interna para comunicación segura BFF <-> Microservicios.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token de Servicio generado exitosamente",
                    content = @Content(schema = @Schema(implementation = ServiceTokenResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Datos de solicitud incompletos o inválidos"),
            @ApiResponse(responseCode = "404", description = "Usuario delegado no encontrado")
    })
    @PostMapping("/service-token")
    public ResponseEntity<?> generarServiceToken(@Valid @RequestBody ServiceTokenRequestDto request) {
        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());

            String serviceToken = jwtTokenUtil.generateServiceToken(userDetails, request.getEmisorBff());

            log.info("[SERVICE-TOKEN] Token de servicio emitido para BFF '{}' en nombre de '{}'",
                    request.getEmisorBff(), request.getUsername());

            ServiceTokenResponseDto response = ServiceTokenResponseDto.builder()
                    .serviceToken(serviceToken)
                    .tipoToken("Bearer")
                    .emisorBff(request.getEmisorBff())
                    .audiencia("core-bancario")
                    .username(userDetails.getUsername())
                    .expiracionMilisegundos(jwtProperties.getServiceExpiration())
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("[SERVICE-TOKEN-ERROR] Error generando token de servicio: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "error", "NOT_FOUND",
                            "mensaje", "No se encontró el usuario en contexto para emitir el token de servicio: " + request.getUsername()
                    ));
        }
    }

    /**
     * Determina el canal válido para la petición. Si se especifica, valida coherencia de roles;
     * de lo contrario, infiere el canal según los privilegios del usuario.
     */
    private String determinarCanal(UserDetails userDetails, String canalSolicitado) {
        boolean esAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (canalSolicitado != null && !canalSolicitado.trim().isEmpty()) {
            String canalNorm = canalSolicitado.trim().toUpperCase();
            if (esAdmin) {
                return canalNorm;
            }
            boolean autorizado = userDetails.getAuthorities().stream().anyMatch(a -> {
                String auth = a.getAuthority();
                return switch (canalNorm) {
                    case "WEB" -> auth.equals("ROLE_WEB");
                    case "MOVIL" -> auth.equals("ROLE_MOVIL") || auth.equals("ROLE_CLIENTE");
                    case "ATM" -> auth.equals("ROLE_ATM");
                    default -> false;
                };
            });
            if (!autorizado) {
                throw new BadCredentialsException("El usuario no tiene permisos para el canal: " + canalSolicitado);
            }
            return canalNorm;
        }

        // Inferir por rol por defecto
        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ATM"))) {
            return "ATM";
        }
        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_MOVIL") || a.getAuthority().equals("ROLE_CLIENTE"))) {
            return "MOVIL";
        }
        return "WEB";
    }
}
