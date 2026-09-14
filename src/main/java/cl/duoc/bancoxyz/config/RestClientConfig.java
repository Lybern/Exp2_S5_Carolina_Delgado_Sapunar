package cl.duoc.bancoxyz.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

// =========================================================================
// CONFIGURACIÓN DE REST CLIENT (SPRING 6+ / BOOT 4+):
// Define el cliente HTTP/HTTPS síncrono moderno para la comunicación entre el
// BFF y los microservicios del Core Bancario, garantizando canales cifrados.
// =========================================================================
@Configuration
public class RestClientConfig {

    @Value("${banco.core.base-url:https://localhost:8443}")
    private String coreBaseUrl;

    @Bean
    public RestClient coreBancarioRestClient() {
        return RestClient.builder()
                .baseUrl(coreBaseUrl)
                .build();
    }
}
