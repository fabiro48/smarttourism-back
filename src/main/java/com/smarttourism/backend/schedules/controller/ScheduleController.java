package com.smarttourism.backend.schedules.controller;

import com.smarttourism.backend.schedules.dto.ScheduleRequest;
import com.smarttourism.backend.schedules.dto.ScheduleResponse;
import com.smarttourism.backend.schedules.service.ScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST controller that exposes schedule management endpoints nested under
 * {@code /api/v1/experiences/{id}/schedules}.
 *
 * <p>All endpoints are restricted to users with the {@code ADMIN} role,
 * enforced both by Spring Security's filter chain (see {@code SecurityConfig})
 * and by {@code @PreAuthorize} (Requirement 9.4).
 *
 * <p>Validates: Requirements 9.4, 9.5
 */
@RestController
@RequestMapping("/api/v1/experiences/{experienceId}/schedules")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ScheduleController {

    private final ScheduleService scheduleService;

    /**
     * Creates a new schedule for the given experience.
     *
     * @param experienceId the UUID of the parent experience
     * @param request      the creation payload; validated with {@code @Valid}
     * @return HTTP 201 with the persisted {@link ScheduleResponse} DTO,
     *         or HTTP 404 if the experience does not exist or is inactive
     */
    @PostMapping
    public ResponseEntity<ScheduleResponse> createSchedule(
            @PathVariable UUID experienceId,
            @Valid @RequestBody ScheduleRequest request) {

        ScheduleResponse response = scheduleService.createSchedule(experienceId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Updates an existing schedule.
     *
     * @param experienceId the UUID of the parent experience (used for URL scoping)
     * @param scheduleId   the UUID of the schedule to update
     * @param request      the update payload; validated with {@code @Valid}
     * @return HTTP 200 with the updated {@link ScheduleResponse} DTO,
     *         or HTTP 404 if the schedule does not exist
     */
    @PutMapping("/{scheduleId}")
    public ResponseEntity<ScheduleResponse> updateSchedule(
            @PathVariable UUID experienceId,
            @PathVariable UUID scheduleId,
            @Valid @RequestBody ScheduleRequest request) {

        ScheduleResponse response = scheduleService.updateSchedule(scheduleId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Logically deletes a schedule by setting {@code active = false}.
     * The record is never physically removed from the database (Requirement 9.5).
     *
     * @param experienceId the UUID of the parent experience (used for URL scoping)
     * @param scheduleId   the UUID of the schedule to deactivate
     * @return HTTP 204 No Content on success,
     *         or HTTP 404 if the schedule does not exist
     */
    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<Void> deactivateSchedule(
            @PathVariable UUID experienceId,
            @PathVariable UUID scheduleId) {

        scheduleService.deactivateSchedule(scheduleId);
        return ResponseEntity.noContent().build();
    }
}
