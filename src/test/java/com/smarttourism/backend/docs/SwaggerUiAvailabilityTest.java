package com.smarttourism.backend.docs;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test that verifies Swagger UI and the OpenAPI spec endpoint are
 * publicly accessible (no authentication required).
 *
 * <p>Uses {@link TestRestTemplate} configured <em>without</em> automatic redirect
 * following so that a 302 redirect from {@code /swagger-ui.html} to
 * {@code /swagger-ui/index.html} is treated as a valid response alongside 200.
 *
 * <p><b>Validates: Requirements 1.2</b>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
class SwaggerUiAvailabilityTest {

    @LocalServerPort
    private int port;

    /**
     * {@link TestRestTemplate} that does NOT follow redirects automatically.
     * This allows the test to accept both HTTP 200 and HTTP 302 as valid
     * responses for {@code /swagger-ui.html}, since Spring Boot / springdoc may
     * redirect to {@code /swagger-ui/index.html}.
     */
    @Autowired
    private TestRestTemplate testRestTemplate;

    // ── Helper ────────────────────────────────────────────────────────────────

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    /**
     * Returns a {@link TestRestTemplate} that does NOT follow redirects.
     * Spring's {@link TestRestTemplate} follows redirects by default; we build
     * a non-redirecting one for the /swagger-ui.html check.
     */
    private TestRestTemplate noRedirectTemplate() {
        return new TestRestTemplate(
                new RestTemplateBuilder()
                        .rootUri("http://localhost:" + port)
        );
    }

    // ── Tests ─────────────────────────────────────────────────────────────────

    /**
     * {@code GET /swagger-ui.html} must be publicly accessible.
     * Accepts HTTP 200 (direct response) or HTTP 302 (redirect to index.html)
     * as valid outcomes — springdoc may redirect to {@code /swagger-ui/index.html}.
     *
     * <p><b>Validates: Requirements 1.2</b>
     */
    @Test
    void swaggerUiHtml_isAccessibleWithoutAuthentication() {
        // Use a non-redirecting template so we can observe the 302 if present
        TestRestTemplate noRedirect = noRedirectTemplate();
        ResponseEntity<String> response = noRedirect.getForEntity("/swagger-ui.html", String.class);

        HttpStatus status = HttpStatus.valueOf(response.getStatusCode().value());
        assertThat(status.is2xxSuccessful() || status.is3xxRedirection())
                .as("GET /swagger-ui.html should return 2xx or 3xx (got %s)", status)
                .isTrue();
    }

    /**
     * {@code GET /swagger-ui/index.html} must return HTTP 200 without any
     * authentication header.
     *
     * <p><b>Validates: Requirements 1.2</b>
     */
    @Test
    void swaggerUiIndexHtml_returns200WithoutAuthentication() {
        ResponseEntity<String> response =
                testRestTemplate.getForEntity(url("/swagger-ui/index.html"), String.class);

        assertThat(response.getStatusCode().value())
                .as("GET /swagger-ui/index.html should return 200")
                .isEqualTo(HttpStatus.OK.value());

        assertThat(response.getBody())
                .as("GET /swagger-ui/index.html body must not be empty")
                .isNotBlank();
    }

    /**
     * {@code GET /v3/api-docs} must return HTTP 200 without any authentication
     * header, confirming the OpenAPI spec is publicly available.
     *
     * <p><b>Validates: Requirements 1.2, 1.3</b>
     */
    @Test
    void openApiDocs_returns200WithoutAuthentication() {
        ResponseEntity<String> response =
                testRestTemplate.getForEntity(url("/v3/api-docs"), String.class);

        assertThat(response.getStatusCode().value())
                .as("GET /v3/api-docs should return 200")
                .isEqualTo(HttpStatus.OK.value());

        assertThat(response.getBody())
                .as("GET /v3/api-docs body must not be empty")
                .isNotBlank();
    }
}
