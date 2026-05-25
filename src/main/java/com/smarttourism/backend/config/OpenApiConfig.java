package com.smarttourism.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger configuration for the Smart Tourism Santander API.
 *
 * <p>Configures the global API metadata (title, version, description) and
 * registers the JWT Bearer authentication scheme so that all protected
 * endpoints display the lock icon in Swagger UI.
 *
 * <p>Validates: Requirements 1.4, 1.5
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH = "bearerAuth";

    /**
     * Defines the global OpenAPI bean with title, version, description,
     * contact info, and the JWT Bearer security scheme.
     *
     * @return the configured {@link OpenAPI} instance
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Smart Tourism Santander API")
                        .version("1.0.0")
                        .description(
                                "API REST del backend de la plataforma **Turismo Inteligente Santander**. " +
                                "Gestiona el ciclo de vida completo de las experiencias turísticas: " +
                                "registro y autenticación de usuarios, catálogo de experiencias, " +
                                "reservas con control de cupos, simulación de pagos, " +
                                "calificaciones y panel de administración.\n\n" +
                                "**Autenticación**: todos los endpoints protegidos requieren un token JWT " +
                                "en el header `Authorization: Bearer <token>`. " +
                                "Obtén el token mediante `POST /api/v1/auth/login`."
                        )
                        .contact(new Contact()
                                .name("Equipo Smart Tourism Santander")
                                .email("dev@smarttourism.com")))
                // Apply bearerAuth globally to all operations
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH,
                                new SecurityScheme()
                                        .name(BEARER_AUTH)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Token JWT obtenido mediante POST /api/v1/auth/login")));
    }
}
