package com.frauas.workforce_planning.dto;

public record RejectionReasonDTO(
    Long requestId,
    String rejectionType,  // e.g., "DEPT_HEAD_INITIAL", "EMPLOYEE_DECLINE"
    String rejectedBy,    // The email or role of the person who rejected
    String reasonDetail,   // The actual text comment typed by the rejector
    String timestamp       // When it happened
) {}