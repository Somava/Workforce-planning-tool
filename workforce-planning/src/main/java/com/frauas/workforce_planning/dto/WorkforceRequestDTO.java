package com.frauas.workforce_planning.dto;

/**
 * This Record defines the "Contract" between Frontend and Backend.
 * Any field added here will automatically become a Camunda variable.
 */
public record WorkforceRequestDTO(
    String positionName,     // e.g., "Senior Java Developer"
    String department,       // e.g., "Engineering"
    Integer headCount,       // e.g., 2
    String priority,         // e.g., "High"
    String requesterEmail,    // e.g., "manager@company.com"
    Long projectId
) {}