package cl.duoc.bancoxyz;

import static org.assertj.core.api.Assertions.assertThat;

import cl.duoc.bancoxyz.auth.dto.LoginRequestDto;
import cl.duoc.bancoxyz.auth.dto.LoginResponseDto;
import cl.duoc.bancoxyz.auth.dto.ServiceTokenRequestDto;
import cl.duoc.bancoxyz.auth.dto.ServiceTokenResponseDto;
import cl.duoc.bancoxyz.controller.AuthController;
import cl.duoc.bancoxyz.security.JwtAuthenticationFilter;
import cl.duoc.bancoxyz.security.JwtTokenUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

// =========================================================================
// PRUEBAS DE INTEGRACIÓN DE AUTENTICACIÓN Y SEGURIDAD JWT (BANCO XYZ):
// Valida la emisión de tokens por canal, tokens de servicio, y filtros de seguridad.
// =========================================================================
@SpringBootTest
class AuthControllerIntegrationTest {

    @Autowired
    private AuthController authController;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("Login exitoso para usuario móvil emite JWT con audiencia MOVIL y rol ROLE_MOVIL")
    void loginMovilDebeEmitirTokenValido() {
        LoginRequestDto request = new LoginRequestDto("usuario_movil", "movil123", "MOVIL");

        ResponseEntity<?> response = authController.login(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(LoginResponseDto.class);

        LoginResponseDto body = (LoginResponseDto) response.getBody();
        assertThat(body.getToken()).isNotBlank();
        assertThat(body.getUsername()).isEqualTo("usuario_movil");
        assertThat(body.getCanal()).isEqualTo("MOVIL");
        assertThat(body.getRol()).isEqualTo("ROLE_MOVIL");

        // Validar que el token decodificado tenga la audiencia correcta
        UserDetails userDetails = userDetailsService.loadUserByUsername("usuario_movil");
        assertThat(jwtTokenUtil.validateTokenForAudience(body.getToken(), userDetails, "MOVIL")).isTrue();
    }

    @Test
    @DisplayName("Login exitoso para usuario web emite JWT con audiencia WEB")
    void loginWebDebeEmitirTokenValido() {
        LoginRequestDto request = new LoginRequestDto("usuario_web", "web123", "WEB");

        ResponseEntity<?> response = authController.login(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        LoginResponseDto body = (LoginResponseDto) response.getBody();
        assertThat(body.getToken()).isNotBlank();
        assertThat(body.getUsername()).isEqualTo("usuario_web");
        assertThat(body.getCanal()).isEqualTo("WEB");

        UserDetails userDetails = userDetailsService.loadUserByUsername("usuario_web");
        assertThat(jwtTokenUtil.validateTokenForAudience(body.getToken(), userDetails, "WEB")).isTrue();
    }

    @Test
    @DisplayName("Login con credenciales inválidas debe responder HTTP 401 Unauthorized")
    void loginConPasswordIncorrectoDebeFallar() {
        LoginRequestDto request = new LoginRequestDto("usuario_movil", "password_incorrecto", "MOVIL");

        ResponseEntity<?> response = authController.login(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Emisión de Token Delegado de Servicio para comunicación BFF <-> Microservicios")
    void emisionServiceTokenDebeGenerarTokenParaCoreBancario() {
        ServiceTokenRequestDto request = new ServiceTokenRequestDto("bff-movil", "usuario_movil");

        ResponseEntity<?> response = authController.generarServiceToken(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(ServiceTokenResponseDto.class);

        ServiceTokenResponseDto body = (ServiceTokenResponseDto) response.getBody();
        assertThat(body.getServiceToken()).isNotBlank();
        assertThat(body.getEmisorBff()).isEqualTo("bff-movil");
        assertThat(body.getAudiencia()).isEqualTo("core-bancario");

        // Validar que el serviceToken se pueda decodificar con la clave secreta de servicio
        Claims claims = jwtTokenUtil.parseServiceToken(body.getServiceToken());
        assertThat(claims.getSubject()).isEqualTo("usuario_movil");
        assertThat(claims.getAudience()).contains("core-bancario");
        assertThat(claims.getIssuer()).isEqualTo("bff-movil");
        assertThat(claims.get("tipo", String.class)).isEqualTo("SERVICE_TOKEN");
    }

    @Test
    @DisplayName("Token malformado en cabecera Authorization no debe autenticar el contexto")
    void tokenMalformadoDebeSerRechazadoPorFiltro() throws Exception {
        SecurityContextHolder.clearContext();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-invalido-o-corrupto");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = (req, res) -> {};

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
