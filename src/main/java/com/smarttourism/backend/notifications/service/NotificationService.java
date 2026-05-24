package com.smarttourism.backend.notifications.service;

import com.smarttourism.backend.payments.dto.PaymentResponse;
import com.smarttourism.backend.reservations.dto.ReservationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service for sending email notifications asynchronously.
 *
 * <p>All email sending operations are annotated with {@code @Async} to avoid
 * blocking the main application flow. Failures are logged but do not propagate
 * exceptions to the caller.
 *
 * <p>Validates: Requirements 10.1, 10.2, 10.3, 10.4, 10.5, 10.6
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Sends an email notification when a reservation is created.
     *
     * <p>Includes reservation details and the 15-minute payment deadline.
     *
     * @param to          recipient email address
     * @param reservation the created reservation
     */
    @Async
    public void sendReservationCreatedEmail(String to, ReservationResponse reservation) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Reserva Creada - Turismo Inteligente Santander");
            message.setText(String.format(
                    "Hola,\n\n" +
                    "Tu reserva ha sido creada exitosamente.\n\n" +
                    "Detalles de la reserva:\n" +
                    "- ID: %s\n" +
                    "- Experiencia: %s\n" +
                    "- Fecha: %s\n" +
                    "- Cantidad: %d personas\n" +
                    "- Monto total: $%.2f\n" +
                    "- Estado: %s\n\n" +
                    "IMPORTANTE: Tienes 15 minutos para completar el pago antes de que la reserva expire.\n" +
                    "Fecha de expiración: %s\n\n" +
                    "Saludos,\n" +
                    "Equipo de Turismo Inteligente Santander",
                    reservation.getId(),
                    reservation.getExperienceTitle(),
                    reservation.getReservationDate(),
                    reservation.getQuantity(),
                    reservation.getTotalAmount(),
                    reservation.getStatus(),
                    reservation.getExpirationDate()
            ));

            mailSender.send(message);
            log.info("Reservation created email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send reservation created email to: {}", to, e);
        }
    }

    /**
     * Sends an email notification with the payment result.
     *
     * @param to          recipient email address
     * @param payment     the payment result
     * @param reservation the associated reservation
     */
    @Async
    public void sendPaymentResultEmail(String to, PaymentResponse payment, ReservationResponse reservation) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);

            if ("APPROVED".equals(payment.getPaymentStatus() != null ? payment.getPaymentStatus().name() : "")) {
                message.setSubject("Pago Aprobado - Turismo Inteligente Santander");
                message.setText(String.format(
                        "Hola,\n\n" +
                        "¡Tu pago ha sido aprobado exitosamente!\n\n" +
                        "Detalles del pago:\n" +
                        "- Referencia de transacción: %s\n" +
                        "- Monto: $%.2f\n" +
                        "- Estado: %s\n\n" +
                        "Tu reserva ha sido confirmada:\n" +
                        "- ID de reserva: %s\n" +
                        "- Experiencia: %s\n" +
                        "- Fecha: %s\n" +
                        "- Cantidad: %d personas\n\n" +
                        "¡Disfruta tu experiencia!\n\n" +
                        "Saludos,\n" +
                        "Equipo de Turismo Inteligente Santander",
                        payment.getTransactionReference(),
                        payment.getAmount(),
                        payment.getPaymentStatus(),
                        reservation.getId(),
                        reservation.getExperienceTitle(),
                        reservation.getReservationDate(),
                        reservation.getQuantity()
                ));
            } else {
                message.setSubject("Pago Rechazado - Turismo Inteligente Santander");
                message.setText(String.format(
                        "Hola,\n\n" +
                        "Lamentablemente, tu pago ha sido rechazado.\n\n" +
                        "Detalles del pago:\n" +
                        "- Referencia de transacción: %s\n" +
                        "- Monto: $%.2f\n" +
                        "- Estado: %s\n\n" +
                        "Tu reserva sigue en estado PENDING_PAYMENT. Puedes intentar realizar el pago nuevamente.\n\n" +
                        "Saludos,\n" +
                        "Equipo de Turismo Inteligente Santander",
                        payment.getTransactionReference(),
                        payment.getAmount(),
                        payment.getPaymentStatus()
                ));
            }

            mailSender.send(message);
            log.info("Payment result email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send payment result email to: {}", to, e);
        }
    }

    /**
     * Sends an email notification when a reservation is cancelled.
     *
     * @param to                 recipient email address
     * @param reservation        the cancelled reservation
     * @param eligibleForRefund  whether the user is eligible for a partial refund
     */
    @Async
    public void sendCancellationEmail(String to, ReservationResponse reservation, boolean eligibleForRefund) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Reserva Cancelada - Turismo Inteligente Santander");

            String refundMessage = eligibleForRefund
                    ? "Eres elegible para un reembolso parcial del 30% del monto total. El reembolso será procesado en los próximos días."
                    : "No eres elegible para reembolso según las políticas de cancelación.";

            message.setText(String.format(
                    "Hola,\n\n" +
                    "Tu reserva ha sido cancelada.\n\n" +
                    "Detalles de la reserva cancelada:\n" +
                    "- ID: %s\n" +
                    "- Experiencia: %s\n" +
                    "- Fecha: %s\n" +
                    "- Cantidad: %d personas\n" +
                    "- Monto total: $%.2f\n\n" +
                    "%s\n\n" +
                    "Saludos,\n" +
                    "Equipo de Turismo Inteligente Santander",
                    reservation.getId(),
                    reservation.getExperienceTitle(),
                    reservation.getReservationDate(),
                    reservation.getQuantity(),
                    reservation.getTotalAmount(),
                    refundMessage
            ));

            mailSender.send(message);
            log.info("Cancellation email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send cancellation email to: {}", to, e);
        }
    }

    /**
     * Sends an email notification when a reservation expires.
     *
     * @param to          recipient email address
     * @param reservation the expired reservation
     */
    @Async
    public void sendExpirationEmail(String to, ReservationResponse reservation) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Reserva Expirada - Turismo Inteligente Santander");
            message.setText(String.format(
                    "Hola,\n\n" +
                    "Tu reserva ha expirado debido a que no se completó el pago dentro del tiempo límite de 15 minutos.\n\n" +
                    "Detalles de la reserva expirada:\n" +
                    "- ID: %s\n" +
                    "- Experiencia: %s\n" +
                    "- Fecha: %s\n" +
                    "- Cantidad: %d personas\n" +
                    "- Monto total: $%.2f\n\n" +
                    "Los cupos han sido liberados y están disponibles nuevamente.\n" +
                    "Puedes crear una nueva reserva si aún estás interesado.\n\n" +
                    "Saludos,\n" +
                    "Equipo de Turismo Inteligente Santander",
                    reservation.getId(),
                    reservation.getExperienceTitle(),
                    reservation.getReservationDate(),
                    reservation.getQuantity(),
                    reservation.getTotalAmount()
            ));

            mailSender.send(message);
            log.info("Expiration email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send expiration email to: {}", to, e);
        }
    }
}
