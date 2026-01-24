package com.frauas.workforce_planning.dto;

public record EmployeeApprovalDTO(
    Long requestId,
    String title,
    String status,
    String candidateType, // "INTERNAL" or "EXTERNAL"
    String candidateName,
    String email,
    Double wage,
    String skills,
    String provider // Only for external
) {}