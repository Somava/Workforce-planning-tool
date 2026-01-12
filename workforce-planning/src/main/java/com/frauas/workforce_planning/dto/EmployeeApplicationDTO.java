package com.frauas.workforce_planning.dto;

import java.time.OffsetDateTime;

/**
 * DTO for the Employee Dashboard to track application status.
 */
public record EmployeeApplicationDTO(
    Long applicationId,
    String projectTitle,
    String status,
    OffsetDateTime appliedAt,
    boolean canWithdraw // 🔹 New field to control UI button visibility
) {}