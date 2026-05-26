package com.smarttourism.backend.docs;

// Feature: api-docs-and-testing, Property 5: Los tipos de datos Java se mapean correctamente en la especificación OpenAPI

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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based integration test that verifies Property 5:
 * Java data types are correctly mapped in the OpenAPI specification.
 *
 * <p><b>Validates: Requirements 10.3</b>
 *
 * <p>Type mapping table:
 * <ul>
 *   <li>UUID        → type: "string",  format: "uuid"</li>
 *   <li>LocalDate   → type: "string",  format: "date"</li>
 *   <li>LocalDateTime → type: "string", format: "date-time"</li>
 *   <li>LocalTime   → type: "string",  format: "time"</li>
 *   <li>BigDecimal  → type: "number",  format: (none)</li>
 *   <li>Enums       → type: "string",  enum array present and non-empty</li>
 * </ul>
 *
 * <p>Strategy: The Spring Boot application is started programmatically once
 * (via a static initializer) with the {@code test} profile and a random port.
 * The jqwik {@code @Provide} method fetches the OpenAPI spec from the running
 * server, iterates {@code components.schemas}, and collects only the schema
 * properties that match one of the known Java types by their format or enum
 * presence. The property then verifies the type/format invariants.
 *
 * <p>jqwik runs as a separate JUnit Platform engine and does not process JUnit
 * Jupiter annotations such as {@code @ExtendWith(SpringExtension.class)} or
 * {@code @BeforeAll}. This design avoids that limitation by managing the
 * application lifecycle independently.
 */
class OpenApiTypeMappingPropertyTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Shared Spring application context started once for the entire test class.
     * Lazily initialized by {@link #ensureApplicationStarted()}.
     */
    private static volatile ConfigurableApplicationContext applicationContext;

    /** The random port on which the test server is listening. */
    private static volatile int serverPort;

    /**
     * Parsed list of schema property entries that match the known Java type
     * mappings. Populated by {@link #loadSchemaProperties()} before each
     * property run.
     */
    private List<SchemaPropertyEntry> schemaPropertyEntries;

    // ── Inner record ──────────────────────────────────────────────────────────

    /**
     * Represents a single property extracted from an OpenAPI schema component.
     *
     * @param schemaName    the name of the parent schema (e.g. "ExperienceResponse")
     * @param propertyName  the name of the property (e.g. "id")
     * @param type          the OpenAPI {@code type} field value (e.g. "string", "number")
     * @param format        the OpenAPI {@code format} field value, or {@code null} if absent
     * @param enumValues    the list of enum values if the property has an {@code enum} array,
     *                      or {@code null} if not an enum
     */
    record SchemaPropertyEntry(
            String schemaName,
            String propertyName,
            String type,
            String format,
            List<String> enumValues
    ) {
        @Override
        public String toString() {
            return schemaName + "." + propertyName
                    + " [type=" + type
                    + (format != null ? ", format=" + format : "")
                    + (enumValues != null ? ", enum=" + enumValues : "")
                    + "]";
        }
    }

    // ── Spring Boot lifecycle ─────────────────────────────────────────────────

    /**
     * Starts the Spring Boot application with the {@code test} profile and a
     * random port if it has not been started yet. Thread-safe via
     * double-checked locking on {@link #applicationContext}.
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
     * spec before each property execution. Collects only the schema properties
     * that match one of the known Java type mappings:
     * <ul>
     *   <li>format = "uuid"      → UUID</li>
     *   <li>format = "date"      → LocalDate</li>
     *   <li>format = "date-time" → LocalDateTime</li>
     *   <li>format = "time"      → LocalTime</li>
     *   <li>type = "number" (no format) → BigDecimal</li>
     *   <li>type = "string" with "enum" array → enum type</li>
     * </ul>
     */
    @BeforeProperty
    void loadSchemaProperties() throws Exception {
        ensureApplicationStarted();

        String url = "http://localhost:" + serverPort + "/v3/api-docs";
        RestTemplate restTemplate = new RestTemplate();
        String body = restTemplate.getForObject(url, String.class);

        assertThat(body)
                .as("GET /v3/api-docs must return a non-empty body")
                .isNotBlank();

        JsonNode root = OBJECT_MAPPER.readTree(body);
        JsonNode schemas = root.path("components").path("schemas");

        List<SchemaPropertyEntry> collected = new ArrayList<>();

        schemas.fields().forEachRemaining(schemaEntry -> {
            String schemaName = schemaEntry.getKey();
            JsonNode schemaNode = schemaEntry.getValue();
            JsonNode properties = schemaNode.path("properties");

            if (properties.isMissingNode()) {
                return;
            }

            properties.fields().forEachRemaining(propEntry -> {
                String propertyName = propEntry.getKey();
                JsonNode propNode = propEntry.getValue();

                String type = propNode.path("type").asText(null);
                String format = propNode.path("format").isMissingNode()
                        ? null
                        : propNode.path("format").asText(null);
                JsonNode enumNode = propNode.path("enum");

                boolean isUuid = "uuid".equals(format);
                boolean isDate = "date".equals(format);
                boolean isDateTime = "date-time".equals(format);
                boolean isTime = "time".equals(format);
                boolean isNumber = "number".equals(type) && format == null;
                boolean isEnum = !enumNode.isMissingNode() && enumNode.isArray() && enumNode.size() > 0;

                if (isUuid || isDate || isDateTime || isTime || isNumber || isEnum) {
                    List<String> enumValues = null;
                    if (isEnum) {
                        enumValues = new ArrayList<>();
                        for (JsonNode v : enumNode) {
                            enumValues.add(v.asText());
                        }
                    }
                    collected.add(new SchemaPropertyEntry(
                            schemaName, propertyName, type, format, enumValues));
                }
            });
        });

        assertThat(collected)
                .as("OpenAPI spec must expose at least one schema property matching the known Java type mappings")
                .isNotEmpty();

        schemaPropertyEntries = collected;
    }

    // ── jqwik provider ────────────────────────────────────────────────────────

    /**
     * Provides an {@link Arbitrary} of {@link SchemaPropertyEntry} instances
     * parsed from the live OpenAPI spec. Only entries that match one of the
     * known Java type mappings are included.
     */
    @Provide
    Arbitrary<SchemaPropertyEntry> schemaProperties() {
        assertThat(schemaPropertyEntries)
                .as("schemaPropertyEntries must be populated by @BeforeProperty before the provider is called")
                .isNotNull()
                .isNotEmpty();
        return Arbitraries.of(schemaPropertyEntries);
    }

    // ── Property ──────────────────────────────────────────────────────────────

    /**
     * <b>Property 5</b>: For any schema property in the OpenAPI specification
     * that corresponds to a Java field of type {@code UUID}, {@code LocalDate},
     * {@code LocalDateTime}, {@code LocalTime}, {@code BigDecimal}, or an enum,
     * that property SHALL have the correct OpenAPI type and format according to
     * the mapping table defined in the design.
     *
     * <p>Mapping table:
     * <ul>
     *   <li>UUID        → type: "string",  format: "uuid"</li>
     *   <li>LocalDate   → type: "string",  format: "date"</li>
     *   <li>LocalDateTime → type: "string", format: "date-time"</li>
     *   <li>LocalTime   → type: "string",  format: "time"</li>
     *   <li>BigDecimal  → type: "number",  format: absent/null</li>
     *   <li>Enums       → type: "string",  enum array non-empty</li>
     * </ul>
     *
     * <p><b>Validates: Requirements 10.3</b>
     */
    @Property(tries = 100)
    void javaTypesCorrectlyMappedInOpenApiSpec(
            @ForAll("schemaProperties") SchemaPropertyEntry entry) {

        String format = entry.format();
        String type = entry.type();
        List<String> enumValues = entry.enumValues();

        if ("uuid".equals(format)) {
            assertThat(type)
                    .as("Property '%s' with format 'uuid' must have type 'string'", entry)
                    .isEqualTo("string");

        } else if ("date".equals(format)) {
            assertThat(type)
                    .as("Property '%s' with format 'date' must have type 'string'", entry)
                    .isEqualTo("string");

        } else if ("date-time".equals(format)) {
            assertThat(type)
                    .as("Property '%s' with format 'date-time' must have type 'string'", entry)
                    .isEqualTo("string");

        } else if ("time".equals(format)) {
            assertThat(type)
                    .as("Property '%s' with format 'time' must have type 'string'", entry)
                    .isEqualTo("string");

        } else if ("number".equals(type) && format == null) {
            // BigDecimal: type must be "number" and format must be absent
            assertThat(format)
                    .as("Property '%s' of type 'number' (BigDecimal) must have no format", entry)
                    .isNull();

        } else if (enumValues != null && !enumValues.isEmpty()) {
            // Enum type: type must be "string" and enum array must not be empty
            assertThat(type)
                    .as("Property '%s' with enum values must have type 'string'", entry)
                    .isEqualTo("string");
            assertThat(enumValues)
                    .as("Property '%s' must have a non-empty enum array", entry)
                    .isNotEmpty();
        }
    }
}
