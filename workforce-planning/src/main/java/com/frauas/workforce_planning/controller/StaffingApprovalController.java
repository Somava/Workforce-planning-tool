package com.frauas.workforce_planning.controllers;

import com.frauas.workforce_planning.services.StaffingRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/approvals")
@Slf4j // Added for logging
public class StaffingApprovalController {

    @Autowired
    private StaffingRequestService staffingRequestService;

    /**
     * Triggers the rejection logic.
     * When called:
     * 1. DB Status changes to REJECTED.
     * 2. Camunda moves to 'Notify Requester'.
     * 3. Worker sends the email to the Manager (ID 1 or 3).
     */
    @PostMapping("/{requestId}/reject")
    public ResponseEntity<String> rejectRequest(@PathVariable Long requestId) {
        // --- LOGS METHOD START ---
        log.info("API CALL RECEIVED: Rejecting Staffing Request ID: {}", requestId);
        // --- LOGS METHOD END ---
        try {
            staffingRequestService.rejectRequestByDepartmentHead(requestId);
            return ResponseEntity.ok("Request " + requestId + " has been rejected and manager notified.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error processing rejection: " + e.getMessage());
        }
    }
    

}