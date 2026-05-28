package com.smarttourism.backend.schedules.service;

import com.smarttourism.backend.common.exception.ResourceNotFoundException;
import com.smarttourism.backend.experiences.entity.Experience;
import com.smarttourism.backend.experiences.repository.ExperienceRepository;
import com.smarttourism.backend.schedules.dto.ScheduleRequest;
import com.smarttourism.backend.schedules.dto.ScheduleResponse;
import com.smarttourism.backend.schedules.entity.Schedule;
import com.smarttourism.backend.schedules.mapper.ScheduleMapper;
import com.smarttourism.backend.schedules.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Application service for the {@link Schedule} domain.
 *
 * <p>Handles creation, update and logical deletion of schedules associated
 * with a parent {@link Experience}. All write operations are restricted to
 * users with the {@code ADMIN} role (enforced at the controller layer).
 *
 * <p>Requirements: 9.5
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final ExperienceRepository experienceRepository;
    private final ScheduleMapper scheduleMapper;

    // ── Queries ──────────────────────────────────────────────────────────────

    /**
     * Returns all active schedules for the given experience.
     *
     * @param experienceId the UUID of the parent experience
     * @return list of {@link ScheduleResponse} DTOs, may be empty
     * @throws ResourceNotFoundException if no active experience exists with the given id
     */
    public List<ScheduleResponse> getSchedulesByExperience(UUID experienceId) {
        experienceRepository.findById(experienceId)
                .filter(exp -> Boolean.TRUE.equals(exp.getActive()))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Experiencia no encontrada con id: " + experienceId));

        return scheduleRepository.findByExperienceIdAndActiveTrue(experienceId)
                .stream()
                .map(scheduleMapper::toResponse)
                .toList();
    }

    // ── Commands ──────────────────────────────────────────────────────────────

    /**
     * Creates a new active schedule for the given experience.
     *
     * @param experienceId the UUID of the parent experience
     * @param request      the creation request DTO
     * @return the persisted {@link ScheduleResponse} DTO
     * @throws ResourceNotFoundException if no active experience exists with the given id
     */
    @Transactional
    public ScheduleResponse createSchedule(UUID experienceId, ScheduleRequest request) {
        Experience experience = experienceRepository.findById(experienceId)
                .filter(exp -> Boolean.TRUE.equals(exp.getActive()))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Experiencia no encontrada con id: " + experienceId));

        Schedule schedule = scheduleMapper.toEntity(request);
        schedule.setExperience(experience);
        schedule.setActive(true);

        Schedule saved = scheduleRepository.save(schedule);
        return scheduleMapper.toResponse(saved);
    }

    /**
     * Updates an existing schedule with the fields from the given request.
     *
     * @param scheduleId the UUID of the schedule to update
     * @param request    the update request DTO
     * @return the updated {@link ScheduleResponse} DTO
     * @throws ResourceNotFoundException if no schedule exists with the given id
     */
    @Transactional
    public ScheduleResponse updateSchedule(UUID scheduleId, ScheduleRequest request) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Horario no encontrado con id: " + scheduleId));

        scheduleMapper.updateEntityFromRequest(request, schedule);
        Schedule saved = scheduleRepository.save(schedule);
        return scheduleMapper.toResponse(saved);
    }

    /**
     * Logically deletes a schedule by setting {@code active = false}.
     * The record is never physically removed from the database.
     *
     * @param scheduleId the UUID of the schedule to deactivate
     * @throws ResourceNotFoundException if no schedule exists with the given id
     */
    @Transactional
    public void deactivateSchedule(UUID scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Horario no encontrado con id: " + scheduleId));

        schedule.setActive(false);
        scheduleRepository.save(schedule);
    }
}
