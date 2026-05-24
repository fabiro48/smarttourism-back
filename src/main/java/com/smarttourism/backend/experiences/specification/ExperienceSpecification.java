package com.smarttourism.backend.experiences.specification;

import com.smarttourism.backend.common.enums.Difficulty;
import com.smarttourism.backend.experiences.dto.ExperienceFilterParams;
import com.smarttourism.backend.experiences.entity.Experience;
import com.smarttourism.backend.schedules.entity.Schedule;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA Specification for dynamic filtering of {@link Experience} entities.
 *
 * <p>All predicates are combined with AND, so only experiences satisfying
 * every provided filter are returned. Null filter values are ignored.</p>
 *
 * <p>Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6</p>
 */
public class ExperienceSpecification {

    private ExperienceSpecification() {
        // Utility class — no instantiation
    }

    /**
     * Builds a {@link Specification} from the given filter parameters.
     * Only active experiences are ever included (active = true).
     *
     * @param params filter parameters (all fields optional)
     * @return a composed Specification combining all non-null predicates with AND
     */
    public static Specification<Experience> withFilters(ExperienceFilterParams params) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always restrict to active experiences
            predicates.add(cb.isTrue(root.get("active")));

            if (params == null) {
                return cb.and(predicates.toArray(new Predicate[0]));
            }

            // 4.1 — category: exact match
            String category = params.getCategory();
            if (category != null && !category.isBlank()) {
                predicates.add(cb.equal(root.get("category"), category));
            }

            // 4.2 — location: case-insensitive LIKE (partial match)
            String location = params.getLocation();
            if (location != null && !location.isBlank()) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("location")),
                                "%" + location.toLowerCase() + "%"
                        )
                );
            }

            // 4.3 — difficulty: exact match using Difficulty enum
            Difficulty difficulty = params.getDifficulty();
            if (difficulty != null) {
                predicates.add(cb.equal(root.get("difficulty"), difficulty));
            }

            // 4.4 — minPrice / maxPrice: inclusive range
            BigDecimal minPrice = params.getMinPrice();
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            BigDecimal maxPrice = params.getMaxPrice();
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            // 4.5 — available: join with schedules where active=true AND availableSlots > 0
            Boolean available = params.getAvailable();
            if (Boolean.TRUE.equals(available)) {
                // Use a subquery to avoid duplicates caused by multiple matching schedules
                Subquery<UUID> subquery = query.subquery(UUID.class);
                Root<Schedule> scheduleRoot = subquery.from(Schedule.class);
                subquery.select(scheduleRoot.get("experience").get("id"))
                        .where(
                                cb.equal(scheduleRoot.get("experience").get("id"), root.get("id")),
                                cb.isTrue(scheduleRoot.get("active")),
                                cb.greaterThan(scheduleRoot.get("availableSlots"), 0)
                        );
                predicates.add(cb.exists(subquery));
            }

            // 4.6 — combine all predicates with AND
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    // -------------------------------------------------------------------------
    // Individual predicate factories (useful for composing specs externally)
    // -------------------------------------------------------------------------

    /** Predicate: category equals the given value (exact, case-sensitive). */
    public static Specification<Experience> hasCategory(String category) {
        return (root, query, cb) -> cb.equal(root.get("category"), category);
    }

    /** Predicate: location contains the given value (case-insensitive LIKE). */
    public static Specification<Experience> locationContains(String location) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("location")), "%" + location.toLowerCase() + "%");
    }

    /** Predicate: difficulty equals the given enum value. */
    public static Specification<Experience> hasDifficulty(Difficulty difficulty) {
        return (root, query, cb) -> cb.equal(root.get("difficulty"), difficulty);
    }

    /** Predicate: price >= minPrice. */
    public static Specification<Experience> priceGreaterThanOrEqual(BigDecimal minPrice) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    /** Predicate: price <= maxPrice. */
    public static Specification<Experience> priceLessThanOrEqual(BigDecimal maxPrice) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), maxPrice);
    }

    /** Predicate: experience has at least one active schedule with availableSlots > 0. */
    public static Specification<Experience> isAvailable() {
        return (root, query, cb) -> {
            Subquery<UUID> subquery = query.subquery(UUID.class);
            Root<Schedule> scheduleRoot = subquery.from(Schedule.class);
            subquery.select(scheduleRoot.get("experience").get("id"))
                    .where(
                            cb.equal(scheduleRoot.get("experience").get("id"), root.get("id")),
                            cb.isTrue(scheduleRoot.get("active")),
                            cb.greaterThan(scheduleRoot.get("availableSlots"), 0)
                    );
            return cb.exists(subquery);
        };
    }

    /** Predicate: active = true. */
    public static Specification<Experience> isActive() {
        return (root, query, cb) -> cb.isTrue(root.get("active"));
    }
}
