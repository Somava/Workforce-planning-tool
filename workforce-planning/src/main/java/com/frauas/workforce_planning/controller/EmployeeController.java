package com.frauas.workforce_planning.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.frauas.workforce_planning.dto.EmployeeApplicationDTO;
import com.frauas.workforce_planning.dto.EmployeeProfileDTO;
import com.frauas.workforce_planning.dto.WorkforceRequestDTO;
import com.frauas.workforce_planning.model.entity.StaffingRequest;
import com.frauas.workforce_planning.model.entity.User;
import com.frauas.workforce_planning.model.enums.RequestStatus;
import com.frauas.workforce_planning.repository.StaffingRequestRepository;
import com.frauas.workforce_planning.repository.UserRepository;
import com.frauas.workforce_planning.security.JwtAuthFilter;
import com.frauas.workforce_planning.services.EmployeeApplicationService;
import com.frauas.workforce_planning.services.EmployeeService;
import com.frauas.workforce_planning.services.StaffingRequestService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/api/employee")
public class EmployeeController {

    private final UserRepository userRepository;
    private final StaffingRequestRepository staffingRequestRepository;
    private final EmployeeApplicationService applicationService;
    private final StaffingRequestService staffingRequestService;
    private final EmployeeService employeeService;

    public EmployeeController(UserRepository userRepository,
                             StaffingRequestRepository staffingRequestRepository,
                             EmployeeApplicationService applicationService,
                             StaffingRequestService staffingRequestService,
                             EmployeeService employeeService) {
        this.userRepository = userRepository;
        this.staffingRequestRepository = staffingRequestRepository;
        this.applicationService = applicationService;
        this.staffingRequestService = staffingRequestService;
        this.employeeService = employeeService;
    }
    
    // 1. View Open Positions (Filtered for the specific employee)
    @GetMapping("/open-positions")
     public ResponseEntity<List<WorkforceRequestDTO>> getOpenPositions() {
    // We pass the email so the service can filter out what THIS employee already applied for
        JwtAuthFilter.JwtPrincipal p = (JwtAuthFilter.JwtPrincipal) SecurityContextHolder
        .getContext()
        .getAuthentication()
        .getPrincipal();

        String email = p.email();
        
        List<WorkforceRequestDTO> positions = staffingRequestService.getOpenPositionsForEmployee(email);
        return positions.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(positions);
    }

    // 2. Apply: No IDs in URL. Uses email and requestId as params.
    @PostMapping("/apply")
    public ResponseEntity<?> applyForPosition(
            @RequestParam Long requestId) {
        
        JwtAuthFilter.JwtPrincipal p = (JwtAuthFilter.JwtPrincipal) SecurityContextHolder
        .getContext()
        .getAuthentication()
        .getPrincipal();

        String email = p.email();

        try {
            applicationService.apply(requestId, email);
            return ResponseEntity.ok("Successfully applied for position!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 3. Dashboard: Fetch by email query parameter
    @GetMapping("/my-applications")
    public ResponseEntity<List<EmployeeApplicationDTO>> getMyApplications() {
        
        JwtAuthFilter.JwtPrincipal p = (JwtAuthFilter.JwtPrincipal) SecurityContextHolder
        .getContext()
        .getAuthentication()
        .getPrincipal();

        String email = p.email();
        List<EmployeeApplicationDTO> applications = applicationService.getApplicationsForEmployee(email);
        return applications.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(applications);
    }

    // 4. Withdraw: Uses applicationId and email to verify ownership
    @PostMapping("/withdraw") 
    public ResponseEntity<String> withdrawApplication(
            @RequestParam Long applicationId) {

        JwtAuthFilter.JwtPrincipal p = (JwtAuthFilter.JwtPrincipal) SecurityContextHolder
        .getContext()
        .getAuthentication()
        .getPrincipal();

        String email = p.email();
        try {
            applicationService.withdrawApplication(applicationId, email);
            return ResponseEntity.ok("Application withdrawn successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 5. Profile Details: View all employee details for the dashboard
    @GetMapping("/my-profile")
    public ResponseEntity<EmployeeProfileDTO> getMyProfile() {
            
        JwtAuthFilter.JwtPrincipal p = (JwtAuthFilter.JwtPrincipal) SecurityContextHolder
        .getContext()
        .getAuthentication()
        .getPrincipal();

        String email = p.email();
        try {
            EmployeeProfileDTO profile = employeeService.getProfile(email);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/assignment-decision")
    public ResponseEntity<String> handleEmployeeAssignmentDecision(
            @RequestParam Long requestId,
            @RequestParam boolean approved,
            @RequestParam(required = false) String reason) {

        JwtAuthFilter.JwtPrincipal p = (JwtAuthFilter.JwtPrincipal) SecurityContextHolder
        .getContext()
        .getAuthentication()
        .getPrincipal();

        String email = p.email();

        String finalReason = (reason == null || reason.trim().isEmpty()) 
                         ? "Employee declined without providing a specific reason." 
                         : reason;

        // 1) Find user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "User not found with email: " + email
                ));

        // 2) Fetch request
        StaffingRequest request = staffingRequestRepository.findByRequestId(requestId)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Request not found: " + requestId
                ));

        // 3) Must have assigned user
        if (request.getAssignedUser() == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "No assigned user for request: " + requestId
            );
        }

        // 4) Authorization: this employee must be the assigned user
        if (!request.getAssignedUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("User " + email + " is not authorized to decide for request " + requestId);
        }

        // 5) Delegate: service updates DB + signals Camunda
        if (approved) {
            staffingRequestService.confirmAssignmentByEmployee(requestId);
            log.info("Employee {} CONFIRMED assignment for request {}", email, requestId);
        } else {
            staffingRequestService.rejectAssignmentByEmployee(requestId, finalReason);
            log.info("Employee {} REJECTED assignment for request {}", email, requestId, finalReason);
        }

        String action = approved ? "confirmed" : "rejected";
        return ResponseEntity.ok("Assignment for request " + requestId + " has been " + action + " by " + email);
    }

    @GetMapping("/assigned-requests")
    public ResponseEntity<List<StaffingRequest>> getAssignedRequestsForEmployee() {

        JwtAuthFilter.JwtPrincipal p = (JwtAuthFilter.JwtPrincipal) SecurityContextHolder
        .getContext()
        .getAuthentication()
        .getPrincipal();

        String email = p.email();

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
}

