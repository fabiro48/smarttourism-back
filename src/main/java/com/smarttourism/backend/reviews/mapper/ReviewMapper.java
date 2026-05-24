package com.smarttourism.backend.reviews.mapper;

import com.smarttourism.backend.reviews.dto.ReviewResponse;
import com.smarttourism.backend.reviews.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for {@link Review} ↔ DTO conversions.
 *
 * <p>The component model is set to {@code spring} via the global compiler argument
 * {@code -Amapstruct.defaultComponentModel=spring}, so this mapper is available
 * as a Spring bean.
 *
 * <p>Note: there is no {@code toEntity} method here because reviews are
 * never created from a DTO directly — the service layer builds the entity
 * manually to apply business rules (verification of confirmed reservation,
 * uniqueness check).
 *
 * <p>Validates: Requirements 8.1, 8.2
 */
@Mapper
public interface ReviewMapper {

    /**
     * Maps a {@link Review} entity to a {@link ReviewResponse} DTO.
     *
     * <p>Flattened tourist fields are sourced from the nested {@code tourist}
     * association, and flattened experience fields from {@code experience}.
     *
     * @param review the entity to map
     * @return the response DTO
     */
    @Mapping(target = "touristId",        source = "tourist.id")
    @Mapping(target = "touristName",      source = "tourist.fullName")
    @Mapping(target = "touristEmail",     source = "tourist.email")
    @Mapping(target = "experienceId",     source = "experience.id")
    @Mapping(target = "experienceTitle",  source = "experience.title")
    ReviewResponse toResponse(Review review);
}
