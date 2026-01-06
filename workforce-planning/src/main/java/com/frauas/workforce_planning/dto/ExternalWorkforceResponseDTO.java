package com.frauas.workforce_planning.dto;

public record ExternalWorkforceResponseDTO(
    String internalRequestId,     // REQUIRED (correlation key)
    String status,                // REQUIRED: FOUND / NOT_FOUND / IN_PROGRESS
    ExternalCandidate candidate,  // OPTIONAL (present when FOUND)
    String reason                 // OPTIONAL (present when NOT_FOUND)
) {
  public record ExternalCandidate(
      String externalEmployeeId,        // REQUIRED when FOUND
      Integer workloadHoursPerWeek,     // OPTIONAL but very useful
      String skills,                    // OPTIONAL (CSV)
      String availableFrom              // OPTIONAL (YYYY-MM-DD)
  ) {}
}
