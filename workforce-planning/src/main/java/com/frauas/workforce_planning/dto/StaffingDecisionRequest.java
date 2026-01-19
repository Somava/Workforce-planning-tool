package com.frauas.workforce_planning.dto;

public record StaffingDecisionRequest(
        Long employeeDbId,
        boolean accept
) {}
