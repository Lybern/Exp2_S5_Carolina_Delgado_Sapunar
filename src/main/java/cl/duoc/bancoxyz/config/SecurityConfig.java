package cl.duoc.bancoxyz.config;

import cl.duoc.bancoxyz.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

// =========================================================================
// CONFIGURACIÓN CENTRAL DE SPRING SECURITY 6+ / BOOT 4+:
// Define la cadena de filtros de seguridad (SecurityFilterChain), la política
// STATELESS para arquitectura REST basada en JWT, los usuarios por canal y CORS.
// =========================================================================
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * Define las reglas de autorización por canal y la política de sesión sin estado (STATELESS).
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Deshabilitar CSRF ya que la API REST utiliza tokens JWT sin estado
                .csrf(csrf -> csrf.disable())
                // Habilitar configuración CORS para aplicaciones frontend externas
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        // Endpoints públicos: Autenticación / Login y Documentación OpenAPI Swagger
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/swagger-resources/**", "/webjars/**").permitAll()

                        // Protección diferenciada por canal (Previene ataques de acceso cruzado):
                        // 1. Canal Web: Requiere rol ROLE_WEB o ROLE_ADMIN
                        .requestMatchers("/api/v1/web/**").hasAnyRole("WEB", "ADMIN")

                        // 2. Canal Móvil: Requiere rol ROLE_MOVIL o ROLE_CLIENTE
                        .requestMatchers("/api/v1/movil/**", "/api/v1/mobile/**").hasAnyRole("MOVIL", "CLIENTE", "ADMIN")

                        // 3. Canal Cajeros Automáticos (ATM): Requiere rol ROLE_ATM u operador
                        .requestMatchers("/api/v1/cajero/**", "/api/v1/atm/**").hasAnyRole("ATM", "ADMIN")

                        // Cualquier otra petición debe estar autenticada
                        .anyRequest().authenticated()
                )
                // Política de sesión sin estado (Stateless) requerida por el estándar JWT
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Inserción del filtro JWT antes del filtro de usuario/contraseña estándar
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Usuarios en memoria diferenciados con credenciales y roles específicos para cada canal.
     * Las contraseñas están encriptadas con algoritmo BCrypt (fuerza 10).
     */
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        // Usuario exclusivo para Portal Web
        UserDetails userWeb = User.withUsername("usuario_web")
                .password(passwordEncoder.encode("web123"))
                .roles("WEB")
                .build();

        // Usuario exclusivo para App Móvil
        UserDetails userMovil = User.withUsername("usuario_movil")
                .password(passwordEncoder.encode("movil123"))
                .roles("MOVIL", "CLIENTE")
                .build();

        // Terminal / Operador exclusivo para Cajero Automático (ATM)
        UserDetails userAtm = User.withUsername("operador_atm")
                .password(passwordEncoder.encode("atm123"))
                .roles("ATM")
                .build();

        // Administrador general con acceso a todos los canales
        UserDetails admin = User.withUsername("admin_general")
                .password(passwordEncoder.encode("admin123"))
                .roles("ADMIN", "WEB", "MOVIL", "ATM")
                .build();

        return new InMemoryUserDetailsManager(userWeb, userMovil, userAtm, admin);
    }

    /**
     * Encriptador estándar de contraseñas mediante algoritmo BCrypt.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Expone el AuthenticationManager de Spring Security para el controlador de Login.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Configuración de CORS para permitir solicitudes desde navegadores web y dispositivos móviles.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
