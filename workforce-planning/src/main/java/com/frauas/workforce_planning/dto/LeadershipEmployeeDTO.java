package com.frauas.workforce_planning.dto;

import java.util.List;

public record LeadershipEmployeeDTO(
    Long id,
    String employeeId,
    String fullName,
    String jobRole,
    String email,
    List<String> skills,
    List<String> languages,
    Integer experienceYears,
    Double performanceRating,
    String availabilityStatus
) {}