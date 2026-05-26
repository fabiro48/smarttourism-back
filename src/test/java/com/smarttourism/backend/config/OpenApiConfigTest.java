package com.smarttourism.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link OpenApiConfig}.
 *
 * <p>Validates: Requirements 1.4, 1.5
 */
@DisplayName("OpenApiConfig")
class OpenApiConfigTest {

    private OpenAPI openApi;

    @BeforeEach
    void setUp() {
        openApi = new OpenApiConfig().customOpenAPI();
    }

    @Test
    @DisplayName("should set API title to 'Smart Tourism Santander API'")
    void shouldHaveCorrectTitle() {
        assertThat(openApi.getInfo().getTitle())
                .isEqualTo("Smart Tourism Santander API");
    }

    @Test
    @DisplayName("should set API version to '1.0.0'")
    void shouldHaveCorrectVersion() {
        assertThat(openApi.getInfo().getVersion())
                .isEqualTo("1.0.0");
    }

    @Test
    @DisplayName("should register 'bearerAuth' security scheme in components")
    void shouldContainBearerAuthSecurityScheme() {
        assertThat(openApi.getComponents().getSecuritySchemes())
                .containsKey("bearerAuth");
    }

    @Test
    @DisplayName("'bearerAuth' scheme should be HTTP bearer type with JWT format")
    void bearerAuthSchemeShouldBeHttpBearerJwt() {
        var scheme = openApi.getComponents().getSecuritySchemes().get("bearerAuth");
        assertThat(scheme.getType())
                .isEqualTo(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP);
        assertThat(scheme.getScheme()).isEqualTo("bearer");
        assertThat(scheme.getBearerFormat()).isEqualTo("JWT");
    }
}
