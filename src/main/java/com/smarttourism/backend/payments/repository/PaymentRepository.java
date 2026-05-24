package com.smarttourism.backend.payments.repository;

import com.smarttourism.backend.payments.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Repository for {@link Payment} entities.
 *
 * <p>Provides CRUD operations and custom query methods for payment records.
 *
 * <p>Validates: Requirements 7.1, 7.6
 */
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
}
