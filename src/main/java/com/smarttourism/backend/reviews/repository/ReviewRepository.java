package com.smarttourism.backend.reviews.repository;

import com.smarttourism.backend.reviews.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for {@link Review} entities.
 *
 * <p>Provides methods for querying reviews by tourist, experience, and uniqueness checks.
 *
 * <p>Validates: Requirements 8.4, 8.5
 */
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    /**
     * Checks if a review already exists for the given tourist and experience.
     *
     * <p>Used to enforce the uniqueness constraint: a tourist can only review
     * each experience once (Requirement 8.4).
     *
     * @param touristId     the UUID of the tourist
     * @param experienceId  the UUID of the experience
     * @return {@code true} if a review exists, {@code false} otherwise
     */
    boolean existsByTouristIdAndExperienceId(UUID touristId, UUID experienceId);

    /**
     * Retrieves all reviews for a given experience.
     *
     * <p>Used to compute aggregated statistics (average rating, review count)
     * and to display reviews on the experience detail page (Requirement 8.5).
     *
     * @param experienceId the UUID of the experience
     * @return list of reviews for the experience (empty if none exist)
     */
    List<Review> findByExperienceId(UUID experienceId);
}
