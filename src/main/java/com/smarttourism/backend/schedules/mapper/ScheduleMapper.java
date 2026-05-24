package com.smarttourism.backend.schedules.mapper;

import com.smarttourism.backend.schedules.dto.ScheduleRequest;
import com.smarttourism.backend.schedules.dto.ScheduleResponse;
import com.smarttourism.backend.schedules.entity.Schedule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * MapStruct mapper for {@link Schedule} ↔ DTO conversions.
 *
 * <p>The component model is set to {@code spring} via the global compiler argument
 * {@code -Amapstruct.defaultComponentModel=spring}, so this mapper is available
 * as a Spring bean.
 *
 * <p>Validates: Requirements 9.5
 */
@Mapper
public interface ScheduleMapper {

    // ── ScheduleRequest → Schedule ────────────────────────────────────────────

    /**
     * Maps a creation request to a new {@link Schedule} entity.
     *
     * <p>Fields managed by the persistence layer ({@code id}, {@code experience},
     * {@code active}) are intentionally ignored so that the service layer can set
     * them explicitly.
     *
     * @param request the incoming request DTO
     * @return a new (unpersisted) {@link Schedule} entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "experience", ignore = true)
    @Mapping(target = "active", ignore = true)
    Schedule toEntity(ScheduleRequest request);

    /**
     * Updates an existing {@link Schedule} entity in-place from a request DTO.
     *
     * @param request the incoming request DTO
     * @param entity  the entity to update (modified in-place)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "experience", ignore = true)
    @Mapping(target = "active", ignore = true)
    void updateEntityFromRequest(ScheduleRequest request, @MappingTarget Schedule entity);

    // ── Schedule → ScheduleResponse ───────────────────────────────────────────

    /**
     * Maps a {@link Schedule} entity to a response DTO.
     *
     * @param schedule the entity to map
     * @return the response DTO
     */
    ScheduleResponse toResponse(Schedule schedule);
}
