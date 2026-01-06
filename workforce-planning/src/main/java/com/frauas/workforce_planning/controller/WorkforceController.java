package com.frauas.workforce_planning.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.frauas.workforce_planning.dto.WorkforceRequestDTO;
import com.frauas.workforce_planning.model.entity.StaffingRequest;
import com.frauas.workforce_planning.services.StaffingRequestService;

import java.util.List;

@RestController
@RequestMapping("/api/requests")
public class WorkforceController {

    @Autowired
    private StaffingRequestService staffingService;

    /**
     * Creates a new staffing request.
     * Note: Added a temporary header to simulate the logged-in manager ID.
     */
    @PostMapping("/create")
    public ResponseEntity<StaffingRequest> createRequest(
            @RequestBody WorkforceRequestDTO dto,
            @RequestHeader(value = "X-User-ID", defaultValue = "1") Long currentUserId) {
        
        // Passing the DTO and the manager ID to the service
        StaffingRequest savedRequest = staffingService.createAndStartRequest(dto, currentUserId);
        return ResponseEntity.ok(savedRequest);
    }

    /**
     * Updates an existing request.
     * Changed path variable name to match the service's findByRequestId logic.
     */
    @PutMapping("/{requestId}/update")
    public ResponseEntity<StaffingRequest> updateRequest(
            @PathVariable Long requestId, 
            @RequestBody WorkforceRequestDTO dto) {
        
        StaffingRequest updated = staffingService.updateExistingRequest(requestId, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * New helper endpoint to view all requests
     */
    @GetMapping("/all")
    public ResponseEntity<List<StaffingRequest>> getAllRequests() {
        return ResponseEntity.ok(staffingService.getAllRequests());
    }
}