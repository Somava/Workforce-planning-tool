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
import org.springframework.web.server.ResponseStatusException;

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
     * Gets all requests waiting for internal employee approval
     * for a specific Department Head's department.
     */
    @GetMapping("/dept-head/int-employee-approval")
    public ResponseEntity<?> getPendingIntEmployeeApprovals(@RequestParam String email) {

        log.info("Fetching employee-approval requests for dept head email: {}", email);
         // Validate + resolve user
        User deptHead = userRepository.findByEmail(email)
            .orElseThrow(() -> {
                log.warn("User not found for email={}", email);
                return new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "User not found for email: " + email
                );
            });

        List<StaffingRequest> pending =
        staffingRequestRepository.findPendingByDeptHead(
            RequestStatus.EMPLOYEE_RESERVED,
            deptHead.getId()
        );

        // Return empty list (200 OK) if none — this is correct REST behavior
        return ResponseEntity.ok(pending);
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

    /**
     * Handles the INTERNAL EMPLOYEE approval decision (rejected/confirmed by Dept Head)
     * and signals the Camunda Receive Task.
     */
    @PostMapping("/dept-head/int-employee-decision")
    public ResponseEntity<String> handleInternalEmployeeDecision(
            @RequestParam Long requestId,
            @RequestParam String email,
            @RequestParam boolean approved) {

        // 1) Resolve Dept Head user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        // 2) Load request
        StaffingRequest request = staffingRequestRepository.findByRequestId(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found: " + requestId));

        // 3) Authorization: Dept Head must own this department
        if (request.getDepartment() == null || request.getDepartment().getDepartmentHeadUserId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Request " + requestId + " has no department/dept head assigned.");
        }

        if (!request.getDepartment().getDepartmentHeadUserId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("User with email " + email + " is not authorized to update this request.");
        }

        // 4) Delegate: service updates DB + signals Camunda
        if (approved) {
            staffingRequestService.markInternalEmployeeApproved(requestId);
            log.info("Internal employee APPROVED request {} (reported by dept head email={})", requestId, email);
        } else {
            staffingRequestService.markInternalEmployeeRejected(requestId);
            log.info("Internal employee REJECTED request {} (reported by dept head email={})", requestId, email);
        }

        String action = approved ? "approved" : "rejected";
        return ResponseEntity.ok("Internal employee has " + action + " request " + requestId + " (reported by " + email + ").");
    }


     /**
     * Gets all requests approved by Dept Head that are waiting for this Resource Planner to assign.
     * Input: userId (resource planner's user id)
     */
    @GetMapping("/resource-planner")
    public ResponseEntity<List<StaffingRequest>> getApprovedForResourcePlanner(@RequestParam Long userId) {
        log.info("Fetching approved requests for resource planner userId: {}", userId);

        List<StaffingRequest> pending = staffingRequestRepository.findApprovedForResourcePlanner(
            RequestStatus.APPROVED,
            userId
        );

        return ResponseEntity.ok(pending);
    }


}