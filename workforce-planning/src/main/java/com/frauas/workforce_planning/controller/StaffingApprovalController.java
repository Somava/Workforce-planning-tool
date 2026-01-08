package com.frauas.workforce_planning.controller; // Ensure this is singular

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.frauas.workforce_planning.services.StaffingRequestService;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/approvals")
@Slf4j 
@CrossOrigin(origins = "http://localhost:3000") // Added to allow frontend access
public class StaffingApprovalController {

    @Autowired
    private StaffingRequestService staffingRequestService;

    @PostMapping("/{requestId}/reject")
    public ResponseEntity<String> rejectRequest(@PathVariable Long requestId) {
        log.info("API CALL RECEIVED: Rejecting Staffing Request ID: {}", requestId);
        try {
            staffingRequestService.rejectRequestByDepartmentHead(requestId);
            return ResponseEntity.ok("Request " + requestId + " has been rejected and manager notified.");
        } catch (Exception e) {
            log.error("Rejection error: ", e);
            return ResponseEntity.status(500).body("Error processing rejection: " + e.getMessage());
        }
    }
}