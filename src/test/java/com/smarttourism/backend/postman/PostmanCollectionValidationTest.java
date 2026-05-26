package com.smarttourism.backend.postman;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test that validates the structure of the Postman collection file.
 * Requirements: 6.1, 6.2, 6.4
 */
class PostmanCollectionValidationTest {

    private static final List<String> EXPECTED_FOLDERS = List.of(
            "Auth", "Experiences", "Schedules", "Reservations",
            "Payments", "Reviews", "Admin", "Health"
    );

    private static JsonNode collection;

    @BeforeAll
    static void loadCollection() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        collection = mapper.readTree(
                Paths.get("postman/SmartTourism.postman_collection.json").toFile()
        );
    }

    @Test
    void collectionSchema_shouldBeV21() {
        String schema = collection.path("info").path("schema").asText();
        assertThat(schema)
                .as("info.schema should contain 'v2.1'")
                .contains("v2.1");
    }

    @Test
    void rootItems_shouldContainExactly8Folders() {
        JsonNode items = collection.path("item");
        assertThat(items.isArray())
                .as("Root 'item' node should be an array")
                .isTrue();

        List<String> folderNames = new ArrayList<>();
        items.forEach(item -> folderNames.add(item.path("name").asText()));

        assertThat(folderNames)
                .as("Root item array should contain exactly 8 folders")
                .hasSize(8);

        assertThat(folderNames)
                .as("Root folders should match expected names")
                .containsExactlyInAnyOrderElementsOf(EXPECTED_FOLDERS);
    }

    @Test
    void collectionEvents_shouldContainPreRequestScriptWithNonEmptyExec() {
        JsonNode events = collection.path("event");
        assertThat(events.isArray())
                .as("Root 'event' node should be an array")
                .isTrue();

        boolean foundPreRequest = false;
        for (JsonNode event : events) {
            if ("prerequest".equals(event.path("listen").asText())) {
                JsonNode exec = event.path("script").path("exec");
                assertThat(exec.isArray())
                        .as("Pre-request script 'exec' should be an array")
                        .isTrue();
                assertThat(exec.size())
                        .as("Pre-request script 'exec' array should be non-empty")
                        .isGreaterThan(0);
                foundPreRequest = true;
                break;
            }
        }

        assertThat(foundPreRequest)
                .as("Collection should have at least one event with listen='prerequest'")
                .isTrue();
    }
}
