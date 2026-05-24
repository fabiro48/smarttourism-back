package com.smarttourism.backend.reservations.repository;

import com.smarttourism.backend.common.enums.ReservationStatus;
import com.smarttourism.backend.reservations.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID>, JpaSpecificationExecutor<Reservation> {

    List<Reservation> findByTouristIdOrderByCreatedAtDesc(UUID touristId);

    List<Reservation> findByStatusAndExpirationDateBefore(ReservationStatus status, LocalDateTime dateTime);
}
