package com.smarttourism.backend.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for CORS configuration and Actuator health check.
 *
 * <p>Validates: Requirements 11.4, 12.4
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class CorsAndHealthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // ── CORS preflight ────────────────────────────────────────────────────────

    @Test
    void preflightRequest_returnsOkWithCorsHeaders() throws Exception {
        mockMvc.perform(options("/api/v1/experiences")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "Authorization,Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"))
                .andExpect(header().exists("Access-Control-Allow-Methods"));
    }

    @Test
    void preflightForPostEndpoint_returnsOkWithCorsHeaders() throws Exception {
        mockMvc.perform(options("/api/v1/auth/login")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Content-Type"))
                .andExpect(status().isOk());
    }

    // ── Actuator health check ─────────────────────────────────────────────────

    @Test
    void healthEndpoint_returnsOkWithSystemStatus() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").isNotEmpty());
    }

    @Test
    void healthEndpoint_isPubliclyAccessible() throws Exception {
        // No Authorization header — should still return 200
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    // ── Admin endpoints blocked for TOURIST ──────────────────────────────────

    @Test
    void adminEndpoints_withoutToken_return401() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/admin/reservations"))
                .andExpect(status().isForbidden());
    }
}
