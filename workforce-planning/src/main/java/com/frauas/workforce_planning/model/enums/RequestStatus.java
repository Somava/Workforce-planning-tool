package com.frauas.workforce_planning.model.enums;

public enum RequestStatus {
    DRAFT,
    PUBLISHED,
    PENDING_APPROVAL,
    SUBMITTED,
    REJECTED,
    APPROVED,
    ASSIGNED,
    CANCELLED,
    OPEN,    // Added to match common workflow
    CLOSED   // Added because your data.sql uses 'CLOSED' for request_id 6
}