package com.frauas.workforce_planning.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.frauas.workforce_planning.model.enums.RequestStatus;
import com.frauas.workforce_planning.repository.StaffingRequestRepository;
import com.frauas.workforce_planning.model.entity.StaffingRequest;
import com.frauas.workforce_planning.services.StaffingRequestService;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/tasks")
@Slf4j
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class TaskController {

    @Autowired
    private StaffingRequestService staffingRequestService; // Fixed: Use Autowired

    @Autowired
    private StaffingRequestRepository staffingRequestRepository;

    /**
     * Gets all requests currently waiting for a specific Department Head's approval.
     */
    @GetMapping("/dept-head")
    public ResponseEntity<List<StaffingRequest>> getPendingApprovals(@RequestParam Long departmentHeadUserId) {
        var pending = staffingRequestRepository.findPendingByDeptHead(RequestStatus.PENDING_APPROVAL, departmentHeadUserId);
        return ResponseEntity.ok(pending);
    }

    /**
     * Handles the Approve/Reject decision and signals the Camunda Receive Task.
     */
    @PostMapping("/dept-head/decision")
    public ResponseEntity<String> handleDeptHeadDecision(
            @RequestParam Long requestId,
            @RequestParam Long deptHeadId,
            @RequestParam boolean approved) {

        // 1. Fetch to verify authorization
        StaffingRequest request = staffingRequestRepository.findByRequestId(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found: " + requestId));

        // 2. AUTHORIZATION
        if (!request.getDepartment().getDepartmentHeadUserId().equals(deptHeadId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("User " + deptHeadId + " is not authorized to approve this request.");
        }

        // 3. Delegation: Let the Service handle BOTH DB and Zeebe logic
        if (approved) {
            staffingRequestService.approveRequestByDepartmentHead(requestId);
            log.info("Request {} approved by Dept Head {}", requestId, deptHeadId);
        } else {
            staffingRequestService.rejectRequestByDepartmentHead(requestId);
            log.info("Request {} rejected by Dept Head {}", requestId, deptHeadId);
        }

        String action = approved ? "approved" : "rejected";
        return ResponseEntity.ok("Request " + requestId + " has been " + action + " and signaling Camunda.");
    }
}