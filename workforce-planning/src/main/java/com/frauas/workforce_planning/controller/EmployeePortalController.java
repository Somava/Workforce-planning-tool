package com.frauas.workforce_planning.controller;

import com.frauas.workforce_planning.dto.WorkforceRequestDTO;
import com.frauas.workforce_planning.model.entity.EmployeeApplication;
import com.frauas.workforce_planning.services.StaffingRequestService;
import com.frauas.workforce_planning.services.EmployeeApplicationService; // Import the new service
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*; // Use wildcard to include PostMapping, PathVariable, etc.

import java.util.List;

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
}