package com.frauas.workforce_planning.dto;

public record ExternalWorkforceResponseDTO(
    String externalEmployeeId,
    String provider,
    String firstName,
    String lastName,
    String email,
    Double wagePerHour,
    String skills,
    Long staffingRequestId, // Changed from internalRequestId to match Team 3b
    Float experienceYears
) {}
