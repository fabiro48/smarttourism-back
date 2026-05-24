package com.smarttourism.backend.experiences.mapper;

import com.smarttourism.backend.experiences.dto.ExperienceRequest;
import com.smarttourism.backend.experiences.dto.ExperienceResponse;
import com.smarttourism.backend.experiences.entity.Experience;
import com.smarttourism.backend.schedules.dto.ScheduleResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * MapStruct mapper for {@link Experience} ↔ DTO conversions.
 *
 * <p>The component model is set to {@code spring} via the global compiler argument
 * {@code -Amapstruct.defaultComponentModel=spring}, so this mapper is available
 * as a Spring bean.
 *
 * <p>Validates: Requirements 3.1, 3.2, 8.5
 */
@Mapper
public interface ExperienceMapper {

    // ── ExperienceRequest → Experience ────────────────────────────────────────

    /**
     * Maps a creation/update request to a new {@link Experience} entity.
     *
     * <p>Fields managed by the persistence layer ({@code id}, {@code active},
     * {@code createdAt}, {@code updatedAt}) are intentionally ignored so that
     * the service layer can set them explicitly.
     *
     * @param request the incoming request DTO
     * @return a new (unpersisted) {@link Experience} entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "images", source = "images", qualifiedByName = "listToArray")
    Experience toEntity(ExperienceRequest request);

    /**
     * Updates an existing {@link Experience} entity in-place from a request DTO.
     *
     * @param request the incoming request DTO
     * @param entity  the entity to update (modified in-place)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "images", source = "images", qualifiedByName = "listToArray")
    void updateEntityFromRequest(ExperienceRequest request, @MappingTarget Experience entity);

    // ── Experience → ExperienceResponse ───────────────────────────────────────

    /**
     * Maps an {@link Experience} entity to a response DTO.
     *
     * <p>The computed fields {@code averageRating}, {@code reviewCount} and
     * {@code schedules} are not present on the entity; they must be set by the
     * caller after this method returns (e.g. in {@code ExperienceService}).
     *
     * @param experience the entity to map
     * @return a response DTO with {@code averageRating}, {@code reviewCount}
     *         and {@code schedules} left as {@code null}
     */
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "reviewCount", ignore = true)
    @Mapping(target = "schedules", ignore = true)
    @Mapping(target = "images", source = "images", qualifiedByName = "arrayToList")
    ExperienceResponse toResponse(Experience experience);

    /**
     * Convenience overload that also sets the aggregated review statistics and
     * the list of schedules in a single call.
     *
     * @param experience    the entity to map
     * @param averageRating average rating across all reviews ({@code null} if none)
     * @param reviewCount   total number of reviews
     * @param schedules     active schedules for this experience
     * @return a fully-populated response DTO
     */
    default ExperienceResponse toResponse(Experience experience,
                                          Double averageRating,
                                          Integer reviewCount,
                                          List<ScheduleResponse> schedules) {
        ExperienceResponse response = toResponse(experience);
        response.setAverageRating(averageRating);
        response.setReviewCount(reviewCount);
        response.setSchedules(schedules);
        return response;
    }

    // ── Type-conversion helpers ────────────────────────────────────────────────

    /**
     * Converts a {@code String[]} (stored in the entity) to a {@code List<String>}
     * (used in the response DTO).
     */
    @Named("arrayToList")
    default List<String> arrayToList(String[] array) {
        if (array == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(array);
    }

    /**
     * Converts a {@code List<String>} (received in the request DTO) to a
     * {@code String[]} (stored in the entity).
     */
    @Named("listToArray")
    default String[] listToArray(List<String> list) {
        if (list == null) {
            return new String[0];
        }
        return list.toArray(new String[0]);
    }
}
