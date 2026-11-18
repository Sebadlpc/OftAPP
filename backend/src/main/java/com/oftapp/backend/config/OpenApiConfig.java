package com.oftapp.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuración OpenAPI / Swagger para documentación interactiva de endpoints.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("OftAPP - REST API Backend")
                        .version("1.0.0")
                        .description("API REST para el sistema clínico de trazabilidad de exámenes oftalmológicos OftAPP.")
                        .contact(new Contact()
                                .name("Equipo de Desarrollo OftAPP")
                                .email("soporte@oftapp.cl"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Servidor Local"),
                        new Server().url("http://10.0.2.2:8080").description("Emulador Android (Loopback)")
                ));
    }
}
