package com.frauas.workforce_planning.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ExternalWorkforce3BRequestDTO(
    @JsonProperty("internalRequestId") Long requestId,
    @JsonProperty("projectId") Long projectId,
    @JsonProperty("projectName") String projectName,
    String jobTitle,
    String description,
    Integer availabilityHoursPerWeek,
    Double wagePerHour,
    List<String> skills,
    Integer experienceYears,
    String location,
    // String projectContext,
    String startDate,
    String endDate
) {}
