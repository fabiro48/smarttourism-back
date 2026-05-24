package com.smarttourism.backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarttourism.backend.auth.dto.LoginRequest;
import com.smarttourism.backend.auth.dto.RegisterRequest;
import com.smarttourism.backend.common.enums.Difficulty;
import com.smarttourism.backend.experiences.dto.ExperienceRequest;
import com.smarttourism.backend.payments.dto.PaymentRequest;
import com.smarttourism.backend.reservations.dto.ReservationRequest;
import com.smarttourism.backend.reviews.dto.ReviewRequest;
import com.smarttourism.backend.schedules.dto.ScheduleRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for the full user flow: register → login → reserve → pay → review.
 *
 * <p>Uses Testcontainers to spin up a real PostgreSQL instance.
 *
 * <p>Validates: Requirements 1.1, 2.1, 5.1, 7.1, 8.1
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class FullFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fullFlow_registerLoginReservePayReview() throws Exception {
        // ── Step 1: Register tourist ──────────────────────────────────────────
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        RegisterRequest registerRequest = RegisterRequest.builder()
                .fullName("Turista Test")
                .email("turista." + uniqueSuffix + "@test.com")
                .password("Password123!")
                .phone("+57 300 1234567")
                .documentNumber("DOC-" + uniqueSuffix)
                .build();

        MvcResult registerResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.email").value(registerRequest.getEmail()))
                .andReturn();

        String touristToken = objectMapper.readTree(
                registerResult.getResponse().getContentAsString()
        ).get("token").asText();

        assertThat(touristToken).isNotBlank();

        // ── Step 2: Login admin to create experience ──────────────────────────
        LoginRequest adminLogin = LoginRequest.builder()
                .email("admin@smarttourism.com")
                .password("Admin123!")
                .build();

        MvcResult adminLoginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminLogin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        String adminToken = objectMapper.readTree(
                adminLoginResult.getResponse().getContentAsString()
        ).get("token").asText();

        // ── Step 3: Admin creates experience ─────────────────────────────────
        ExperienceRequest experienceRequest = new ExperienceRequest();
        experienceRequest.setTitle("Senderismo en el Cañón del Chicamocha");
        experienceRequest.setDescription("Experiencia de senderismo en el cañón más profundo de Colombia");
        experienceRequest.setCategory("Aventura");
        experienceRequest.setLocation("Cañón del Chicamocha, Santander");
        experienceRequest.setDuration(240);
        experienceRequest.setDifficulty(Difficulty.MODERATE);
        experienceRequest.setPrice(new BigDecimal("150000"));
        experienceRequest.setImages(List.of("https://example.com/chicamocha.jpg"));

        MvcResult createExpResult = mockMvc.perform(post("/api/v1/experiences")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(experienceRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andReturn();

        String experienceId = objectMapper.readTree(
                createExpResult.getResponse().getContentAsString()
        ).get("id").asText();

        // ── Step 4: Admin creates schedule ────────────────────────────────────
        ScheduleRequest scheduleRequest = new ScheduleRequest();
        scheduleRequest.setDayOfWeek(com.smarttourism.backend.common.enums.DayOfWeek.SATURDAY);
        scheduleRequest.setStartTime(LocalTime.of(8, 0));
        scheduleRequest.setEndTime(LocalTime.of(12, 0));
        scheduleRequest.setAvailableSlots(20);

        MvcResult createScheduleResult = mockMvc.perform(
                        post("/api/v1/experiences/" + experienceId + "/schedules")
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(scheduleRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andReturn();

        String scheduleId = objectMapper.readTree(
                createScheduleResult.getResponse().getContentAsString()
        ).get("id").asText();

        // ── Step 5: Tourist creates reservation ───────────────────────────────
        ReservationRequest reservationRequest = new ReservationRequest();
        reservationRequest.setExperienceId(UUID.fromString(experienceId));
        reservationRequest.setScheduleId(UUID.fromString(scheduleId));
        reservationRequest.setReservationDate(LocalDate.now().plusDays(7));
        reservationRequest.setQuantity(2);

        MvcResult createReservationResult = mockMvc.perform(post("/api/v1/reservations")
                        .header("Authorization", "Bearer " + touristToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"))
                .andReturn();

        String reservationId = objectMapper.readTree(
                createReservationResult.getResponse().getContentAsString()
        ).get("id").asText();

        // ── Step 6: Tourist simulates payment ─────────────────────────────────
        PaymentRequest paymentRequest = PaymentRequest.builder()
                .reservationId(UUID.fromString(reservationId))
                .build();

        mockMvc.perform(post("/api/v1/payments/simulate")
                        .header("Authorization", "Bearer " + touristToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionReference").isNotEmpty());

        // ── Step 7: Tourist views their reservations ──────────────────────────
        mockMvc.perform(get("/api/v1/reservations/me")
                        .header("Authorization", "Bearer " + touristToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // ── Step 8: Tourist leaves a review (only if reservation is CONFIRMED) ─
        ReviewRequest reviewRequest = ReviewRequest.builder()
                .experienceId(UUID.fromString(experienceId))
                .rating(5)
                .comment("Experiencia increíble, totalmente recomendada!")
                .build();

        int reviewStatus = mockMvc.perform(post("/api/v1/reviews")
                        .header("Authorization", "Bearer " + touristToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewRequest)))
                .andReturn()
                .getResponse()
                .getStatus();

        // Payment simulation is random — accept CONFIRMED (201) or PENDING_PAYMENT (403)
        assertThat(reviewStatus).isIn(201, 403);
    }

    @Test
    void register_withDuplicateEmail_returns409() throws Exception {
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Usuario Test")
                .email("dup." + uniqueSuffix + "@test.com")
                .password("Password123!")
                .phone("+57 300 0000000")
                .documentNumber("DUP-" + uniqueSuffix)
                .build();

        // First registration — should succeed
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Second registration with same email — should fail with 409
        RegisterRequest duplicate = RegisterRequest.builder()
                .fullName("Otro Usuario")
                .email(request.getEmail())
                .password("Password123!")
                .phone("+57 300 9999999")
                .documentNumber("OTHER-" + uniqueSuffix)
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void login_withInvalidCredentials_returns401() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("nonexistent@test.com")
                .password("WrongPassword!")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void publicEndpoints_areAccessibleWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/experiences"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void protectedEndpoints_requireAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isUnauthorized());
    }
}
