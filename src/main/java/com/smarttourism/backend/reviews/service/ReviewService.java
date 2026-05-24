package com.smarttourism.backend.reviews.service;

import com.smarttourism.backend.common.enums.ReservationStatus;
import com.smarttourism.backend.common.exception.DuplicateResourceException;
import com.smarttourism.backend.common.exception.ResourceNotFoundException;
import com.smarttourism.backend.common.exception.UnauthorizedAccessException;
import com.smarttourism.backend.experiences.entity.Experience;
import com.smarttourism.backend.experiences.repository.ExperienceRepository;
import com.smarttourism.backend.reservations.entity.Reservation;
import com.smarttourism.backend.reservations.repository.ReservationRepository;
import com.smarttourism.backend.reviews.dto.ReviewRequest;
import com.smarttourism.backend.reviews.dto.ReviewResponse;
import com.smarttourism.backend.reviews.entity.Review;
import com.smarttourism.backend.reviews.mapper.ReviewMapper;
import com.smarttourism.backend.reviews.repository.ReviewRepository;
import com.smarttourism.backend.users.entity.User;
import com.smarttourism.backend.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Application service for the {@link Review} domain.
 *
 * <p>Handles creation and retrieval of reviews with business rule validation:
 * - Tourist must have a CONFIRMED reservation for the experience
 * - Only one review per tourist per experience (uniqueness constraint)
 *
 * <p>Requirements: 8.1, 8.3, 8.4
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReservationRepository reservationRepository;
    private final ExperienceRepository experienceRepository;
    private final UserRepository userRepository;
    private final ReviewMapper reviewMapper;

    // ── Queries ───────────────────────────────────────────────────────────────

    /**
     * Retrieves all reviews for a given experience.
     *
     * <p>Used to display reviews on the experience detail page and to compute
     * aggregated statistics (Requirement 8.5).
     *
     * @param experienceId the UUID of the experience
     * @return list of reviews for the experience (empty if none exist)
     */
    public List<ReviewResponse> getReviewsByExperience(UUID experienceId) {
        // Verify that the experience exists
        if (!experienceRepository.existsById(experienceId)) {
            throw new ResourceNotFoundException("Experiencia no encontrada con id: " + experienceId);
        }

        return reviewRepository.findByExperienceId(experienceId).stream()
                .map(reviewMapper::toResponse)
                .toList();
    }

    // ── Commands ──────────────────────────────────────────────────────────────

    /**
     * Creates a new review for the given experience by the given tourist.
     *
     * <p>Business rules enforced:
     * 1. Tourist must have at least one CONFIRMED reservation for the experience
     *    (Requirement 8.3) — throws 403 if not
     * 2. Tourist can only review each experience once (Requirement 8.4) —
     *    throws 409 if a review already exists
     *
     * @param touristId the UUID of the tourist creating the review
     * @param request   the review creation request DTO
     * @return the created {@link ReviewResponse} DTO
     * @throws ResourceNotFoundException if the tourist or experience does not exist
     * @throws UnauthorizedAccessException (HTTP 403) if the tourist has no CONFIRMED
     *         reservation for the experience
     * @throws DuplicateResourceException (HTTP 409) if a review already exists
     */
    @Transactional
    public ReviewResponse createReview(UUID touristId, ReviewRequest request) {
        // Verify that the tourist exists
        User tourist = userRepository.findById(touristId)
                .orElseThrow(() -> new ResourceNotFoundException("Turista no encontrado con id: " + touristId));

        // Verify that the experience exists
        Experience experience = experienceRepository.findById(request.getExperienceId())
                .orElseThrow(() -> new ResourceNotFoundException("Experiencia no encontrada con id: " + request.getExperienceId()));

        // Requirement 8.3: Verify that the tourist has at least one CONFIRMED
        // reservation for this experience
        boolean hasConfirmedReservation = reservationRepository
                .findByTouristIdOrderByCreatedAtDesc(touristId).stream()
                .anyMatch(res -> res.getExperience().getId().equals(request.getExperienceId())
                        && res.getStatus() == ReservationStatus.CONFIRMED);

        if (!hasConfirmedReservation) {
            throw new UnauthorizedAccessException(
                    "Solo puedes calificar experiencias que hayas reservado");
        }

        // Requirement 8.4: Verify uniqueness — tourist can only review each
        // experience once
        if (reviewRepository.existsByTouristIdAndExperienceId(touristId, request.getExperienceId())) {
            throw new DuplicateResourceException(
                    "Ya existe una reseña del turista para esta experiencia");
        }

        // Create and persist the review
        Review review = Review.builder()
                .tourist(tourist)
                .experience(experience)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        Review saved = reviewRepository.save(review);
        return reviewMapper.toResponse(saved);
    }
}
