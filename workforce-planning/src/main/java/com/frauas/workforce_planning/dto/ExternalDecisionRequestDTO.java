package com.frauas.workforce_planning.dto;

public record ExternalDecisionRequestDTO(
    Long requestId,
    String decision,
    String reason
) {}


