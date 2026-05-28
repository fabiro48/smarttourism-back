package com.smarttourism.backend.payments.controller;

import com.smarttourism.backend.common.enums.PaymentStatus;
import com.smarttourism.backend.common.enums.ReservationStatus;
import com.smarttourism.backend.common.enums.Role;
import com.smarttourism.backend.payments.dto.PaymentResponse;
import com.smarttourism.backend.payments.service.PaymentService;
import com.smarttourism.backend.users.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link PaymentController#getMyPayments(Authentication)}.
 *
 * <p>Validates: Requirements 1.1, 1.2
 */
@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private PaymentController paymentController;

    private User tourist;
    private UUID touristId;

    @BeforeEach
    void setUp() {
        touristId = UUID.randomUUID();
        tourist = User.builder()
                .id(touristId)
                .fullName("Test Tourist")
                .email("tourist@test.com")
                .role(Role.TOURIST)
                .build();
    }

    @Test
    void getMyPayments_withPayments_returnsOkWithList() {
        // Given
        when(authentication.getPrincipal()).thenReturn(tourist);

        PaymentResponse payment1 = PaymentResponse.builder()
                .id(UUID.randomUUID())
                .paymentStatus(PaymentStatus.APPROVED)
                .transactionReference(UUID.randomUUID().toString())
                .amount(BigDecimal.valueOf(150000))
                .reservationId(UUID.randomUUID())
                .reservationStatus(ReservationStatus.CONFIRMED)
                .touristId(touristId)
                .touristName("Test Tourist")
                .touristEmail("tourist@test.com")
                .experienceId(UUID.randomUUID())
                .experienceTitle("City Tour Santander")
                .experienceLocation("Santander")
                .reservationDate(LocalDate.now())
                .quantity(2)
                .totalAmount(BigDecimal.valueOf(150000))
                .expirationDate(LocalDateTime.now().plusHours(24))
                .createdAt(LocalDateTime.now())
                .build();

        PaymentResponse payment2 = PaymentResponse.builder()
                .id(UUID.randomUUID())
                .paymentStatus(PaymentStatus.REJECTED)
                .transactionReference(UUID.randomUUID().toString())
                .amount(BigDecimal.valueOf(80000))
                .reservationId(UUID.randomUUID())
                .reservationStatus(ReservationStatus.PENDING_PAYMENT)
                .touristId(touristId)
                .touristName("Test Tourist")
                .touristEmail("tourist@test.com")
                .experienceId(UUID.randomUUID())
                .experienceTitle("Museo del Oro")
                .experienceLocation("Bucaramanga")
                .reservationDate(LocalDate.now().plusDays(3))
                .quantity(1)
                .totalAmount(BigDecimal.valueOf(80000))
                .expirationDate(LocalDateTime.now().plusHours(24))
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();

        List<PaymentResponse> payments = List.of(payment1, payment2);
        when(paymentService.getPaymentsByTourist(touristId)).thenReturn(payments);

        // When
        ResponseEntity<List<PaymentResponse>> response = paymentController.getMyPayments(authentication);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0).getPaymentStatus()).isEqualTo(PaymentStatus.APPROVED);
        assertThat(response.getBody().get(1).getPaymentStatus()).isEqualTo(PaymentStatus.REJECTED);
    }

    @Test
    void getMyPayments_withNoPayments_returnsOkWithEmptyList() {
        // Given
        when(authentication.getPrincipal()).thenReturn(tourist);
        when(paymentService.getPaymentsByTourist(touristId)).thenReturn(Collections.emptyList());

        // When
        ResponseEntity<List<PaymentResponse>> response = paymentController.getMyPayments(authentication);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEmpty();
    }
}
