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
    private EmployeeApplicationService applicationService; // Inject the application service

    /**
     * View all staffing requests where the project is 'published'.
     */
    @GetMapping("/open-positions")
    public ResponseEntity<List<WorkforceRequestDTO>> getOpenPositions() {
        List<WorkforceRequestDTO> positions = staffingRequestService.getPublishedRequestsForEmployees();
        
        if (positions.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        
        return ResponseEntity.ok(positions);
    }

    /**
     * Logic for the "Apply" part. 
     * Allows an employee to apply for a specific request.
     */
    @PostMapping("/apply/{requestId}")
    public ResponseEntity<?> applyForPosition(
            @PathVariable Long requestId, 
            @RequestParam Long employeeId) {
        
        try {
            // Use the 'apply' method from your new service
            applicationService.apply(requestId, employeeId);
            return ResponseEntity.ok("Successfully applied for position!");
        } catch (RuntimeException e) {
            // Returns the "You already applied!" or "Not Found" error message
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    /**
     * Dashboard: Get all applications for a specific employee using DTOs.
     */
    @GetMapping("/my-applications/{employeeId}")
    // 2. Changed return type to List<EmployeeApplicationDTO>
    public ResponseEntity<List<EmployeeApplicationDTO>> getMyApplications(@PathVariable Long employeeId) {
        List<EmployeeApplicationDTO> applications = applicationService.getApplicationsForEmployee(employeeId);
        
        if (applications.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        
        return ResponseEntity.ok(applications);
    }
    /**
     * Withdraw an application.
     * This removes the application record from the database.
     */
    @DeleteMapping("/withdraw/{applicationId}")
    public ResponseEntity<String> withdrawApplication(
            @PathVariable Long applicationId, 
            @RequestParam Long employeeId) {
        
        try {
            // Call the withdraw method in your service
            applicationService.withdrawApplication(applicationId, employeeId);
            return ResponseEntity.ok("Application withdrawn successfully.");
        } catch (RuntimeException e) {
            // Returns error if the application doesn't exist or doesn't belong to the employee
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}