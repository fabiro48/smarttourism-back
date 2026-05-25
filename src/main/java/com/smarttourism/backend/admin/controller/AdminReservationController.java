package com.smarttourism.backend.admin.controller;

import com.smarttourism.backend.admin.service.AdminReservationService;
import com.smarttourism.backend.common.enums.ReservationStatus;
import com.smarttourism.backend.reservations.dto.ReservationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * REST controller for admin-level reservation management.
 *
 * <p>All endpoints require ADMIN role.
 *
 * <p>Validates: Requirement 9.6
 */
@Tag(name = "admin", description = "Administración de reservas (solo ADMIN)")
@RestController
@RequestMapping("/api/v1/admin/reservations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminReservationController {

    private final AdminReservationService adminReservationService;

    /**
     * Retrieves all reservations with optional filtering and pagination.
     *
     * @param status        optional status filter
     * @param experienceId  optional experience ID filter
     * @param startDate     optional start date for date range filter (format: yyyy-MM-dd)
     * @param endDate       optional end date for date range filter (format: yyyy-MM-dd)
     * @param pageable      pagination parameters (default: page 0, size 20, sort by createdAt desc)
     * @return page of reservations
     */
    @Operation(summary = "Listar todas las reservas con filtros opcionales (solo ADMIN)")
    @GetMapping
    public ResponseEntity<Page<ReservationResponse>> getAllReservations(
            @RequestParam(required = false) ReservationStatus status,
            @RequestParam(required = false) UUID experienceId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ReservationResponse> reservations = adminReservationService.getAllReservations(
                status, experienceId, startDate, endDate, pageable
        );
        return ResponseEntity.ok(reservations);
    }
}
