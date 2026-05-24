package com.smarttourism.backend.reservations.mapper;

import com.smarttourism.backend.reservations.dto.ReservationResponse;
import com.smarttourism.backend.reservations.entity.Reservation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for {@link Reservation} ↔ DTO conversions.
 *
 * <p>The component model is set to {@code spring} via the global compiler argument
 * {@code -Amapstruct.defaultComponentModel=spring}, so this mapper is available
 * as a Spring bean.
 *
 * <p>Note: there is no {@code toEntity} method here because reservations are
 * never created from a DTO directly — the service layer builds the entity
 * manually to apply business rules (slot decrement, totalAmount calculation,
 * expirationDate assignment).
 *
 * <p>Validates: Requirements 5.1, 5.2
 */
@Mapper
public interface ReservationMapper {

    /**
     * Maps a {@link Reservation} entity to a {@link ReservationResponse} DTO.
     *
     * <p>Flattened tourist fields are sourced from the nested {@code tourist}
     * association; flattened experience fields from {@code experience}; and the
     * schedule identifier from {@code schedule}.
     *
     * @param reservation the entity to map
     * @return the response DTO
     */
    @Mapping(target = "touristId",          source = "tourist.id")
    @Mapping(target = "touristName",        source = "tourist.fullName")
    @Mapping(target = "touristEmail",       source = "tourist.email")
    @Mapping(target = "experienceId",       source = "experience.id")
    @Mapping(target = "experienceTitle",    source = "experience.title")
    @Mapping(target = "experienceLocation", source = "experience.location")
    @Mapping(target = "scheduleId",         source = "schedule.id")
    ReservationResponse toResponse(Reservation reservation);
}
