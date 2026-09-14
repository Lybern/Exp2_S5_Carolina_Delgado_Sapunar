package cl.duoc.bancoxyz.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

// =========================================================================
// FILTRO INTERCEPTOR DE AUTENTICACIÓN JWT (OncePerRequestFilter):
// Se ejecuta una sola vez por cada petición HTTP entrante.
// Responsabilidades:
// 1. Extraer y parsear el encabezado 'Authorization: Bearer <token>'.
// 2. Verificar la validez de la firma HMAC-SHA256 y la fecha de expiración.
// 3. Validar que la audiencia del token coincida con el canal solicitado (Web, Móvil, ATM).
// 4. Inyectar la identidad y roles en el SecurityContextHolder de Spring Security.
// =========================================================================
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;

    public JwtAuthenticationFilter(JwtTokenUtil jwtTokenUtil) {
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        // Verificar si la petición contiene el header Bearer
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7).trim();

            if (!token.isEmpty() && token.contains(".")) {
                try {
                    String username = jwtTokenUtil.getUsernameFromToken(token);

                    // Si hay un usuario válido en el token y no ha sido autenticado aún en este hilo
                    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        String rol = jwtTokenUtil.getRoleFromToken(token);
                        List<SimpleGrantedAuthority> authorities = (rol != null && !rol.isEmpty())
                                ? List.of(new SimpleGrantedAuthority(rol))
                                : Collections.emptyList();

                        UserDetails userDetails = new User(username, "", authorities);

                        // Determinar la audiencia esperada según el canal de la URL
                        String uri = request.getRequestURI();
                        String canalEsperado = null;
                        if (uri.startsWith("/api/v1/web")) {
                            canalEsperado = "WEB";
                        } else if (uri.startsWith("/api/v1/movil") || uri.startsWith("/api/v1/mobile")) {
                            canalEsperado = "MOVIL";
                        } else if (uri.startsWith("/api/v1/cajero") || uri.startsWith("/api/v1/atm")) {
                            canalEsperado = "ATM";
                        }

                        // Validación: Firma válida, no expirado y audiencia coherente con el canal
                        boolean esValido = (canalEsperado != null)
                                ? jwtTokenUtil.validateTokenForAudience(token, userDetails, canalEsperado)
                                : jwtTokenUtil.validateToken(token, userDetails);

                        if (esValido) {
                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                            // Establecer la autenticación en el contexto de seguridad de Spring
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                        }
                    }
                } catch (Exception ex) {
                    // Si el token es inválido, está expirado o fue alterado, se limpia el contexto
                    SecurityContextHolder.clearContext();
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
