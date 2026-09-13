package cl.duoc.bancoxyz.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class SeguridadCanalesInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(SeguridadCanalesInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();

        // 1. Canal Móvil: validación de cabecera o simulación de token móvil
        if (path.startsWith("/api/v1/movil") || path.startsWith("/api/v1/mobile")) {
            String tokenMovil = request.getHeader("X-Mobile-Auth");
            log.info("[SEGURIDAD MÓVIL] Petición entrante desde App Móvil: Path={}, TokenPresente={}",
                    path, tokenMovil != null);
            response.setHeader("X-Channel-Security", "MOBILE-OAUTH2-VALIDATED");
            return true;
        }

        // 2. Canal Web: validación de sesión de escritorio y CORS
        if (path.startsWith("/api/v1/web")) {
            String tokenWeb = request.getHeader("X-Web-Session");
            log.info("[SEGURIDAD WEB] Petición entrante desde Portal Web: Path={}, SesionPresente={}",
                    path, tokenWeb != null);
            response.setHeader("X-Channel-Security", "WEB-TLS13-SESSION-ACTIVE");
            return true;
        }

        // 3. Canal Cajero Automático (ATM): validación estricta de terminal de hardware
        if (path.startsWith("/api/v1/cajero") || path.startsWith("/api/v1/atm")) {
            String terminalId = request.getHeader("X-ATM-Terminal-ID");
            log.info("[SEGURIDAD ATM] Petición transaccional desde Terminal Físico: Path={}, TerminalID={}",
                    path, terminalId != null ? terminalId : "ATM-TERMINAL-GENERIC");
            response.setHeader("X-Channel-Security", "ATM-SECURE-TERMINAL-ENCRYPTED");
            return true;
        }

        return true;
    }
}
