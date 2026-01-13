package com.frauas.workforce_planning.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.frauas.workforce_planning.dto.WorkforceRequestDTO;
import com.frauas.workforce_planning.model.entity.StaffingRequest;
import com.frauas.workforce_planning.services.StaffingRequestService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
    
    // In WorkforceController.java
    @GetMapping("/manager-requests")
    public ResponseEntity<List<StaffingRequest>> getManagerRequests(@RequestParam String email) {
        log.info("Fetching requests created by manager: {}", email);
        List<StaffingRequest> requests = staffingService.getRequestsByManagerEmail(email);
        return ResponseEntity.ok(requests);
    }
}