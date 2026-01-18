package com.frauas.workforce_planning.dto;

public record ExternalWorkforceResponseDTO(
    String externalEmployeeId,
    String provider,
    String firstName,
    String lastName,
    String email,
    Double wagePerHour,
    java.util.List<String> skills,
    Long internalRequestId,   // this should match your internalRequestId (or they will map it)
    Double experienceYears,
    Long projectId
) {}
