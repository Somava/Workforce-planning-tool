package com.frauas.workforce_planning.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.frauas.workforce_planning.services.StaffingRequestService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/approvals")
@Slf4j 
@CrossOrigin(origins = "http://localhost:3000")
public class StaffingApprovalController {

    @Autowired
    private StaffingRequestService staffingRequestService;

    /**
     * Endpoint for Department Heads to APPROVE a request.
     * Logic: Updates DB status to APPROVED and moves Camunda flow forward.
     */
    @PostMapping("/{requestId}/approve")
    public ResponseEntity<String> approveRequest(@PathVariable Long requestId) {
        log.info("API CALL: Approving Staffing Request ID: {}", requestId);
        try {
            // You will need to implement this method in your Service
            staffingRequestService.approveRequestByDepartmentHead(requestId);
            return ResponseEntity.ok("Request " + requestId + " has been approved.");
        } catch (Exception e) {
            log.error("Approval error: ", e);
            return ResponseEntity.status(500).body("Error processing approval: " + e.getMessage());
        }
    }

    /**
     * Endpoint for Department Heads to REJECT a request.
     */
    @PostMapping("/{requestId}/reject")
    public ResponseEntity<String> rejectRequest(@PathVariable Long requestId) {
        log.info("API CALL: Rejecting Staffing Request ID: {}", requestId);
        try {
            staffingRequestService.rejectRequestByDepartmentHead(requestId);
            return ResponseEntity.ok("Request " + requestId + " has been rejected and manager notified.");
        } catch (Exception e) {
            log.error("Rejection error: ", e);
            return ResponseEntity.status(500).body("Error processing rejection: " + e.getMessage());
        }
    }
}