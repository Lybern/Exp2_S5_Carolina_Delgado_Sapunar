package cl.duoc.bancoxyz;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cl.duoc.bancoxyz.client.CoreBancarioClient;
import cl.duoc.bancoxyz.security.JwtTokenUtil;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

// =========================================================================
// PRUEBAS DE CLIENTE DE MICROSERVICIO (DELEGATED SERVICE TOKEN):
// Valida la correcta generación y firma de tokens de servicio para comunicación inter-servicios.
// =========================================================================
@SpringBootTest
class CoreBancarioClientTest {

    @Autowired
    private CoreBancarioClient coreBancarioClient;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Generación de Service Token autenticado con usuario en SecurityContextHolder")
    void generarServiceTokenConUsuarioAutenticado() {
        UserDetails userDetails = userDetailsService.loadUserByUsername("usuario_movil");
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        String serviceToken = coreBancarioClient.generarServiceToken("bff-movil");

        assertThat(serviceToken).isNotBlank();

        // Validar con la clave secreta de servicio
        Claims claims = jwtTokenUtil.parseServiceToken(serviceToken);
        assertThat(claims.getSubject()).isEqualTo("usuario_movil");
        assertThat(claims.getIssuer()).isEqualTo("bff-movil");
        assertThat(claims.getAudience()).contains("core-bancario");
        assertThat(claims.get("tipo", String.class)).isEqualTo("SERVICE_TOKEN");
    }

    @Test
    @DisplayName("Error al generar Service Token si no hay usuario autenticado en SecurityContextHolder")
    void generarServiceTokenSinUsuarioLanzaExcepcion() {
        SecurityContextHolder.clearContext();

        assertThatThrownBy(() -> coreBancarioClient.generarServiceToken("bff-web"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No hay usuario autenticado");
    }
}
