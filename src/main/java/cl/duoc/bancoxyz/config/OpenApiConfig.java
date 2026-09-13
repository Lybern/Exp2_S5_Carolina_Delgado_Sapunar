package cl.duoc.bancoxyz.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Banco XYZ - API Backend for Frontend (BFF)")
                        .version("1.0.0")
                        .description("Arquitectura Backend for Frontend (BFF) para el Banco XYZ. " +
                                "Provee APIs personalizadas y optimizadas para clientes Web, Móvil y Cajeros Automáticos (ATM).")
                        .contact(new Contact()
                                .name("Grupo 3 - Desarrollo Backend III")
                                .url("https://github.com/Lybern/Exp2_S4_Grupo3")))
                .tags(List.of(
                        new Tag().name("BFF Móvil").description("Endpoints para aplicación móvil (respuestas ligeras y bajo consumo de red)"),
                        new Tag().name("BFF Web").description("Endpoints para portal web (datos completos, historial y dashboard)"),
                        new Tag().name("BFF Cajero Automático").description("Endpoints para cajeros ATM (operaciones críticas de saldo y retiros)")
                ));
    }
}
