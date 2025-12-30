package com.frauas.workforce_planning.dto;

public record ExternalWorkforceResponseDTO(
    String internalRequestId,       // mandatory (correlation key)
    Boolean externalResourceFound,   // mandatory
    String externalEmployeeId        // optional
) {}
