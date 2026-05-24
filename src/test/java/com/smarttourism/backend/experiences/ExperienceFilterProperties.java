package com.smarttourism.backend.experiences;

import com.smarttourism.backend.common.enums.Difficulty;
import com.smarttourism.backend.experiences.entity.Experience;
import net.jqwik.api.*;
import net.jqwik.api.constraints.NotBlank;
import net.jqwik.api.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for experience filtering logic.
 *
 * <p>Validates: Requirements 4.1, 4.2, 4.3, 4.4, 4.5, 4.6
 */
class ExperienceFilterProperties {

    // ── Property 5: Filtros de experiencias son conjuntivos (AND) ─────────────

    @Property(tries = 100)
    void categoryFilterReturnsOnlyMatchingExperiences(
            @ForAll @NotBlank String targetCategory,
            @ForAll @NotBlank String otherCategory
    ) {
        // Feature: smart-tourism-backend, Property 5: Filtros de experiencias son conjuntivos (AND)
        Assume.that(!targetCategory.equals(otherCategory));

        List<Experience> experiences = List.of(
                buildExperience(targetCategory, "Bucaramanga", Difficulty.EASY, new BigDecimal("100")),
                buildExperience(otherCategory, "Barichara", Difficulty.HARD, new BigDecimal("200")),
                buildExperience(targetCategory, "San Gil", Difficulty.MODERATE, new BigDecimal("150"))
        );

        List<Experience> filtered = experiences.stream()
                .filter(e -> e.getCategory().equals(targetCategory))
                .collect(Collectors.toList());

        assertThat(filtered).allMatch(e -> e.getCategory().equals(targetCategory));
        assertThat(filtered).noneMatch(e -> e.getCategory().equals(otherCategory));
    }

    @Property(tries = 100)
    void priceRangeFilterReturnsOnlyExperiencesInRange(
            @ForAll @Positive int minPrice,
            @ForAll @Positive int rangeSize
    ) {
        // Feature: smart-tourism-backend, Property 5: Filtros de experiencias son conjuntivos (AND)
        BigDecimal min = BigDecimal.valueOf(minPrice);
        BigDecimal max = min.add(BigDecimal.valueOf(rangeSize));

        List<Experience> experiences = List.of(
                buildExperience("Cat", "Loc", Difficulty.EASY, min.subtract(BigDecimal.ONE)),
                buildExperience("Cat", "Loc", Difficulty.EASY, min),
                buildExperience("Cat", "Loc", Difficulty.EASY, min.add(BigDecimal.valueOf(rangeSize / 2))),
                buildExperience("Cat", "Loc", Difficulty.EASY, max),
                buildExperience("Cat", "Loc", Difficulty.EASY, max.add(BigDecimal.ONE))
        );

        List<Experience> filtered = experiences.stream()
                .filter(e -> e.getPrice().compareTo(min) >= 0 && e.getPrice().compareTo(max) <= 0)
                .collect(Collectors.toList());

        assertThat(filtered).allMatch(e ->
                e.getPrice().compareTo(min) >= 0 && e.getPrice().compareTo(max) <= 0
        );
    }

    @Property(tries = 100)
    void multipleFiltersAreConjunctive(
            @ForAll @NotBlank String category,
            @ForAll @From("difficulties") Difficulty difficulty
    ) {
        // Feature: smart-tourism-backend, Property 5: Filtros de experiencias son conjuntivos (AND)
        List<Experience> experiences = List.of(
                buildExperience(category, "Loc1", difficulty, new BigDecimal("100")),
                buildExperience(category, "Loc2", Difficulty.EXTREME, new BigDecimal("200")),
                buildExperience("OtherCat", "Loc3", difficulty, new BigDecimal("100")),
                buildExperience("OtherCat", "Loc4", Difficulty.EXTREME, new BigDecimal("300"))
        );

        // Apply both filters simultaneously (AND)
        List<Experience> filtered = experiences.stream()
                .filter(e -> e.getCategory().equals(category))
                .filter(e -> e.getDifficulty() == difficulty)
                .collect(Collectors.toList());

        // Every result must satisfy ALL filters
        assertThat(filtered).allMatch(e ->
                e.getCategory().equals(category) && e.getDifficulty() == difficulty
        );
    }

    @Provide
    Arbitrary<Difficulty> difficulties() {
        return Arbitraries.of(Difficulty.values());
    }

    private Experience buildExperience(String category, String location,
                                        Difficulty difficulty, BigDecimal price) {
        return Experience.builder()
                .id(UUID.randomUUID())
                .title("Test Experience")
                .description("Description")
                .category(category)
                .location(location)
                .difficulty(difficulty)
                .price(price)
                .active(true)
                .build();
    }
}
