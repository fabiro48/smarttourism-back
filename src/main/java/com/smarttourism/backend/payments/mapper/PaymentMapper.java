package com.smarttourism.backend.payments.mapper;

import com.smarttourism.backend.payments.dto.PaymentResponse;
import com.smarttourism.backend.payments.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for {@link Payment} ↔ DTO conversions.
 *
 * <p>The component model is set to {@code spring} via the global compiler argument
 * {@code -Amapstruct.defaultComponentModel=spring}, so this mapper is available
 * as a Spring bean.
 *
 * <p>Validates: Requirements 7.1, 7.6
 */
@Mapper
public interface PaymentMapper {

    /**
     * Maps a {@link Payment} entity to a {@link PaymentResponse} DTO.
     *
     * <p>Flattened reservation fields are sourced from the nested {@code reservation}
     * association; flattened tourist fields from {@code reservation.tourist};
     * and flattened experience fields from {@code reservation.experience}.
     *
     * @param payment the entity to map
     * @return the response DTO
     */
    @Mapping(target = "paymentStatus",       source = "status")
    @Mapping(target = "reservationId",       source = "reservation.id")
    @Mapping(target = "reservationStatus",   source = "reservation.status")
    @Mapping(target = "touristId",           source = "reservation.tourist.id")
    @Mapping(target = "touristName",         source = "reservation.tourist.fullName")
    @Mapping(target = "touristEmail",        source = "reservation.tourist.email")
    @Mapping(target = "experienceId",        source = "reservation.experience.id")
    @Mapping(target = "experienceTitle",     source = "reservation.experience.title")
    @Mapping(target = "experienceLocation",  source = "reservation.experience.location")
    @Mapping(target = "reservationDate",     source = "reservation.reservationDate")
    @Mapping(target = "quantity",            source = "reservation.quantity")
    @Mapping(target = "totalAmount",         source = "reservation.totalAmount")
    @Mapping(target = "expirationDate",      source = "reservation.expirationDate")
    PaymentResponse toResponse(Payment payment);
}
