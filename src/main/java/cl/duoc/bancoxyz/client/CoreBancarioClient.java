package cl.duoc.bancoxyz.client;

import cl.duoc.bancoxyz.model.Cuenta;
import cl.duoc.bancoxyz.security.JwtTokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

// =========================================================================
// CLIENTE SEGURO DE MICROSERVICIO (DELEGATED SERVICE TOKEN PATTERN):
// Implementa el patrón de propagación de identidad y tokens delegados entre
// la capa BFF (Backend for Frontend) y los microservicios internos del Core Bancario.
//
// 1. Cifrado en tránsito: Comunicación sobre canal HTTPS/TLS.
// 2. Token de Servicio: El BFF emite un JWT firmado con 'jwt.service-secret'
//    con audiencia 'core-bancario' y emisor del BFF correspondiente.
// 3. Delegación de contexto: Mantiene la identidad del usuario autenticado sin
//    exponer secretos externos hacia la red interna.
// =========================================================================
@Component
public class CoreBancarioClient {

    private static final Logger log = LoggerFactory.getLogger(CoreBancarioClient.class);

    private final RestClient coreBancarioRestClient;
    private final JwtTokenUtil jwtTokenUtil;

    public CoreBancarioClient(RestClient coreBancarioRestClient, JwtTokenUtil jwtTokenUtil) {
        this.coreBancarioRestClient = coreBancarioRestClient;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    /**
     * Genera un Token Delegado de Servicio para el usuario actualmente autenticado en el contexto.
     * @param emisorCanal Identificador del BFF (ej: "bff-web", "bff-movil", "bff-cajero")
     * @return Token JWT firmado con clave compartida de servicio
     */
    public String generarServiceToken(String emisorCanal) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails userDetails)) {
            log.warn("[CORE-CLIENT] No se detectó usuario autenticado en el contexto de seguridad");
            throw new IllegalStateException("No hay usuario autenticado en SecurityContext para generar el Service Token");
        }

        String serviceToken = jwtTokenUtil.generateServiceToken(userDetails, emisorCanal);
        log.info("[CORE-CLIENT] Service Token generado exitosamente para BFF '{}' en nombre del usuario '{}'",
                emisorCanal, userDetails.getUsername());
        return serviceToken;
    }

    /**
     * Ejemplo de consulta cifrada y autenticada con Service Token hacia microservicio Core Bancario.
     */
    public Cuenta consultarCuentaRemota(String numeroCuenta, String emisorBff) {
        String serviceToken = generarServiceToken(emisorBff);

        log.info("[CORE-CLIENT] Enviando petición HTTPS autenticada con Service Token hacia /api/core/cuentas/{}", numeroCuenta);

        return coreBancarioRestClient.get()
                .uri("/api/core/cuentas/{numeroCuenta}", numeroCuenta)
                .header("Authorization", "Bearer " + serviceToken)
                .retrieve()
                .body(Cuenta.class);
    }
}
