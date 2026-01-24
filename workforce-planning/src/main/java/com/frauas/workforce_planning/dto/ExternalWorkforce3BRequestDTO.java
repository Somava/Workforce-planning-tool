package com.frauas.workforce_planning.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ExternalWorkforce3BRequestDTO(
    @JsonProperty("internalRequestId") Long requestId,
    @JsonProperty("projectId") Long projectId,
    @JsonProperty("projectName") String projectName,
    String jobTitle,
    String description,
    Integer availabilityHoursPerWeek,
    Double wagePerHour,
    java.util.List<String> skills,
    Integer experienceYears,
    String location,
    // String projectContext,
    String startDate,
    String endDate
) {}
