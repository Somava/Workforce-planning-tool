package com.frauas.workforce_planning.model.enums;

public enum RequestStatus {
    DRAFT, //Wrong Data
    //PUBLISHED,
    PENDING_APPROVAL,
    REQUEST_REJECTED,
    APPROVED, //request approved, open for emeployee application
    EMPLOYEE_RESERVED,
    EXTERNAL_SEARCH_TRIGGERED,
    INT_EMPLOYEE_APPROVED_BY_DH,
    INT_EMPLOYEE_REJECTED_BY_DH,
    INT_EMPLOYEE_REJECTED_BY_EMP,
    INT_EMPLOYEE_ASSIGNED,
    PM_RESUBMITTED,
    PM_CANCELLED,
    SUBMITTED, //WrongData , manager needs to correct
    REJECTED,
    NO_EXT_EMPLOYEE_FOUND,
    EXT_EMPLOYEE_REJECTED_BY_DH,

    
    ASSIGNED,
    CANCELLED, //request cancelled
    OPEN,    // Added to match common workflow
    CLOSED   // Added because your data.sql uses 'CLOSED' for request_id 6
}

// public enum RequestStatus {
//     
//     PENDING_INT_ASSIGNMENT_APPROVAL, -- resource planner assigned employee and waiting for dept head approval
//     INT_REJECTED, -- internal employee rejected
//     INT_APPROVED, -- internal employee approved 
//     WAITING_EXT_RESPONSE, -- request sent to 3b
//     WAITING_EXT_APPROVAL, -- waiting for dept head approval for external employee
//     EXT_REJECTED, -- external employee rejected
//     EXT_APPROVED -- external employee approved
// }