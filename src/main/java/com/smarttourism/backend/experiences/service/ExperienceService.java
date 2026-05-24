package com.smarttourism.backend.experiences.service;

import com.smarttourism.backend.common.exception.ResourceNotFoundException;
import com.smarttourism.backend.experiences.dto.ExperienceFilterParams;
import com.smarttourism.backend.experiences.dto.ExperienceRequest;
import com.smarttourism.backend.experiences.dto.ExperienceResponse;
import com.smarttourism.backend.experiences.entity.Experience;
import com.smarttourism.backend.experiences.mapper.ExperienceMapper;
import com.smarttourism.backend.experiences.repository.ExperienceRepository;
import com.smarttourism.backend.experiences.specification.ExperienceSpecification;
import com.smarttourism.backend.schedules.dto.ScheduleResponse;
import com.smarttourism.backend.schedules.entity.Schedule;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Application service for the {@link Experience} domain.
 *
 * <p>Handles CRUD operations and dynamic filtering for experiences.
 * All public-facing queries restrict results to active experiences only.
 *
 * <p>Requirements: 3.1, 3.2, 3.3, 3.4, 9.1, 9.2, 9.3
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExperienceService {

    private final ExperienceRepository experienceRepository;
    private final ExperienceMapper experienceMapper;

    @PersistenceContext
    private EntityManager entityManager;

    // ── Queries ───────────────────────────────────────────────────────────────

    /**
     * Returns a paginated list of active experiences matching the given filters.
     *
     * <p>The {@link ExperienceSpecification} always includes the {@code active = true}
     * predicate, so inactive experiences are never returned.
     *
     * @param filtros   optional filter parameters (null-safe)
     * @param pageable  pagination and sorting configuration
     * @return page of {@link ExperienceResponse} DTOs
     */
    public Page<ExperienceResponse> getExperiences(ExperienceFilterParams filtros, Pageable pageable) {
        Specification<Experience> spec = ExperienceSpecification.withFilters(filtros);
        return experienceRepository.findAll(spec, pageable)
                .map(exp -> {
                    List<ScheduleResponse> schedules = fetchActiveSchedules(exp.getId());
                    Double avgRating = fetchAverageRating(exp.getId());
                    Integer reviewCount = fetchReviewCount(exp.getId());
                    return experienceMapper.toResponse(exp, avgRating, reviewCount, schedules);
                });
    }

    /**
     * Returns the full detail of a single active experience, including its
     * active schedules and aggregated review statistics.
     *
     * @param id the experience UUID
     * @return the {@link ExperienceResponse} DTO
     * @throws ResourceNotFoundException if the experience does not exist or is inactive
     */
    public ExperienceResponse getExperienceById(UUID id) {
        Experience experience = experienceRepository.findById(id)
                .filter(exp -> Boolean.TRUE.equals(exp.getActive()))
                .orElseThrow(() -> new ResourceNotFoundException("Experiencia no encontrada con id: " + id));

        List<ScheduleResponse> schedules = fetchActiveSchedules(id);
        Double avgRating = fetchAverageRating(id);
        Integer reviewCount = fetchReviewCount(id);

        return experienceMapper.toResponse(experience, avgRating, reviewCount, schedules);
    }

    // ── Commands ──────────────────────────────────────────────────────────────

    /**
     * Creates a new active experience from the given request.
     *
     * @param request the creation request DTO
     * @return the persisted {@link ExperienceResponse} DTO
     */
    @Transactional
    public ExperienceResponse createExperience(ExperienceRequest request) {
        Experience experience = experienceMapper.toEntity(request);
        experience.setActive(true);
        Experience saved = experienceRepository.save(experience);
        return experienceMapper.toResponse(saved, null, 0, List.of());
    }

    /**
     * Updates an existing experience with the fields from the given request.
     *
     * @param id      the UUID of the experience to update
     * @param request the update request DTO
     * @return the updated {@link ExperienceResponse} DTO
     * @throws ResourceNotFoundException if no experience exists with the given id
     */
    @Transactional
    public ExperienceResponse updateExperience(UUID id, ExperienceRequest request) {
        Experience experience = experienceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Experiencia no encontrada con id: " + id));

        experienceMapper.updateEntityFromRequest(request, experience);
        Experience saved = experienceRepository.save(experience);

        List<ScheduleResponse> schedules = fetchActiveSchedules(id);
        Double avgRating = fetchAverageRating(id);
        Integer reviewCount = fetchReviewCount(id);

        return experienceMapper.toResponse(saved, avgRating, reviewCount, schedules);
    }

    /**
     * Logically deletes an experience by setting {@code active = false}.
     * The record is never physically removed from the database.
     *
     * @param id the UUID of the experience to deactivate
     * @throws ResourceNotFoundException if no experience exists with the given id
     */
    @Transactional
    public void deleteExperience(UUID id) {
        Experience experience = experienceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Experiencia no encontrada con id: " + id));

        experience.setActive(false);
        experienceRepository.save(experience);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Fetches the active schedules for the given experience using JPQL.
     * Returns an empty list if no active schedules exist.
     */
    private List<ScheduleResponse> fetchActiveSchedules(UUID experienceId) {
        List<Schedule> schedules = entityManager.createQuery(
                        "SELECT s FROM Schedule s WHERE s.experience.id = :expId AND s.active = true",
                        Schedule.class)
                .setParameter("expId", experienceId)
                .getResultList();

        return schedules.stream()
                .map(s -> ScheduleResponse.builder()
                        .id(s.getId())
                        .dayOfWeek(s.getDayOfWeek())
                        .startTime(s.getStartTime())
                        .endTime(s.getEndTime())
                        .availableSlots(s.getAvailableSlots())
                        .build())
                .toList();
    }

    /**
     * Computes the average rating for the given experience using JPQL.
     * Returns {@code null} if no reviews exist.
     */
    private Double fetchAverageRating(UUID experienceId) {
        return (Double) entityManager.createQuery(
                        "SELECT AVG(r.rating) FROM Review r WHERE r.experience.id = :expId")
                .setParameter("expId", experienceId)
                .getSingleResult();
    }

    /**
     * Counts the total number of reviews for the given experience using JPQL.
     */
    private Integer fetchReviewCount(UUID experienceId) {
        Long count = (Long) entityManager.createQuery(
                        "SELECT COUNT(r) FROM Review r WHERE r.experience.id = :expId")
                .setParameter("expId", experienceId)
                .getSingleResult();
        return count != null ? count.intValue() : 0;
    }
}
