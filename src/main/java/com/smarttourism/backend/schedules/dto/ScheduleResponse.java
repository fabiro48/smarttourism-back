package com.smarttourism.backend.schedules.dto;

import com.smarttourism.backend.common.enums.DayOfWeek;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.UUID;

/**
 * Response DTO for a {@code Schedule} resource.
 *
 * <p>Validates: Requirements 3.2, 9.5
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleResponse {

    /** Unique identifier of the schedule. */
    private UUID id;

    /** Day of the week when the experience runs. */
    private DayOfWeek dayOfWeek;

    /** Time the experience starts. */
    private LocalTime startTime;

    /** Time the experience ends. */
    private LocalTime endTime;

    /** Number of slots still available for booking. */
    private Integer availableSlots;
}
