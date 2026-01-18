package com.frauas.workforce_planning.dto;

public record ExternalWorkforce3BRequestDTO(
    Long internalRequestId,
    Long projectId,
    String projectName,
    String jobTitle,
    String description,
    Integer availabilityHoursPerWeek,
    Double wagePerHour,
    java.util.List<String> skills,
    Integer experienceYears,
    String location,
    String projectContext,  // optional
    String startDate,       // YYYY-MM-DD
    String endDate          // YYYY-MM-DD
) {}
