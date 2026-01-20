package com.frauas.workforce_planning.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import com.frauas.workforce_planning.repository.StaffingRequestRepository;
import com.frauas.workforce_planning.repository.UserRepository;
import com.frauas.workforce_planning.services.EmployeeApplicationService;
import com.frauas.workforce_planning.services.EmployeeService;
import com.frauas.workforce_planning.services.StaffingRequestService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/api/employee-portal")
public class EmployeePortalController {

    @Autowired
    private StaffingRequestService staffingRequestService;

    @Autowired
    private EmployeeApplicationService applicationService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StaffingRequestRepository staffingRequestRepository;
    
    // 1. View Open Positions (Filtered for the specific employee)
    @GetMapping("/open-positions")
     public ResponseEntity<List<WorkforceRequestDTO>> getOpenPositions(@RequestParam String email) {
    // We pass the email so the service can filter out what THIS employee already applied for
    List<WorkforceRequestDTO> positions = staffingRequestService.getOpenPositionsForEmployee(email);
    return positions.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(positions);
}

    // 2. Apply: No IDs in URL. Uses email and requestId as params.
    @PostMapping("/apply")
    public ResponseEntity<?> applyForPosition(
            @RequestParam Long requestId, 
            @RequestParam String email) {
        try {
            applicationService.apply(requestId, email);
            return ResponseEntity.ok("Successfully applied for position!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 3. Dashboard: Fetch by email query parameter
    @GetMapping("/my-applications")
    public ResponseEntity<List<EmployeeApplicationDTO>> getMyApplications(@RequestParam String email) {
        List<EmployeeApplicationDTO> applications = applicationService.getApplicationsForEmployee(email);
        return applications.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(applications);
    }

    // 4. Withdraw: Uses applicationId and email to verify ownership
    @PostMapping("/withdraw") 
    public ResponseEntity<String> withdrawApplication(
            @RequestParam Long applicationId, 
            @RequestParam String email) {
        try {
            applicationService.withdrawApplication(applicationId, email);
            return ResponseEntity.ok("Application withdrawn successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 5. Profile Details: View all employee details for the dashboard
    @GetMapping("/my-profile")
    public ResponseEntity<EmployeeProfileDTO> getMyProfile(@RequestParam String email) {
        try {
            EmployeeProfileDTO profile = employeeService.getProfile(email);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/employee/assignment-decision")
    public ResponseEntity<String> handleEmployeeAssignmentDecision(
            @RequestParam Long requestId,
            @RequestParam String email,
            @RequestParam boolean approved,
            @RequestParam(required = false) String reason) {

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
}
