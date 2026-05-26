package com.smarttourism.backend.docs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test that verifies the OpenAPI specification returned by
 * {@code GET /v3/api-docs} is well-formed and contains the expected metadata.
 *
 * <p>Checks that the spec includes the required top-level fields
 * ({@code openapi}, {@code info}, {@code paths}, {@code components}),
 * that {@code info.title} and {@code info.version} match the configured values,
 * that {@code paths} is non-empty, and that {@code components.securitySchemes}
 * contains the {@code bearerAuth} scheme.
 *
 * <p><b>Validates: Requirements 1.3, 1.4</b>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
class OpenApiSpecTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate testRestTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private ResponseEntity<String> response;
    private JsonNode root;

    @BeforeEach
    void fetchApiDocs() throws Exception {
        response = testRestTemplate.getForEntity(
                "http://localhost:" + port + "/v3/api-docs", String.class);
        if (response.getStatusCode().value() == HttpStatus.OK.value()
                && response.getBody() != null) {
            root = objectMapper.readTree(response.getBody());
        }
    }

    // ── Tests ─────────────────────────────────────────────────────────────────

    /**
     * {@code GET /v3/api-docs} must return HTTP 200.
     *
     * <p><b>Validates: Requirements 1.3</b>
     */
    @Test
    void apiDocs_returns200() {
        assertThat(response.getStatusCode().value())
                .as("GET /v3/api-docs should return HTTP 200")
                .isEqualTo(HttpStatus.OK.value());
    }

    /**
     * The response body must be non-blank, valid JSON.
     *
     * <p><b>Validates: Requirements 1.3</b>
     */
    @Test
    void apiDocs_bodyIsValidJson() {
        assertThat(response.getBody())
                .as("GET /v3/api-docs body must not be blank")
                .isNotBlank();

        assertThat(root)
                .as("GET /v3/api-docs body must be parseable as JSON")
                .isNotNull();
    }

    /**
     * The root JSON object must contain the {@code openapi} field.
     *
     * <p><b>Validates: Requirements 1.3</b>
     */
    @Test
    void apiDocs_hasOpenapiField() {
        assertThat(root).isNotNull();
        assertThat(root.has("openapi"))
                .as("Root JSON must contain the 'openapi' field")
                .isTrue();
        assertThat(root.get("openapi").isNull())
                .as("'openapi' field must not be null")
                .isFalse();
    }

    /**
     * The root JSON object must contain an {@code info} field with
     * {@code title} = {@code "Smart Tourism Santander API"} and
     * {@code version} = {@code "1.0.0"}.
     *
     * <p><b>Validates: Requirements 1.3, 1.4</b>
     */
    @Test
    void apiDocs_infoHasCorrectTitleAndVersion() {
        assertThat(root).isNotNull();
        assertThat(root.has("info"))
                .as("Root JSON must contain the 'info' field")
                .isTrue();

        JsonNode info = root.get("info");
        assertThat(info.get("title").asText())
                .as("info.title must be 'Smart Tourism Santander API'")
                .isEqualTo("Smart Tourism Santander API");
        assertThat(info.get("version").asText())
                .as("info.version must be '1.0.0'")
                .isEqualTo("1.0.0");
    }

    /**
     * The root JSON object must contain a non-empty {@code paths} field.
     *
     * <p><b>Validates: Requirements 1.3</b>
     */
    @Test
    void apiDocs_pathsFieldIsNotEmpty() {
        assertThat(root).isNotNull();
        assertThat(root.has("paths"))
                .as("Root JSON must contain the 'paths' field")
                .isTrue();

        JsonNode paths = root.get("paths");
        assertThat(paths.isObject())
                .as("'paths' must be a JSON object")
                .isTrue();
        assertThat(paths.size())
                .as("'paths' must not be empty — at least one endpoint must be documented")
                .isGreaterThan(0);
    }

    /**
     * The root JSON object must contain a {@code components} field, and
     * {@code components.securitySchemes} must include the {@code bearerAuth} key.
     *
     * <p><b>Validates: Requirements 1.3, 1.4</b>
     */
    @Test
    void apiDocs_componentsContainsBearerAuthScheme() {
        assertThat(root).isNotNull();
        assertThat(root.has("components"))
                .as("Root JSON must contain the 'components' field")
                .isTrue();

        JsonNode components = root.get("components");
        assertThat(components.has("securitySchemes"))
                .as("'components' must contain 'securitySchemes'")
                .isTrue();

        JsonNode securitySchemes = components.get("securitySchemes");
        assertThat(securitySchemes.has("bearerAuth"))
                .as("'components.securitySchemes' must contain the 'bearerAuth' key")
                .isTrue();
    }
}
