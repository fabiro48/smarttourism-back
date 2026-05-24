package com.smarttourism.backend.reviews.controller;

import com.smarttourism.backend.reviews.dto.ReviewRequest;
import com.smarttourism.backend.reviews.dto.ReviewResponse;
import com.smarttourism.backend.reviews.service.ReviewService;
import com.smarttourism.backend.users.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for review operations.
 *
 * <p>Exposes endpoints for creating and retrieving reviews:
 * - POST /api/v1/reviews (TOURIST) — create a new review
 * - GET /api/v1/experiences/{id}/reviews (public) — retrieve reviews for an experience
 *
 * <p>Requirements: 8.1, 8.5
 */
@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * Creates a new review for an experience.
     *
     * <p>Endpoint: POST /api/v1/reviews
     * Role: TOURIST (authenticated)
     *
     * <p>Business rules:
     * - Tourist must have at least one CONFIRMED reservation for the experience
     * - Tourist can only review each experience once
     * - Rating must be between 1 and 5
     *
     * @param request        the review creation request DTO (validated)
     * @param authentication the authenticated user (injected by Spring Security)
     * @return 201 Created with the created review DTO
     * @throws com.smarttourism.backend.common.exception.UnauthorizedAccessException
     *         (HTTP 403) if the tourist has no CONFIRMED reservation
     * @throws com.smarttourism.backend.common.exception.DuplicateResourceException
     *         (HTTP 409) if a review already exists for this tourist-experience pair
     * @throws jakarta.validation.ConstraintViolationException (HTTP 400) if rating
     *         is outside [1, 5] or required fields are missing
     */
    @PostMapping
    @PreAuthorize("hasRole('TOURIST')")
    public ResponseEntity<ReviewResponse> createReview(
            @Valid @RequestBody ReviewRequest request,
            Authentication authentication) {

        UUID touristId = extractUserIdFromAuthentication(authentication);
        ReviewResponse response = reviewService.createReview(touristId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves all reviews for a given experience.
     *
     * <p>Endpoint: GET /api/v1/experiences/{id}/reviews
     * Role: Public (no authentication required)
     *
     * @param experienceId the UUID of the experience
     * @return 200 OK with the list of reviews for the experience
     * @throws com.smarttourism.backend.common.exception.ResourceNotFoundException
     *         (HTTP 404) if the experience does not exist
     */
    @GetMapping("/experiences/{experienceId}/reviews")
    public ResponseEntity<List<ReviewResponse>> getReviewsByExperience(
            @PathVariable UUID experienceId) {

        List<ReviewResponse> reviews = reviewService.getReviewsByExperience(experienceId);
        return ResponseEntity.ok(reviews);
    }

    /**
     * Extracts the user ID from the Spring Security authentication object.
     *
     * <p>The principal is expected to be a {@link User} entity instance
     * (which implements {@link org.springframework.security.core.userdetails.UserDetails}).
     *
     * @param authentication the Spring Security authentication object
     * @return the UUID of the authenticated user
     * @throws IllegalArgumentException if the principal is not a User instance
     */
    private UUID extractUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            throw new IllegalArgumentException("Invalid authentication principal");
        }

        User user = (User) authentication.getPrincipal();
        return user.getId();
    }
}
