package cl.duoc.bancoxyz.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

// =========================================================================
// CONFIGURACIÓN DE DOCUMENTACIÓN OPENAPI / SWAGGER 3:
// Configura la información general de la API y el esquema de seguridad Bearer JWT
// para permitir pruebas interactivas autenticadas desde Swagger UI.
// =========================================================================
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "BearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Banco XYZ - API Backend for Frontend (BFF)")
                        .version("1.0.0")
                        .description("Arquitectura Backend for Frontend (BFF) para el Banco XYZ con cifrado HTTPS/TLS, " +
                                "autenticación JWT diferenciada por canal (WEB, MOVIL, ATM) y Tokens Delegados de Servicio.")
                        .contact(new Contact()
                                .name("Carolina Delgado - Desarrollo Backend III")
                                .url("https://github.com/Lybern/Exp2_S5_Carolina_Delgado_Sapunar")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Ingrese el token JWT obtenido en /api/auth/login")))
                .tags(List.of(
                        new Tag().name("Autenticación y Seguridad").description("Endpoints para emisión de tokens JWT y Service Tokens"),
                        new Tag().name("BFF Móvil").description("Endpoints para aplicación móvil (respuestas ligeras y bajo consumo de red)"),
                        new Tag().name("BFF Web").description("Endpoints para portal web (datos completos, historial y dashboard)"),
                        new Tag().name("BFF Cajero Automático").description("Endpoints para cajeros ATM (operaciones críticas de saldo y retiros)")
                ));
    }
}

