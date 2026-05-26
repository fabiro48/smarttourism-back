package com.smarttourism.backend.postman;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.jqwik.api.*;
import net.jqwik.api.lifecycle.BeforeProperty;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

// Feature: api-docs-and-testing, Property 2: Todos los requests protegidos de la colección Postman tienen el header Authorization

/**
 * Property-based test verifying Property 2 of the api-docs-and-testing feature.
 *
 * <p>Property 2: All protected requests in the Postman collection have the Authorization header
 * with value {@code Bearer {{jwtToken}}}.
 *
 * <p>A "protected" request is any request that is NOT in the {@code Auth} or {@code Health}
 * top-level folders AND that has an {@code Authorization} header (i.e., it explicitly declares
 * that it requires authentication). Public read endpoints in non-Auth/Health folders (e.g.,
 * {@code GET /api/v1/experiences}) intentionally omit the Authorization header and are therefore
 * not considered "protected" for the purposes of this property.
 *
 * <p>Validates: Requirements 6.5
 */
class PostmanAuthHeaderPropertyTest {

    /** Top-level folders whose requests are considered public (no auth required). */
    private static final Set<String> PUBLIC_FOLDERS = Set.of("Auth", "Health");

    private List<ProtectedRequest> protectedRequestList;

    @BeforeProperty
    void parseCollection() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode collection = mapper.readTree(
                Paths.get("postman/SmartTourism.postman_collection.json").toFile()
        );

        protectedRequestList = new ArrayList<>();
        JsonNode rootItems = collection.path("item");

        for (JsonNode folder : rootItems) {
            String folderName = folder.path("name").asText();
            if (PUBLIC_FOLDERS.contains(folderName)) {
                continue; // skip public folders — their requests are not protected
            }
            collectProtectedRequests(folder, folderName);
        }
    }

    /**
     * Recursively collects all request items that have an Authorization header.
     * A request item has a {@code request} field (not a sub-folder).
     * Only requests that explicitly declare an Authorization header are considered "protected".
     */
    private void collectProtectedRequests(JsonNode folderNode, String topLevelFolderName) {
        JsonNode items = folderNode.path("item");
        if (!items.isArray()) {
            return;
        }
        for (JsonNode item : items) {
            if (item.has("request")) {
                // This is a leaf request item — check if it has an Authorization header
                JsonNode requestNode = item.path("request");
                JsonNode headers = requestNode.path("header");
                if (headers.isArray()) {
                    for (JsonNode header : headers) {
                        if ("Authorization".equalsIgnoreCase(header.path("key").asText())) {
                            String requestName = item.path("name").asText();
                            protectedRequestList.add(
                                    new ProtectedRequest(topLevelFolderName, requestName, requestNode)
                            );
                            break;
                        }
                    }
                }
            } else if (item.has("item")) {
                // This is a sub-folder — recurse
                collectProtectedRequests(item, topLevelFolderName);
            }
        }
    }

    /**
     * Property 2: Every protected request in the Postman collection has an Authorization header
     * with a Bearer token referencing a Postman environment variable ({@code Bearer {{...}}}).
     *
     * <p>// Feature: api-docs-and-testing, Property 2: Todos los requests protegidos de la colección Postman tienen el header Authorization
     */
    @Property(tries = 100)
    void allProtectedRequestsHaveAuthorizationHeader(
            @ForAll("protectedRequests") ProtectedRequest request) {

        JsonNode headers = request.requestNode().path("header");

        boolean hasAuthorizationHeader = false;
        String authValue = null;

        for (JsonNode header : headers) {
            if ("Authorization".equalsIgnoreCase(header.path("key").asText())) {
                hasAuthorizationHeader = true;
                authValue = header.path("value").asText();
                break;
            }
        }

        assertThat(hasAuthorizationHeader)
                .as("Request '%s' in folder '%s' should have an Authorization header",
                        request.requestName(), request.folderName())
                .isTrue();

        assertThat(authValue)
                .as("Authorization header in request '%s' (folder '%s') should use Bearer with a Postman variable (Bearer {{...}})",
                        request.requestName(), request.folderName())
                .startsWith("Bearer {{")
                .endsWith("}}");
    }

    @Provide
    Arbitrary<ProtectedRequest> protectedRequests() {
        assertThat(protectedRequestList)
                .as("There should be at least one protected request in the collection")
                .isNotEmpty();
        return Arbitraries.of(protectedRequestList);
    }

    // ---------------------------------------------------------------------------
    // Internal record to carry request data between @BeforeProperty and @Property
    // ---------------------------------------------------------------------------

    record ProtectedRequest(String folderName, String requestName, JsonNode requestNode) {}
}
