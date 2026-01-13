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
import com.frauas.workforce_planning.repository.UserRepository;
import com.frauas.workforce_planning.model.entity.StaffingRequest;
import com.frauas.workforce_planning.services.StaffingRequestService;
import com.frauas.workforce_planning.model.entity.User;
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

    @Autowired
    private UserRepository userRepository;

    /**
     * Gets all requests currently waiting for a specific Department Head's approval.
     */
    @GetMapping("/dept-head")
    public ResponseEntity<?> getPendingApprovals(@RequestParam String email) {
        log.info("Fetching pending approvals for email: {}", email);

        // 1. Find the User by email to get their ID
        // Assuming you have a UserRepository or can find them via EmployeeRepository
        return userRepository.findByEmail(email)
            .map(user -> {
                // 2. Use the ID from the found user to query the requests
                List<StaffingRequest> pending = staffingRequestRepository.findPendingByDeptHead(
                    RequestStatus.PENDING_APPROVAL, 
                    user.getId()
                );
                return ResponseEntity.ok(pending);
            })
            .orElseGet(() -> {
                log.warn("User with email {} not found", email);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            });
    }

    /**
     * Handles the Approve/Reject decision and signals the Camunda Receive Task.
     */
    @PostMapping("/dept-head/decision")
    public ResponseEntity<String> handleDeptHeadDecision(
            @RequestParam Long requestId,
            @RequestParam String email, // Changed from Long deptHeadId to String email
            @RequestParam boolean approved) {

        // 1. Find the User by email to get their ID
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        // 2. Fetch the request to verify it exists
        StaffingRequest request = staffingRequestRepository.findByRequestId(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found: " + requestId));

        // 3. AUTHORIZATION: Use the ID we found from the email
        if (!request.getDepartment().getDepartmentHeadUserId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("User with email " + email + " is not authorized to approve this request.");
        }

        // 4. Delegation: Service handles DB updates and Camunda signaling
        if (approved) {
            staffingRequestService.approveRequestByDepartmentHead(requestId);
            log.info("Request {} approved by email: {}", requestId, email);
        } else {
            staffingRequestService.rejectRequestByDepartmentHead(requestId);
            log.info("Request {} rejected by email: {}", requestId, email);
        }

        String action = approved ? "approved" : "rejected";
        return ResponseEntity.ok("Request " + requestId + " has been " + action + " by " + email);
    }
}