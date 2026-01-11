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

// public enum RequestStatus {
//     DRAFT,  
//     PENDING_APPROVAL, -- pending request approval
//     PUBLISHED,  -- request approved, open for emeployee application
//     PENDING_INT_ASSIGNMENT_APPROVAL, -- resource planner assigned employee and waiting for dept head approval
//     INT_REJECTED, -- internal employee rejected
//     INT_APPROVED, -- internal employee approved 
//     CANCELLED, -- request cancelled
//     WAITING_EXT_RESPONSE, -- request sent to 3b
//     WAITING_EXT_APPROVAL, -- waiting for dept head approval for external employee
//     EXT_REJECTED, -- external employee rejected
//     EXT_APPROVED -- external employee approved
// }