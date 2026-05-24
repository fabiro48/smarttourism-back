package com.smarttourism.backend.reviews;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.smarttourism.backend.reviews.dto.ReviewRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for review business rules.
 *
 * <p>Validates: Requirements 8.2, 8.3
 */
class ReviewProperties {

    private final Validator validator;

    ReviewProperties() {
        LocalValidatorFactoryBean factory = new LocalValidatorFactoryBean();
        factory.afterPropertiesSet();
        this.validator = factory;
    }

    // ── Property 11: Rating dentro del rango válido [1, 5] ───────────────────

    @Property(tries = 100)
    void ratingBelowOneShouldFailValidation(
            @ForAll @IntRange(min = Integer.MIN_VALUE, max = 0) int invalidRating
    ) {
        // Feature: smart-tourism-backend, Property 11: Rating dentro del rango válido [1, 5]
        ReviewRequest request = ReviewRequest.builder()
                .experienceId(UUID.randomUUID())
                .rating(invalidRating)
                .comment("test comment")
                .build();

        Set<ConstraintViolation<ReviewRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("rating"));
    }

    @Property(tries = 100)
    void ratingAboveFiveShouldFailValidation(
            @ForAll @IntRange(min = 6, max = Integer.MAX_VALUE) int invalidRating
    ) {
        // Feature: smart-tourism-backend, Property 11: Rating dentro del rango válido [1, 5]
        ReviewRequest request = ReviewRequest.builder()
                .experienceId(UUID.randomUUID())
                .rating(invalidRating)
                .comment("test comment")
                .build();

        Set<ConstraintViolation<ReviewRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("rating"));
    }

    @Property(tries = 100)
    void ratingBetweenOneAndFiveShouldPassValidation(
            @ForAll @IntRange(min = 1, max = 5) int validRating
    ) {
        // Feature: smart-tourism-backend, Property 11: Rating dentro del rango válido [1, 5]
        ReviewRequest request = ReviewRequest.builder()
                .experienceId(UUID.randomUUID())
                .rating(validRating)
                .comment("test comment")
                .build();

        Set<ConstraintViolation<ReviewRequest>> violations = validator.validate(request);

        boolean hasRatingViolation = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("rating"));
        assertThat(hasRatingViolation).isFalse();
    }
}
