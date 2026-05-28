package com.smarttourism.backend.payments.repository;

import com.smarttourism.backend.payments.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

/**
 * Repository for {@link Payment} entities.
 *
 * <p>Provides CRUD operations and custom query methods for payment records.
 *
 * <p>Validates: Requirements 7.1, 7.6
 */
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    /**
     * Finds all payments for a given tourist, eagerly fetching reservation, tourist, and experience
     * associations to avoid N+1 queries. Results are ordered by creation date descending.
     *
     * <p>Validates: Requirements 4.1, 4.2, 4.3
     *
     * @param touristId the UUID of the tourist
     * @return list of payments with fully loaded associations, ordered by createdAt DESC
     */
    @Query("SELECT p FROM Payment p " +
           "JOIN FETCH p.reservation r " +
           "JOIN FETCH r.tourist " +
           "JOIN FETCH r.experience " +
           "WHERE r.tourist.id = :touristId " +
           "ORDER BY p.createdAt DESC")
    List<Payment> findByTouristIdWithDetails(@Param("touristId") UUID touristId);
}
