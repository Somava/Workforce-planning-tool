package com.frauas.workforce_planning.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity; // Import the new service
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable; // Use wildcard to include PostMapping, PathVariable, etc.
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.frauas.workforce_planning.dto.EmployeeApplicationDTO;
import com.frauas.workforce_planning.dto.WorkforceRequestDTO;
import com.frauas.workforce_planning.services.EmployeeApplicationService;
import com.frauas.workforce_planning.services.StaffingRequestService;

@RestController
@RequestMapping("/api/employee-portal")
public class EmployeePortalController {

    @Autowired
    private StaffingRequestService staffingRequestService;

    @Autowired
    private EmployeeApplicationService applicationService;

    // 1. View Open Positions
    @GetMapping("/open-positions")
    public ResponseEntity<List<WorkforceRequestDTO>> getOpenPositions() {
        List<WorkforceRequestDTO> positions = staffingRequestService.getApprovedRequestsForEmployees();
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
}