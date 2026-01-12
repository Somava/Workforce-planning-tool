package com.frauas.workforce_planning.dto;

public record ExternalWorkforceResponseDTO(
    Long internalRequestId,   // from us (Echo)
    String status,            // e.g. "EXTERNAL_HIRED"
    ExpertDetails expertDetails
) {
  public record ExpertDetails(
      String name,
      String supplier,
      Double dailyRate
      // optionally later: String externalEmployeeId (if 3B/4B provides it)
  ) {}
}
