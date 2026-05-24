package com.smarttourism.backend.schedules.repository;

import com.smarttourism.backend.schedules.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * JPA repository for {@link Schedule} entities.
 *
 * <p>Validates: Requirements 9.5
 */
@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, UUID> {

    /**
     * Returns all active schedules for the given experience.
     *
     * @param experienceId the UUID of the parent experience
     * @return list of active schedules, may be empty
     */
    List<Schedule> findByExperienceIdAndActiveTrue(UUID experienceId);
}
