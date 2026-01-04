package com.frauas.workforce_planning.dto;

public record ExternalWorkforceRequestDTO(
    String internalRequestId,      // REQUIRED (correlation key)
    String projectName,            // REQUIRED (from form or DB lookup)
    String requiredSkills,         // REQUIRED (CSV string is fine)
    String startDate,              // REQUIRED (YYYY-MM-DD)
    Integer workloadHoursPerWeek   // REQUIRED (hrs/week)
) {}
