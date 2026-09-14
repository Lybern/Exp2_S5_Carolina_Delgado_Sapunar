package cl.duoc.bancoxyz.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

// =========================================================================
// CONFIGURACIÓN DE PROPIEDADES JWT (@ConfigurationProperties):
// Mapea automáticamente los valores definidos con el prefijo "jwt.*" en
// application.properties hacia variables fuertemente tipadas en Java.
// Resuelve las advertencias del IDE y centraliza los parámetros de seguridad.
// =========================================================================
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * Clave secreta para firmar y validar tokens de usuario/cliente (HMAC-SHA256).
     */
    private String secret;

    /**
     * Tiempo de expiración del token de usuario en milisegundos (ej: 3.600.000 ms = 1 hora).
     */
    private Long expiration;

    /**
     * Clave secreta compartida privada entre BFFs y microservicios para Service Tokens.
     */
    private String serviceSecret;

    /**
     * Tiempo de expiración del Token Delegado de Servicio en milisegundos (ej: 3.000.000 ms = 50 min).
     */
    private Long serviceExpiration;

    public JwtProperties() {
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Long getExpiration() {
        return expiration;
    }

    public void setExpiration(Long expiration) {
        this.expiration = expiration;
    }

    public String getServiceSecret() {
        return serviceSecret;
    }

    public void setServiceSecret(String serviceSecret) {
        this.serviceSecret = serviceSecret;
    }

    public Long getServiceExpiration() {
        return serviceExpiration;
    }

    public void setServiceExpiration(Long serviceExpiration) {
        this.serviceExpiration = serviceExpiration;
    }
}
