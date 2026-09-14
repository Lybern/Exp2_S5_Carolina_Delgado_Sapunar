package cl.duoc.bancoxyz;

import static org.assertj.core.api.Assertions.assertThat;

import cl.duoc.bancoxyz.bff.web.controller.WebController;
import cl.duoc.bancoxyz.bff.web.dto.DetalleCuentaWebDto;
import cl.duoc.bancoxyz.security.JwtAuthenticationFilter;
import cl.duoc.bancoxyz.security.JwtTokenUtil;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

// =========================================================================
// PRUEBAS DE SEGURIDAD ESPECÍFICAS: CLIENTE WEB <-> WEB BFF
// Valida la protección de canal, aislamiento de audiencia y prevención
// de ataques de sustitución de tokens (Cross-Channel Token Misuse).
// =========================================================================
@SpringBootTest
class WebSecurityIntegrationTest {

    @Autowired
    private WebController webController;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private UserDetailsService userDetailsService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Cliente Web con Token JWT legítimo (aud=WEB) es autenticado correctamente en Web BFF")
    void accesoConTokenWebValidoDebeSerAceptado() throws Exception {
        UserDetails userDetails = userDetailsService.loadUserByUsername("usuario_web");
        String tokenWeb = jwtTokenUtil.generateToken(userDetails, "WEB");

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/web/cuentas/101");
        request.addHeader("Authorization", "Bearer " + tokenWeb);
        request.addHeader("X-Web-Session", "SESION-WEB-CHROME-12345");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = (req, res) -> {};

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        // Verifica que la autenticación fue aceptada en el contexto de seguridad
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("usuario_web");

        // Invoca el controlador web y valida respuesta completa de datos
        ResponseEntity<DetalleCuentaWebDto> respuesta = webController.obtenerDetalleCuenta(101L);
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).isNotNull();
        assertThat(respuesta.getBody().getCuentaId()).isEqualTo(101L);
    }

    @Test
    @DisplayName("Protección Cross-Channel: Token de App Móvil (aud=MOVIL) es RECHAZADO al intentar acceder a Web BFF")
    void tokenMovilHaciaWebBffDebeSerRechazado() throws Exception {
        UserDetails userDetails = userDetailsService.loadUserByUsername("usuario_movil");
        // Token generado para canal MOVIL
        String tokenMovil = jwtTokenUtil.generateToken(userDetails, "MOVIL");

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/web/cuentas/101");
        request.addHeader("Authorization", "Bearer " + tokenMovil);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = (req, res) -> {};

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        // El filtro debe rechazar el token porque la audiencia (MOVIL) no coincide con el canal WEB solicitado
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Protección Cross-Channel: Token de Cajero ATM (aud=ATM) es RECHAZADO al intentar acceder a Web BFF")
    void tokenAtmHaciaWebBffDebeSerRechazado() throws Exception {
        UserDetails userDetails = userDetailsService.loadUserByUsername("operador_atm");
        // Token generado para canal ATM
        String tokenAtm = jwtTokenUtil.generateToken(userDetails, "ATM");

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/web/dashboard");
        request.addHeader("Authorization", "Bearer " + tokenAtm);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = (req, res) -> {};

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        // El filtro debe rechazar el token porque la audiencia (ATM) no coincide con el canal WEB solicitado
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
