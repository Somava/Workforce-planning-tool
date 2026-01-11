package com.frauas.workforce_planning.dto;

public record ExternalDecisionRequestDTO (
    Long internalRequestId,
    String decision,     // "ACCEPTED" or "REJECTED"
    String reason        // optional
) {}
    

