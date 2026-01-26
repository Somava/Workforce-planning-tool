package com.frauas.workforce_planning.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.frauas.workforce_planning.model.entity.Department;
import com.frauas.workforce_planning.model.entity.ProjectDepartment;
import com.frauas.workforce_planning.model.entity.StaffingRequest;
import com.frauas.workforce_planning.model.entity.User;
import com.frauas.workforce_planning.model.enums.RequestStatus;
import com.frauas.workforce_planning.repository.ExternalEmployeeRepository;
import com.frauas.workforce_planning.repository.ProjectDepartmentRepository;
import com.frauas.workforce_planning.repository.StaffingRequestRepository;
import com.frauas.workforce_planning.repository.UserRepository;
import com.frauas.workforce_planning.services.StaffingRequestService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/tasks")
@Slf4j
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class TaskController {

    private final StaffingRequestService staffingRequestService;
    private final StaffingRequestRepository staffingRequestRepository;
    private final UserRepository userRepository;
    private final ProjectDepartmentRepository projectDepartmentRepository;
    private final ExternalEmployeeRepository externalEmployeeRepository; // Now being used!

    // Using Constructor Injection (Recommended)
    public TaskController(
            StaffingRequestService staffingRequestService,
            StaffingRequestRepository staffingRequestRepository,
            UserRepository userRepository,
            ProjectDepartmentRepository projectDepartmentRepository,
            ExternalEmployeeRepository externalEmployeeRepository
    ) {
        this.staffingRequestService = staffingRequestService;
        this.staffingRequestRepository = staffingRequestRepository;
        this.userRepository = userRepository;
        this.projectDepartmentRepository = projectDepartmentRepository;
        this.externalEmployeeRepository = externalEmployeeRepository;
    }

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
                    user.getEmployee().getDepartment().getId()
                );
                return ResponseEntity.ok(pending);
            })
            .orElseGet(() -> {
                log.warn("User with email {} not found", email);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            });
    }

    @GetMapping("/dept-head/employee-approval")
    public ResponseEntity<List<StaffingRequest>> getFullPendingApprovals(@RequestParam String email) {
        
        User deptHead = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dept Head not found"));
        
        if(!deptHead.getEmployee().getDefaultRole().getName().equals("ROLE_DEPT_HEAD")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not a Department Head");
        }

        Department department = deptHead.getEmployee().getDepartment();

        List<StaffingRequest> requests = staffingRequestRepository.findPendingApprovals(
            department.getId(),
            List.of(RequestStatus.EMPLOYEE_RESERVED, RequestStatus.EXTERNAL_RESPONSE_RECEIVED)
        );

        requests.forEach(sr -> {
            if (sr.getStatus() == RequestStatus.EXTERNAL_RESPONSE_RECEIVED) {
                // This will no longer throw NullPointerException!
                externalEmployeeRepository.findByStaffingRequestId(sr.getRequestId())
                    .ifPresent(sr::setExternalEmployee);
                
                sr.setAssignedUser(null); 
            } else if (sr.getStatus() == RequestStatus.EMPLOYEE_RESERVED) {
                sr.setExternalEmployee(null);
            }
        });

        return ResponseEntity.ok(requests);
    }

    /**
     * Handles the Approve/Reject decision and signals the Camunda Receive Task.
     */
    @PostMapping("/dept-head/request-approval-decision")
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

    @PostMapping("/dept-head/employee-assigning-decision")
    public ResponseEntity<String> handleDecision(
            @RequestParam Long requestId,
            @RequestParam String email,
            @RequestParam boolean approved,
            @RequestParam(required = false) String reason) {

        log.info("Processing decision for Request ID: {} from Dept Head: {}. Approved: {}", requestId, email, approved);

        // 1. Resolve User and Request
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
                    .body("Access Denied: You are not the assigned Head for this department.");
        }

        String finalReason = (reason == null || reason.trim().isEmpty()) 
                ? "No specific feedback provided by Department Head." : reason;

        String candidateType = "";

        // 3. Route Logic based on Request Status
        if (request.getStatus() == RequestStatus.EMPLOYEE_RESERVED) {
            candidateType = "Internal";
            if (approved) {
                staffingRequestService.markInternalEmployeeApproved(requestId);
            } else {
                staffingRequestService.markInternalEmployeeRejected(requestId, finalReason);
            }
        } 
        else if (request.getStatus() == RequestStatus.EXTERNAL_RESPONSE_RECEIVED) {
            candidateType = "External";
            if (approved) {
                staffingRequestService.markExternalEmployeeApproved(requestId);
            } else {
                staffingRequestService.markExternalEmployeeRejected(requestId, finalReason);
            }
        } 
        else {
            return ResponseEntity.badRequest()
                    .body("Action invalid. Request is currently in '" + request.getStatus() + "' status.");
        }

        String action = approved ? "approved" : "rejected";
        return ResponseEntity.ok(String.format("%s candidate has been %s for Request ID: %d", candidateType, action, requestId));
    }


     /**
     * Gets all requests approved by Dept Head that are waiting for this Resource Planner to assign.
     * Input: userId (resource planner's user id)
     */
    @GetMapping("/resource-planner")
    public ResponseEntity<List<StaffingRequest>> getApprovedForResourcePlanner(@RequestParam String email) {
        log.info("Fetching approved requests for resource planner email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found for email: " + email
                ));
        if(!user.getEmployee().getDefaultRole().getName().equals("ROLE_RESOURCE_PLNR")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not a Resource Planner");
        }
        Department department = user.getEmployee().getDepartment();                
        
        List<StaffingRequest> pending = staffingRequestRepository.findApprovedForResourcePlanner(
            RequestStatus.APPROVED,
            department.getId()
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