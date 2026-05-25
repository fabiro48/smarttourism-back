package com.smarttourism.backend.experiences.controller;

import com.smarttourism.backend.common.enums.Difficulty;
import com.smarttourism.backend.experiences.dto.ExperienceFilterParams;
import com.smarttourism.backend.experiences.dto.ExperienceRequest;
import com.smarttourism.backend.experiences.dto.ExperienceResponse;
import com.smarttourism.backend.experiences.service.ExperienceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * REST controller that exposes the experiences catalogue endpoints.
 *
 * <p>Public endpoints ({@code GET}) are accessible without authentication.
 * Write endpoints ({@code POST}, {@code PUT}, {@code DELETE}) are restricted
 * to users with the {@code ADMIN} role, enforced both by Spring Security's
 * filter chain (see {@code SecurityConfig}) and by {@code @PreAuthorize}.
 *
 * <p>Validates: Requirements 3.1, 3.2, 3.3, 9.1, 9.2, 9.3, 9.4
 */
@Tag(name = "experiences", description = "Catálogo de experiencias turísticas")
@RestController
@RequestMapping("/api/v1/experiences")
@RequiredArgsConstructor
public class ExperienceController {

    private final ExperienceService experienceService;

    // ── Public read endpoints ─────────────────────────────────────────────────

    /**
     * Returns a paginated list of active experiences, optionally filtered.
     *
     * <p>All filter parameters are optional. When multiple filters are provided
     * they are combined with AND semantics (Requirement 4.6).
     *
     * @param category   exact match on category (optional)
     * @param location   case-insensitive partial match on location (optional)
     * @param difficulty exact match on difficulty level (optional)
     * @param minPrice   minimum price inclusive (optional)
     * @param maxPrice   maximum price inclusive (optional)
     * @param available  when {@code true}, only experiences with available slots (optional)
     * @param pageable   pagination and sorting (default: page 0, size 20)
     * @return HTTP 200 with a page of {@link ExperienceResponse} DTOs
     */
    @Operation(summary = "Listar experiencias activas con filtros opcionales")
    @GetMapping
    public ResponseEntity<Page<ExperienceResponse>> getExperiences(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Difficulty difficulty,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean available,
            @PageableDefault(size = 20) Pageable pageable) {

        ExperienceFilterParams filters = ExperienceFilterParams.builder()
                .category(category)
                .location(location)
                .difficulty(difficulty)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .available(available)
                .build();

        Page<ExperienceResponse> page = experienceService.getExperiences(filters, pageable);
        return ResponseEntity.ok(page);
    }

    /**
     * Returns the full detail of a single active experience, including its
     * active schedules and aggregated review statistics.
     *
     * @param id the UUID of the experience
     * @return HTTP 200 with the {@link ExperienceResponse} DTO,
     *         or HTTP 404 if the experience does not exist or is inactive
     */
    @Operation(summary = "Obtener detalle de una experiencia por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ExperienceResponse> getExperienceById(@PathVariable UUID id) {
        ExperienceResponse response = experienceService.getExperienceById(id);
        return ResponseEntity.ok(response);
    }

    // ── Admin write endpoints ─────────────────────────────────────────────────

    /**
     * Creates a new active experience.
     *
     * <p>Restricted to {@code ADMIN} role (Requirement 9.4).
     *
     * @param request the creation payload; validated with {@code @Valid}
     * @return HTTP 201 with the persisted {@link ExperienceResponse} DTO
     */
    @Operation(summary = "Crear una nueva experiencia (solo ADMIN)")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ExperienceResponse> createExperience(
            @Valid @RequestBody ExperienceRequest request) {

        ExperienceResponse response = experienceService.createExperience(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Updates an existing experience.
     *
     * <p>Restricted to {@code ADMIN} role (Requirement 9.4).
     *
     * @param id      the UUID of the experience to update
     * @param request the update payload; validated with {@code @Valid}
     * @return HTTP 200 with the updated {@link ExperienceResponse} DTO,
     *         or HTTP 404 if the experience does not exist
     */
    @Operation(summary = "Actualizar una experiencia existente (solo ADMIN)")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ExperienceResponse> updateExperience(
            @PathVariable UUID id,
            @Valid @RequestBody ExperienceRequest request) {

        ExperienceResponse response = experienceService.updateExperience(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Logically deletes an experience by setting {@code active = false}.
     * The record is never physically removed from the database (Requirement 9.3).
     *
     * <p>Restricted to {@code ADMIN} role (Requirement 9.4).
     *
     * @param id the UUID of the experience to deactivate
     * @return HTTP 204 No Content on success,
     *         or HTTP 404 if the experience does not exist
     */
    @Operation(summary = "Desactivar una experiencia (borrado lógico, solo ADMIN)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteExperience(@PathVariable UUID id) {
        experienceService.deleteExperience(id);
        return ResponseEntity.noContent().build();
    }
}
