package cl.duoc.bancoxyz.security;

import cl.duoc.bancoxyz.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

// =========================================================================
// UTILIDAD DE SEGURIDAD JWT (JJWT 0.12.6):
// Encapsula la generación, firma criptográfica HMAC-SHA256 y validación de:
// 1. Tokens de Cliente (Usuario final autenticado por canal: WEB, MOVIL, ATM).
// 2. Tokens Delegados de Servicio (Service Token para comunicación BFF <-> Microservicios).
// =========================================================================
@Component
public class JwtTokenUtil {

    private final JwtProperties jwtProperties;
    private SecretKey secretKey;
    private SecretKey serviceSecretKey;

    public JwtTokenUtil(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    /**
     * Inicializa las claves criptográficas HMAC-SHA256 a partir de las propiedades inyectadas.
     * Requiere que cada secreto tenga al menos 256 bits (32 bytes).
     */
    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
        this.serviceSecretKey = Keys.hmacShaKeyFor(jwtProperties.getServiceSecret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Genera un Token JWT de Usuario para un canal específico.
     * Incluye audiencia (aud) y rol para prevenir ataques de token cruzado entre canales.
     */
    public String generateToken(UserDetails userDetails, String canal) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getExpiration());

        String rol = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_USER");

        return Jwts.builder()
                .issuer("banco-xyz-bff")
                .subject(userDetails.getUsername())
                .audience().add(canal.toUpperCase()).and()
                .claim("rol", rol)
                .claim("email", userDetails.getUsername() + "@bancoxyz.cl")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Genera un Token Delegado de Servicio (Service Token).
     * Se utiliza cuando el BFF se comunica con los microservicios internos del Core Bancario.
     */
    public String generateServiceToken(UserDetails userDetails, String issuerCanal) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getServiceExpiration());

        String rol = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_USER");

        return Jwts.builder()
                .issuer(issuerCanal.toLowerCase())
                .subject(userDetails.getUsername())
                .audience().add("core-bancario").and()
                .claim("rol", rol)
                .claim("email", userDetails.getUsername() + "@bancoxyz.cl")
                .claim("nombre", userDetails.getUsername())
                .claim("tipo", "SERVICE_TOKEN")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(serviceSecretKey)
                .compact();
    }

    /**
     * Extrae el nombre de usuario (subject) del token.
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * Extrae el rol asignado dentro de los claims del token.
     */
    public String getRoleFromToken(String token) {
        return getClaimsFromToken(token).get("rol", String.class);
    }

    /**
     * Valida que el token pertenezca al usuario y no haya expirado.
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /**
     * Valida que el token corresponda al canal/audiencia esperado (ej: "WEB", "MOVIL", "ATM").
     */
    public boolean validateTokenForAudience(String token, UserDetails userDetails, String expectedAudience) {
        if (!validateToken(token, userDetails)) {
            return false;
        }
        Claims claims = getClaimsFromToken(token);
        return claims.getAudience() != null && claims.getAudience().contains(expectedAudience.toUpperCase());
    }

    /**
     * Comprueba si el token ha superado su fecha límite de validez.
     */
    public boolean isTokenExpired(String token) {
        return getExpirationDateFromToken(token).before(new Date());
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Parsea y valida tokens de servicio con la clave compartida interna.
     */
    public Claims parseServiceToken(String token) {
        return Jwts.parser()
                .verifyWith(serviceSecretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
