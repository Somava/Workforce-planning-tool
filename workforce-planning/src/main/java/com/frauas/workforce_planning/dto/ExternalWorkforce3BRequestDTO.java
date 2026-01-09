package com.frauas.workforce_planning.dto;

public record ExternalWorkforce3BRequestDTO(
    Long internalRequestId,
    String jobTitle,
    String project,
    String managerName,
    String skills,
    Integer experienceYears,
    Double budgetLimit,
    String startDate,
    String endDate,
    String location
) {}
