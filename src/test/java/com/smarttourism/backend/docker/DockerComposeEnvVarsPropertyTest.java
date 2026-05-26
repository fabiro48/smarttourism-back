package com.smarttourism.backend.docker;

// Feature: api-docs-and-testing, Property 4: Todas las variables de entorno requeridas por application.yml están definidas en docker-compose

import net.jqwik.api.*;
import net.jqwik.api.lifecycle.BeforeProperty;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based test that verifies Property 4:
 * All environment variables required by {@code application.yml} are defined
 * in the {@code backend} service of {@code docker-compose.yml}.
 *
 * <p><b>Validates: Requirements 8.3</b>
 *
 * <p>Strategy:
 * <ol>
 *   <li>{@link BeforeProperty} reads {@code src/main/resources/application.yml}
 *       and extracts every {@code ${VAR_NAME}} / {@code ${VAR_NAME:default}}
 *       reference using a regex, capturing only the variable name (group 1).</li>
 *   <li>{@link BeforeProperty} also reads {@code docker-compose.yml}, locates
 *       the {@code backend:} service section, and extracts all environment
 *       variable keys from lines matching {@code VAR_NAME:} (YAML key-value)
 *       or {@code VAR_NAME=} (env-var format).</li>
 *   <li>The {@code @Provide} method returns an {@link Arbitrary} over the
 *       variable names found in {@code application.yml}.</li>
 *   <li>The property asserts that each such variable is present in the set
 *       extracted from {@code docker-compose.yml}.</li>
 * </ol>
 */
class DockerComposeEnvVarsPropertyTest {

    // ── Regex patterns ────────────────────────────────────────────────────────

    /**
     * Matches Spring-style environment variable placeholders in application.yml:
     * {@code ${VAR_NAME}} and {@code ${VAR_NAME:default_value}}.
     * Group 1 captures the variable name (uppercase letters, digits, underscores,
     * starting with an uppercase letter or underscore).
     */
    private static final Pattern APP_YML_ENV_VAR_PATTERN =
            Pattern.compile("\\$\\{([A-Z_][A-Z0-9_]*)(?::[^}]*)?\\}");

    /**
     * Matches environment variable keys in the {@code backend} service section
     * of {@code docker-compose.yml}.
     *
     * <ul>
     *   <li>YAML key-value format: {@code      VAR_NAME: value}</li>
     *   <li>Env-var format:        {@code      VAR_NAME=value}</li>
     * </ul>
     * Group 1 captures the variable name.
     */
    private static final Pattern DOCKER_COMPOSE_ENV_VAR_PATTERN =
            Pattern.compile("^\\s+([A-Z_][A-Z0-9_]*)(?::|=)");

    // ── Instance fields populated by @BeforeProperty ─────────────────────────

    /** Variable names extracted from {@code application.yml}. */
    private List<String> applicationYmlEnvVarList;

    /** Variable names extracted from the {@code backend} service in {@code docker-compose.yml}. */
    private Set<String> dockerComposeBackendEnvVars;

    // ── jqwik lifecycle ───────────────────────────────────────────────────────

    /**
     * Reads and parses both configuration files before each property execution.
     *
     * @throws IOException if either file cannot be read
     */
    @BeforeProperty
    void parseConfigurationFiles() throws IOException {
        applicationYmlEnvVarList = extractApplicationYmlEnvVars();
        dockerComposeBackendEnvVars = extractDockerComposeBackendEnvVars();

        assertThat(applicationYmlEnvVarList)
                .as("application.yml must contain at least one ${VAR} reference")
                .isNotEmpty();

        assertThat(dockerComposeBackendEnvVars)
                .as("docker-compose.yml backend service must define at least one environment variable")
                .isNotEmpty();
    }

    /**
     * Reads {@code src/main/resources/application.yml} and extracts all
     * environment variable names referenced with the {@code ${VAR_NAME}} syntax.
     *
     * @return a list of unique variable names (preserving encounter order)
     * @throws IOException if the file cannot be read
     */
    private List<String> extractApplicationYmlEnvVars() throws IOException {
        String content = Files.readString(Paths.get("src/main/resources/application.yml"));

        List<String> vars = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        Matcher matcher = APP_YML_ENV_VAR_PATTERN.matcher(content);

        while (matcher.find()) {
            String varName = matcher.group(1);
            if (seen.add(varName)) {
                vars.add(varName);
            }
        }

        return vars;
    }

    /**
     * Reads {@code docker-compose.yml} and extracts all environment variable
     * keys defined under the {@code backend:} service's {@code environment:}
     * section.
     *
     * <p>The parser uses a simple line-by-line approach:
     * <ol>
     *   <li>Scan until a line containing {@code backend:} is found.</li>
     *   <li>Within the backend section, scan until the {@code environment:}
     *       subsection is found.</li>
     *   <li>Collect lines matching the env-var key pattern until a line at the
     *       same or lower indentation level is encountered (indicating the end
     *       of the environment block).</li>
     * </ol>
     *
     * @return a set of environment variable names defined for the backend service
     * @throws IOException if the file cannot be read
     */
    private Set<String> extractDockerComposeBackendEnvVars() throws IOException {
        String content = Files.readString(Paths.get("docker-compose.yml"));
        String[] lines = content.split("\\r?\\n");

        Set<String> vars = new HashSet<>();

        boolean inBackendService = false;
        boolean inEnvironmentSection = false;
        int environmentIndent = -1;

        for (String line : lines) {
            // Skip blank lines and comment-only lines
            String trimmed = line.stripLeading();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }

            // Detect the start of the backend service block
            if (!inBackendService) {
                if (trimmed.startsWith("backend:")) {
                    inBackendService = true;
                }
                continue;
            }

            // We are inside the backend service block.
            // Detect a sibling service (same indentation as "backend:") to stop.
            int currentIndent = line.length() - trimmed.length();

            if (!inEnvironmentSection) {
                // Look for the environment: subsection
                if (trimmed.startsWith("environment:")) {
                    inEnvironmentSection = true;
                    environmentIndent = currentIndent;
                } else if (currentIndent == 0 && !trimmed.startsWith("#")) {
                    // We've left the backend service block (top-level key)
                    break;
                }
                continue;
            }

            // We are inside the environment: section.
            // Stop if we encounter a key at the same indentation as "environment:"
            // (i.e., another subsection of the backend service) or a top-level key.
            if (currentIndent <= environmentIndent && !trimmed.startsWith("-")) {
                break;
            }

            // Try to match an environment variable key
            Matcher matcher = DOCKER_COMPOSE_ENV_VAR_PATTERN.matcher(line);
            if (matcher.find()) {
                vars.add(matcher.group(1));
            }
        }

        return vars;
    }

    // ── jqwik provider ────────────────────────────────────────────────────────

    /**
     * Provides an {@link Arbitrary} of environment variable names extracted
     * from {@code application.yml}.
     */
    @Provide
    Arbitrary<String> applicationYmlEnvVars() {
        assertThat(applicationYmlEnvVarList)
                .as("applicationYmlEnvVarList must be populated by @BeforeProperty before the provider is called")
                .isNotNull()
                .isNotEmpty();
        return Arbitraries.of(applicationYmlEnvVarList);
    }

    // ── Property ──────────────────────────────────────────────────────────────

    /**
     * <b>Property 4</b>: For any environment variable referenced with the
     * {@code ${VAR_NAME}} syntax in {@code application.yml}, that variable
     * SHALL be defined in the {@code environment} section of the {@code backend}
     * service in {@code docker-compose.yml}.
     *
     * <p><b>Validates: Requirements 8.3</b>
     */
    @Property(tries = 100)
    void allApplicationYmlEnvVarsDefinedInDockerCompose(
            @ForAll("applicationYmlEnvVars") String envVar) {

        assertThat(dockerComposeBackendEnvVars)
                .as("Variable '%s' referenced in application.yml must be defined in " +
                    "the backend service environment section of docker-compose.yml", envVar)
                .contains(envVar);
    }
}
