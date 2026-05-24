package com.smarttourism.backend.reservations.service;

import com.smarttourism.backend.common.enums.ReservationStatus;
import com.smarttourism.backend.reservations.entity.Reservation;
import com.smarttourism.backend.reservations.repository.ReservationRepository;
import com.smarttourism.backend.schedules.entity.Schedule;
import com.smarttourism.backend.schedules.repository.ScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ExpirationService}.
 *
 * <p>Validates: Requirements 6.2, 6.3
 */
@ExtendWith(MockitoExtension.class)
class ExpirationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ScheduleRepository scheduleRepository;

    @InjectMocks
    private ExpirationService expirationService;

    private Reservation expiredReservation;
    private Schedule schedule;

    @BeforeEach
    void setUp() {
        schedule = Schedule.builder()
                .id(UUID.randomUUID())
                .availableSlots(5)
                .build();

        expiredReservation = Reservation.builder()
                .id(UUID.randomUUID())
                .status(ReservationStatus.PENDING_PAYMENT)
                .expirationDate(LocalDateTime.now().minusMinutes(5))
                .quantity(2)
                .schedule(schedule)
                .build();
    }

    @Test
    void expireReservations_shouldExpireReservationsAndRestoreSlots() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        List<Reservation> expiredReservations = Arrays.asList(expiredReservation);
        
        when(reservationRepository.findByStatusAndExpirationDateBefore(
                eq(ReservationStatus.PENDING_PAYMENT), any(LocalDateTime.class)))
                .thenReturn(expiredReservations);

        // When
        expirationService.expireReservations();

        // Then
        verify(reservationRepository).findByStatusAndExpirationDateBefore(
                eq(ReservationStatus.PENDING_PAYMENT), any(LocalDateTime.class));
        verify(reservationRepository).save(expiredReservation);
        verify(scheduleRepository).save(schedule);
        
        // Verify reservation status changed to EXPIRED
        assert expiredReservation.getStatus() == ReservationStatus.EXPIRED;
        
        // Verify slots were restored (5 + 2 = 7)
        assert schedule.getAvailableSlots() == 7;
    }

    @Test
    void expireReservations_shouldHandleNoExpiredReservations() {
        // Given
        when(reservationRepository.findByStatusAndExpirationDateBefore(
                eq(ReservationStatus.PENDING_PAYMENT), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        // When
        expirationService.expireReservations();

        // Then
        verify(reservationRepository).findByStatusAndExpirationDateBefore(
                eq(ReservationStatus.PENDING_PAYMENT), any(LocalDateTime.class));
        verify(reservationRepository, never()).save(any());
        verify(scheduleRepository, never()).save(any());
    }

    @Test
    void expireReservations_shouldContinueOnError() {
        // Given
        Reservation reservation1 = Reservation.builder()
                .id(UUID.randomUUID())
                .status(ReservationStatus.PENDING_PAYMENT)
                .quantity(1)
                .schedule(schedule)
                .build();
        
        Reservation reservation2 = Reservation.builder()
                .id(UUID.randomUUID())
                .status(ReservationStatus.PENDING_PAYMENT)
                .quantity(3)
                .schedule(schedule)
                .build();

        List<Reservation> expiredReservations = Arrays.asList(reservation1, reservation2);
        
        when(reservationRepository.findByStatusAndExpirationDateBefore(
                eq(ReservationStatus.PENDING_PAYMENT), any(LocalDateTime.class)))
                .thenReturn(expiredReservations);
        
        // Simulate error on first reservation
        doThrow(new RuntimeException("Database error"))
                .when(reservationRepository).save(reservation1);

        // When
        expirationService.expireReservations();

        // Then - should still process second reservation
        verify(reservationRepository, times(2)).save(any(Reservation.class));
    }

    @Test
    void expireReservation_shouldUpdateReservationStatusToExpired() {
        // When
        expirationService.expireReservation(expiredReservation);

        // Then
        verify(reservationRepository).save(expiredReservation);
        assert expiredReservation.getStatus() == ReservationStatus.EXPIRED;
    }

    @Test
    void expireReservation_shouldRestoreAvailableSlotsInSchedule() {
        // Given
        int initialSlots = schedule.getAvailableSlots();
        int quantity = expiredReservation.getQuantity();

        // When
        expirationService.expireReservation(expiredReservation);

        // Then
        verify(scheduleRepository).save(schedule);
        assert schedule.getAvailableSlots() == initialSlots + quantity;
    }
}
