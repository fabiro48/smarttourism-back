package com.smarttourism.backend.docs;

// Feature: api-docs-and-testing, Property 1: Todos los endpoints de la especificación OpenAPI tienen al menos un tag

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarttourism.backend.SmartTourismApplication;
import net.jqwik.api.*;
import net.jqwik.api.lifecycle.BeforeProperty;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based integration test that verifies Property 1:
 * All endpoints in the OpenAPI specification have at least one tag.
 *
 * <p><b>Validates: Requirements 5.5</b>
 *
 * <p>Strategy: The Spring Boot application is started programmatically once
 * (via a static initializer) with the {@code test} profile and a random port.
 * The jqwik {@code @Provide} method fetches the OpenAPI spec from the running
 * server and returns an {@link Arbitrary} of endpoint operations.
 *
 * <p>jqwik runs as a separate JUnit Platform engine and does not process JUnit
 * Jupiter annotations such as {@code @ExtendWith(SpringExtension.class)} or
 * {@code @BeforeAll}. This design avoids that limitation by managing the
 * application lifecycle independently.
 */
class OpenApiTagsPropertyTest {

    /** HTTP methods that represent OpenAPI operations inside a path item. */
    private static final List<String> HTTP_METHODS =
            List.of("get", "post", "put", "delete", "patch", "options", "head", "trace");

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Shared Spring application context started once for the entire test class.
     * Lazily initialized by {@link #ensureApplicationStarted()}.
     */
    private static volatile ConfigurableApplicationContext applicationContext;

    /** The random port on which the test server is listening. */
    private static volatile int serverPort;

    /**
     * Parsed list of (path+method → operationNode) entries from the live spec.
     * Populated by {@link #loadOperations()} before each property run.
     */
    private List<Map.Entry<String, JsonNode>> operations;

    // ── Spring Boot lifecycle ─────────────────────────────────────────────────

    /**
     * Starts the Spring Boot application with the {@code test} profile and a
     * random port if it has not been started yet. Thread-safe via double-checked
     * locking on {@link #applicationContext}.
     */
    private static synchronized void ensureApplicationStarted() {
        if (applicationContext != null) {
            return;
        }
        SpringApplication app = new SpringApplication(SmartTourismApplication.class);
        app.setAdditionalProfiles("test");
        ConfigurableApplicationContext ctx = app.run(
                "--server.port=0",
                "--spring.main.banner-mode=off"
        );
        Environment env = ctx.getEnvironment();
        serverPort = Integer.parseInt(env.getProperty("local.server.port", "8080"));
        applicationContext = ctx;
    }

    // ── jqwik lifecycle ───────────────────────────────────────────────────────

    /**
     * Ensures the Spring Boot application is running and fetches the OpenAPI
     * spec before each property execution.
     */
    @BeforeProperty
    void loadOperations() throws Exception {
        ensureApplicationStarted();

        String url = "http://localhost:" + serverPort + "/v3/api-docs";
        RestTemplate restTemplate = new RestTemplate();
        String body = restTemplate.getForObject(url, String.class);

        assertThat(body)
                .as("GET /v3/api-docs must return a non-empty body")
                .isNotBlank();

        JsonNode root = OBJECT_MAPPER.readTree(body);
        JsonNode paths = root.path("paths");

        List<Map.Entry<String, JsonNode>> collected = new ArrayList<>();
        paths.fields().forEachRemaining(pathEntry -> {
            String path = pathEntry.getKey();
            JsonNode pathItem = pathEntry.getValue();
            for (String method : HTTP_METHODS) {
                JsonNode operation = pathItem.path(method);
                if (!operation.isMissingNode()) {
                    collected.add(Map.entry(path + " [" + method.toUpperCase() + "]", operation));
                }
            }
        });

        assertThat(collected)
                .as("OpenAPI spec must expose at least one endpoint")
                .isNotEmpty();

        operations = collected;
    }

    // ── jqwik provider ────────────────────────────────────────────────────────

    /**
     * Provides an {@link Arbitrary} of endpoint operations parsed from the live
     * OpenAPI spec. Each sample is a {@link Map.Entry} whose key is
     * {@code "PATH [METHOD]"} and whose value is the operation JSON node.
     */
    @Provide
    Arbitrary<Map.Entry<String, JsonNode>> endpointOperations() {
        assertThat(operations)
                .as("operations must be populated by @BeforeProperty before the provider is called")
                .isNotNull()
                .isNotEmpty();
        return Arbitraries.of(operations);
    }

    // ── Property ──────────────────────────────────────────────────────────────

    /**
     * <b>Property 1</b>: For any endpoint (path + HTTP method) present in the
     * OpenAPI specification generated by the system, that endpoint SHALL have at
     * least one tag assigned.
     *
     * <p><b>Validates: Requirements 5.5</b>
     */
    @Property(tries = 100)
    void allEndpointsHaveAtLeastOneTag(
            @ForAll("endpointOperations") Map.Entry<String, JsonNode> entry) {

        String operationKey = entry.getKey();
        JsonNode operation = entry.getValue();

        JsonNode tags = operation.path("tags");

        assertThat(tags.isMissingNode())
                .as("Operation '%s' must have a 'tags' field", operationKey)
                .isFalse();

        assertThat(tags.isArray())
                .as("Operation '%s' 'tags' field must be an array", operationKey)
                .isTrue();

        assertThat(tags.size())
                .as("Operation '%s' must have at least one tag, but 'tags' array is empty", operationKey)
                .isGreaterThan(0);
    }
}
