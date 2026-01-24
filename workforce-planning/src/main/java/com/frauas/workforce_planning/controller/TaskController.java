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

import com.frauas.workforce_planning.model.entity.ProjectDepartment;
import com.frauas.workforce_planning.model.entity.StaffingRequest;
import com.frauas.workforce_planning.model.entity.User;
import com.frauas.workforce_planning.model.enums.RequestStatus;
import com.frauas.workforce_planning.repository.StaffingRequestRepository;
import com.frauas.workforce_planning.repository.ProjectDepartmentRepository;
import com.frauas.workforce_planning.repository.UserRepository;
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

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectDepartmentRepository projectDepartmentRepository;

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
            @RequestParam boolean approved,
            @RequestParam(required = false) String reason) {

        String finalReason = (reason == null || reason.trim().isEmpty()) 
                         ? "No specific reason provided by Dept Head." 
                         : reason;

        // 1. Find the User by email to get their ID
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        // 2. Fetch the request to verify it exists
        StaffingRequest request = staffingRequestRepository.findByRequestId(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found: " + requestId));

        ProjectDepartment projDept = projectDepartmentRepository
            .findByProject_IdAndDepartment_Id(
                request.getProject().getId(),
                request.getDepartment().getId()
            );

        // 3. AUTHORIZATION: Use the ID we found from the email
        if (!projDept.getDepartmentHeadUser().getId().equals(user.getId())) {
            log.warn("Unauthorized access attempt by {} for request {}", email, requestId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("User with email " + email + " is not authorized to approve this request.");
        }

        // 4. Delegation: Service handles DB updates and Camunda signaling
        if (approved) {
            staffingRequestService.approveRequestByDepartmentHead(requestId);
            log.info("Request {} approved by email: {}", requestId, email);
        } else {
            staffingRequestService.rejectRequestByDepartmentHead(requestId, finalReason);
            log.info("Request {} rejected by email: {}", requestId, email, finalReason);
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
            @RequestParam boolean approved,
            @RequestParam(required = false) String reason) {

        String finalReason = (reason == null || reason.trim().isEmpty()) 
                         ? "Dept Head rejected the internal employee assignment without specific feedback." 
                         : reason;

        // 1) Resolve Dept Head user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        // 2) Load request
        StaffingRequest request = staffingRequestRepository.findByRequestId(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found: " + requestId));
        
        ProjectDepartment projDept = projectDepartmentRepository
            .findByProject_IdAndDepartment_Id(
                request.getProject().getId(),
                request.getDepartment().getId()
            );

        var departmentHeadUser = projDept.getDepartmentHeadUser(); 

        // 3) Authorization: Dept Head must own this department
        if (request.getDepartment() == null || departmentHeadUser == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Request " + requestId + " has no department/dept head assigned.");
        }

        if (!departmentHeadUser.getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("User with email " + email + " is not authorized to update this request.");
        }

        // 4) Delegate: service updates DB + signals Camunda
        if (approved) {
            staffingRequestService.markInternalEmployeeApproved(requestId);
            log.info("Internal employee APPROVED request {} (reported by dept head email={})", requestId, email);
        } else {
            staffingRequestService.markInternalEmployeeRejected(requestId, finalReason);
            log.info("Internal employee REJECTED request {} (reported by dept head email={})", requestId, email, finalReason);
        }

        String action = approved ? "approved" : "rejected";
        return ResponseEntity.ok("Internal employee has " + action + " request " + requestId + " (reported by " + email + ").");
    }


     /**
     * Gets all requests approved by Dept Head that are waiting for this Resource Planner to assign.
     * Input: userId (resource planner's user id)
     */
    @GetMapping("/resource-planner")
    public ResponseEntity<List<StaffingRequest>> getApprovedForResourcePlanner(@RequestParam String email) {
        log.info("Fetching approved requests for resource planner email: {}", email);

        
        List<StaffingRequest> pending = staffingRequestRepository.findApprovedForResourcePlanner(
            RequestStatus.APPROVED,
            email
        );

        if (pending.isEmpty()) {
            return ResponseEntity.ok()
                .header("X-Info", "No approved staffing requests found")
                .body(List.of());
        }

        return ResponseEntity.ok(pending);
    }

    /**
     * Gets all staffing requests where internal employee rejected by department head.
     */
    @GetMapping("/project-manager/int-employee-rejected")
    public ResponseEntity<List<StaffingRequest>> getAllIntEmployeeRejectedRequests(@RequestParam String email) {

        User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "User not found for email: " + email
        ));

        boolean isProjectManager = user.getEmployee() != null
            && user.getEmployee().getDefaultRole() != null
            && "ROLE_MANAGER".equals(
                user.getEmployee().getDefaultRole().getName()
            );

        if (!isProjectManager) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You are not authorized to view this resource"
            );
        }
        List<StaffingRequest> rejected =
            staffingRequestRepository.findByStatus(RequestStatus.INT_EMPLOYEE_REJECTED_BY_DH);
        
            if (rejected.isEmpty()) {
                return ResponseEntity.ok()
                    .header("X-Info", "No rejected staffing requests found")
                    .body(List.of());
            }

        return ResponseEntity.ok(rejected);
    }

    /**
     * Gets all staffing requests assigned to this employee
     * that are in INTERNAL EMPLOYEE APPROVED state.
     */
    @GetMapping("/employee/assigned-requests")
    public ResponseEntity<List<StaffingRequest>> getAssignedRequestsForEmployee(
            @RequestParam String email) {

        log.info("Fetching assigned requests for employee email: {}", email);

        // 1. Resolve user by email
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "User not found for email: " + email
            ));

        // 2. Ensure this user is linked to an employee
        if (user.getEmployee() == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "User is not linked to an internal employee"
            );
        }

        Long employeeUserId = user.getId();

        // 3. Fetch assigned requests
        List<StaffingRequest> assignedRequests =
            staffingRequestRepository.findAssignedToEmployeeByStatus(
                RequestStatus.INT_EMPLOYEE_APPROVED_BY_DH,
                employeeUserId
            );

        if (assignedRequests.isEmpty()) {
            return ResponseEntity.ok()
                .header("X-Info", "No assigned staffing requests found")
                .body(List.of());
        }

        return ResponseEntity.ok(assignedRequests);
    }

    /**
     * Employee confirms or rejects an assignment for a staffing request.
     * Signals the Camunda Receive Task via service layer.
     */
    


    /**
     * Project Manager decides whether to resubmit a request after internal employee rejection.
     * Signals Camunda in the service layer.
     */
    // @PostMapping("/project-manager/resubmit-decision")
    // public ResponseEntity<String> handleProjectManagerResubmitDecision(
    //         @RequestParam Long requestId,
    //         @RequestParam String email,
    //         @RequestParam boolean resubmit) {

    //     // 1) Find user by email
    //     User user = userRepository.findByEmail(email)
    //             .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

    //     // 2) Role check: must be project manager
    //     boolean isProjectManager =
    //             user.getEmployee() != null
    //             && user.getEmployee().getDefaultRole() != null
    //             && "ROLE_MANAGER".equals(user.getEmployee().getDefaultRole().getName());

    //     if (!isProjectManager) {
    //         return ResponseEntity.status(HttpStatus.FORBIDDEN)
    //                 .body("User with email " + email + " is not authorized (not a Project Manager).");
    //     }

    //     // 3) Fetch request
    //     StaffingRequest request = staffingRequestRepository.findByRequestId(requestId)
    //             .orElseThrow(() -> new RuntimeException("Request not found: " + requestId));

        

    //     // 4) Delegate to service: DB updates + Camunda message
    //     if (resubmit) {
    //         staffingRequestService.resubmitRequestByProjectManager(requestId);
    //         log.info("Request {} RESUBMITTED by PM email: {}", requestId, email);
    //     } else {
    //         staffingRequestService.cancelRequestByProjectManager(requestId);
    //         log.info("Request {} NOT resubmitted (cancelled/closed) by PM email: {}", requestId, email);
    //     }

    //     String action = resubmit ? "resubmitted" : "not resubmitted";
    //     return ResponseEntity.ok("Request " + requestId + " has been " + action + " by " + email);
    // }

}