package com.smarttourism.backend.reservations.service;

import com.smarttourism.backend.common.enums.ReservationStatus;
import com.smarttourism.backend.notifications.service.NotificationService;
import com.smarttourism.backend.reservations.entity.Reservation;
import com.smarttourism.backend.reservations.mapper.ReservationMapper;
import com.smarttourism.backend.reservations.repository.ReservationRepository;
import com.smarttourism.backend.schedules.entity.Schedule;
import com.smarttourism.backend.schedules.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service responsible for automatically expiring reservations that have exceeded their payment deadline.
 *
 * <p>Validates: Requirements 6.2, 6.3
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExpirationService {

    private final ReservationRepository reservationRepository;
    private final ScheduleRepository scheduleRepository;
    private final ReservationMapper reservationMapper;
    private final NotificationService notificationService;

    /**
     * Scheduled task that runs every 60 seconds to expire reservations with status PENDING_PAYMENT
     * and expirationDate in the past.
     *
     * <p>For each expired reservation:
     * <ul>
     *   <li>Changes status to EXPIRED</li>
     *   <li>Restores availableSlots in the associated Schedule</li>
     *   <li>Triggers async expiration email via {@link NotificationService}</li>
     * </ul>
     *
     * <p>Validates: Requirements 6.2, 6.3
     */
    @Scheduled(fixedDelay = 60000)
    public void expireReservations() {
        log.debug("Running expiration check for reservations");

        LocalDateTime now = LocalDateTime.now();
        List<Reservation> expiredReservations = reservationRepository
                .findByStatusAndExpirationDateBefore(ReservationStatus.PENDING_PAYMENT, now);

        if (expiredReservations.isEmpty()) {
            log.debug("No reservations to expire");
            return;
        }

        log.info("Found {} reservations to expire", expiredReservations.size());

        for (Reservation reservation : expiredReservations) {
            try {
                expireReservation(reservation);
            } catch (Exception e) {
                log.error("Error expiring reservation {}: {}", reservation.getId(), e.getMessage(), e);
            }
        }
    }

    /**
     * Expires a single reservation within a transaction.
     *
     * @param reservation the reservation to expire
     */
    @Transactional
    protected void expireReservation(Reservation reservation) {
        // Change status to EXPIRED
        reservation.setStatus(ReservationStatus.EXPIRED);
        reservationRepository.save(reservation);

        // Restore availableSlots in the Schedule
        Schedule schedule = reservation.getSchedule();
        schedule.setAvailableSlots(schedule.getAvailableSlots() + reservation.getQuantity());
        scheduleRepository.save(schedule);

        log.info("Expired reservation {} and restored {} slots to schedule {}",
                reservation.getId(), reservation.getQuantity(), schedule.getId());

        notificationService.sendExpirationEmail(
                reservation.getTourist().getEmail(),
                reservationMapper.toResponse(reservation)
        );
    }
}
