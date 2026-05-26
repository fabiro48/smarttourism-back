package com.smarttourism.backend.postman;

// Feature: api-docs-and-testing, Property 3: Todos los requests de la colección Postman tienen al menos un test automatizado

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.jqwik.api.*;
import net.jqwik.api.lifecycle.BeforeProperty;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based test that verifies Property 3:
 * All requests in the Postman collection have at least one automated test.
 *
 * <p><b>Validates: Requirements 7.1</b>
 *
 * <p>Strategy: The Postman collection JSON is parsed once via {@link BeforeProperty}.
 * All request items are collected recursively (items with a {@code request} field).
 * The {@code @Provide} method returns an {@link Arbitrary} over the collected items.
 * The property verifies that each item has at least one event with
 * {@code listen = "test"} whose {@code script.exec} array contains at least one
 * line with {@code pm.test}.
 */
class PostmanTestScriptsPropertyTest {

    /** Represents a single request item extracted from the Postman collection. */
    record PostmanRequestItem(String name, List<JsonNode> events) {}

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /** All request items collected from the Postman collection. */
    private List<PostmanRequestItem> requestItems;

    // ── jqwik lifecycle ───────────────────────────────────────────────────────

    /**
     * Parses the Postman collection and collects all request items before each
     * property execution.
     */
    @BeforeProperty
    void parseCollection() throws IOException {
        JsonNode collection = OBJECT_MAPPER.readTree(
                Paths.get("postman/SmartTourism.postman_collection.json").toFile()
        );

        List<PostmanRequestItem> collected = new ArrayList<>();
        collectRequestItems(collection.path("item"), collected);

        assertThat(collected)
                .as("Postman collection must contain at least one request item")
                .isNotEmpty();

        requestItems = collected;
    }

    /**
     * Recursively traverses the {@code items} array and collects all leaf items
     * that have a {@code request} field (i.e., actual requests, not folders).
     *
     * @param items     the JSON array of items to traverse
     * @param collected the list to accumulate found request items into
     */
    private void collectRequestItems(JsonNode items, List<PostmanRequestItem> collected) {
        if (items == null || !items.isArray()) {
            return;
        }
        for (JsonNode item : items) {
            if (!item.path("request").isMissingNode()) {
                // Leaf item — it is a request
                String name = item.path("name").asText("(unnamed)");
                List<JsonNode> events = new ArrayList<>();
                JsonNode eventArray = item.path("event");
                if (eventArray.isArray()) {
                    eventArray.forEach(events::add);
                }
                collected.add(new PostmanRequestItem(name, events));
            } else if (item.path("item").isArray()) {
                // Folder item — recurse into its children
                collectRequestItems(item.path("item"), collected);
            }
        }
    }

    // ── jqwik provider ────────────────────────────────────────────────────────

    /**
     * Provides an {@link Arbitrary} of {@link PostmanRequestItem} instances
     * parsed from the Postman collection.
     */
    @Provide
    Arbitrary<PostmanRequestItem> postmanRequests() {
        assertThat(requestItems)
                .as("requestItems must be populated by @BeforeProperty before the provider is called")
                .isNotNull()
                .isNotEmpty();
        return Arbitraries.of(requestItems);
    }

    // ── Property ──────────────────────────────────────────────────────────────

    /**
     * <b>Property 3</b>: For any request item present in the Postman collection,
     * that item SHALL have at least one event with {@code listen = "test"} whose
     * {@code script.exec} array contains at least one line with {@code pm.test}.
     *
     * <p><b>Validates: Requirements 7.1</b>
     */
    @Property(tries = 100)
    void allRequestsHaveAtLeastOneAutomatedTest(
            @ForAll("postmanRequests") PostmanRequestItem item) {

        boolean hasTestEvent = false;

        for (JsonNode event : item.events()) {
            String listen = event.path("listen").asText();
            if (!"test".equals(listen)) {
                continue;
            }

            JsonNode exec = event.path("script").path("exec");
            if (!exec.isArray()) {
                continue;
            }

            for (JsonNode line : exec) {
                if (line.asText().contains("pm.test")) {
                    hasTestEvent = true;
                    break;
                }
            }

            if (hasTestEvent) {
                break;
            }
        }

        assertThat(hasTestEvent)
                .as("Request '%s' must have at least one 'test' event with a line containing 'pm.test'",
                        item.name())
                .isTrue();
    }
}
