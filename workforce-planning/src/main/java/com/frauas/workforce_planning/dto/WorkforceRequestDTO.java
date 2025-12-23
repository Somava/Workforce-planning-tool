package com.frauas.workforce_planning.dto;

import java.time.LocalDate;

/**
 * This Record defines the "Contract" between Frontend and Backend.
 * Any field added here will automatically become a Camunda variable.
 */
public record WorkforceRequestDTO(
    String title,
    String description,
    String requiredSkills,
    Long projectId,
    LocalDate startDate,
    LocalDate endDate,
    Integer availabilityHours,
    String projectContext,
    String performanceLocation
) {}