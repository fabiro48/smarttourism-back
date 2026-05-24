package com.smarttourism.backend.reservations.service;

import com.smarttourism.backend.common.enums.ReservationStatus;
import com.smarttourism.backend.common.exception.InsufficientSlotsException;
import com.smarttourism.backend.common.exception.InvalidReservationStateException;
import com.smarttourism.backend.common.exception.ResourceNotFoundException;
import com.smarttourism.backend.common.exception.UnauthorizedAccessException;
import com.smarttourism.backend.experiences.entity.Experience;
import com.smarttourism.backend.experiences.repository.ExperienceRepository;
import com.smarttourism.backend.notifications.service.NotificationService;
import com.smarttourism.backend.reservations.dto.ReservationRequest;
import com.smarttourism.backend.reservations.dto.ReservationResponse;
import com.smarttourism.backend.reservations.entity.Reservation;
import com.smarttourism.backend.reservations.mapper.ReservationMapper;
import com.smarttourism.backend.reservations.repository.ReservationRepository;
import com.smarttourism.backend.schedules.entity.Schedule;
import com.smarttourism.backend.schedules.repository.ScheduleRepository;
import com.smarttourism.backend.users.entity.User;
import com.smarttourism.backend.users.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service layer for managing {@link Reservation} entities.
 *
 * <p>Implements the core business logic for reservation creation, retrieval,
 * and cancellation, including:
 * <ul>
 *   <li>Pessimistic locking (SELECT FOR UPDATE) to prevent overbooking</li>
 *   <li>Slot availability verification and decrement</li>
 *   <li>Total amount and expiration date calculation</li>
 *   <li>Ownership verification for cancellation</li>
 *   <li>State validation for cancellable reservations</li>
 *   <li>Slot restoration on cancellation</li>
 * </ul>
 *
 * <p>Validates: Requirements 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 5.7, 5.8, 6.1, 6.4, 6.5, 6.6
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ScheduleRepository scheduleRepository;
    private final ExperienceRepository experienceRepository;
    private final UserRepository userRepository;
    private final ReservationMapper reservationMapper;
    private final EntityManager entityManager;
    private final NotificationService notificationService;

    /**
     * Creates a new reservation for the given tourist.
     *
     * <p>This method:
     * <ol>
     *   <li>Acquires a pessimistic lock on the schedule (SELECT FOR UPDATE)</li>
     *   <li>Verifies that {@code availableSlots >= quantity}</li>
     *   <li>Decrements {@code availableSlots} by {@code quantity}</li>
     *   <li>Calculates {@code totalAmount = price * quantity}</li>
     *   <li>Calculates {@code expirationDate = now() + 15 minutes}</li>
     *   <li>Creates the reservation with status {@code PENDING_PAYMENT}</li>
     * </ol>
     *
     * <p>The pessimistic lock ensures that concurrent reservation attempts on the
     * same schedule do not cause overbooking (Requirement 6.1).
     *
     * @param touristId the UUID of the tourist making the reservation
     * @param request   the reservation request containing experienceId, scheduleId,
     *                  reservationDate, and quantity
     * @return the created reservation as a {@link ReservationResponse}
     * @throws ResourceNotFoundException   if the tourist, experience, or schedule
     *                                     does not exist
     * @throws InsufficientSlotsException if the schedule does not have enough
     *                                     available slots
     */
    @Transactional
    public ReservationResponse createReservation(UUID touristId, ReservationRequest request) {
        log.info("Creating reservation for tourist {} on schedule {}", touristId, request.getScheduleId());

        // 1. Load tourist
        User tourist = userRepository.findById(touristId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", touristId));

        // 2. Load experience
        Experience experience = experienceRepository.findById(request.getExperienceId())
                .orElseThrow(() -> new ResourceNotFoundException("Experiencia", request.getExperienceId()));

        // 3. Load schedule with pessimistic lock (SELECT FOR UPDATE)
        Schedule schedule = entityManager.find(Schedule.class, request.getScheduleId(), LockModeType.PESSIMISTIC_WRITE);
        if (schedule == null) {
            throw new ResourceNotFoundException("Horario", request.getScheduleId());
        }

        // 4. Verify available slots
        if (schedule.getAvailableSlots() < request.getQuantity()) {
            log.warn("Insufficient slots for schedule {}: requested={}, available={}",
                    request.getScheduleId(), request.getQuantity(), schedule.getAvailableSlots());
            throw new InsufficientSlotsException(request.getQuantity(), schedule.getAvailableSlots());
        }

        // 5. Decrement available slots
        schedule.setAvailableSlots(schedule.getAvailableSlots() - request.getQuantity());
        scheduleRepository.save(schedule);

        // 6. Calculate total amount (price * quantity)
        BigDecimal totalAmount = experience.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));

        // 7. Calculate expiration date (now + 15 minutes)
        LocalDateTime expirationDate = LocalDateTime.now().plusMinutes(15);

        // 8. Create reservation with status PENDING_PAYMENT
        Reservation reservation = Reservation.builder()
                .tourist(tourist)
                .experience(experience)
                .schedule(schedule)
                .reservationDate(request.getReservationDate())
                .quantity(request.getQuantity())
                .totalAmount(totalAmount)
                .status(ReservationStatus.PENDING_PAYMENT)
                .expirationDate(expirationDate)
                .build();

        Reservation savedReservation = reservationRepository.save(reservation);
        log.info("Reservation {} created successfully for tourist {}", savedReservation.getId(), touristId);

        ReservationResponse response = reservationMapper.toResponse(savedReservation);
        notificationService.sendReservationCreatedEmail(tourist.getEmail(), response);
        return response;
    }

    /**
     * Retrieves all reservations for the given tourist, ordered by creation date
     * descending (most recent first).
     *
     * @param touristId the UUID of the tourist
     * @return list of reservations as {@link ReservationResponse}, may be empty
     */
    @Transactional(readOnly = true)
    public List<ReservationResponse> getMyReservations(UUID touristId) {
        log.debug("Fetching reservations for tourist {}", touristId);

        List<Reservation> reservations = reservationRepository.findByTouristIdOrderByCreatedAtDesc(touristId);

        return reservations.stream()
                .map(reservationMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Cancels a reservation if it belongs to the given tourist and is in a
     * cancellable state.
     *
     * <p>This method:
     * <ol>
     *   <li>Verifies that the reservation exists</li>
     *   <li>Verifies that the reservation belongs to the given tourist</li>
     *   <li>Verifies that the reservation is in a cancellable state
     *       ({@code PENDING_PAYMENT} or {@code CONFIRMED})</li>
     *   <li>Changes the reservation status to {@code CANCELLED}</li>
     *   <li>Restores the {@code availableSlots} in the associated schedule</li>
     * </ol>
     *
     * @param reservationId the UUID of the reservation to cancel
     * @param touristId     the UUID of the tourist requesting the cancellation
     * @throws ResourceNotFoundException         if the reservation does not exist
     * @throws UnauthorizedAccessException       if the reservation does not belong
     *                                           to the given tourist
     * @throws InvalidReservationStateException if the reservation is not in a
     *                                           cancellable state
     */
    @Transactional
    public void cancelReservation(UUID reservationId, UUID touristId) {
        log.info("Cancelling reservation {} for tourist {}", reservationId, touristId);

        // 1. Load reservation
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", reservationId));

        // 2. Verify ownership
        if (!reservation.getTourist().getId().equals(touristId)) {
            log.warn("Tourist {} attempted to cancel reservation {} owned by {}",
                    touristId, reservationId, reservation.getTourist().getId());
            throw new UnauthorizedAccessException("No tienes permiso para cancelar esta reserva");
        }

        // 3. Verify cancellable state (PENDING_PAYMENT or CONFIRMED)
        if (reservation.getStatus() != ReservationStatus.PENDING_PAYMENT
                && reservation.getStatus() != ReservationStatus.CONFIRMED) {
            log.warn("Attempted to cancel reservation {} in non-cancellable state: {}",
                    reservationId, reservation.getStatus());
            throw new InvalidReservationStateException(reservation.getStatus());
        }

        // 4. Change status to CANCELLED
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);

        // 5. Restore available slots in the schedule
        Schedule schedule = reservation.getSchedule();
        schedule.setAvailableSlots(schedule.getAvailableSlots() + reservation.getQuantity());
        scheduleRepository.save(schedule);

        log.info("Reservation {} cancelled successfully, {} slots restored to schedule {}",
                reservationId, reservation.getQuantity(), schedule.getId());

        // Determine refund eligibility: CONFIRMED + reservationDate >= 2 days from now
        boolean eligibleForRefund = reservation.getStatus() == ReservationStatus.CONFIRMED
                && reservation.getReservationDate() != null
                && !reservation.getReservationDate().isBefore(LocalDate.now().plusDays(2));

        ReservationResponse response = reservationMapper.toResponse(reservation);
        notificationService.sendCancellationEmail(reservation.getTourist().getEmail(), response, eligibleForRefund);
    }
}
