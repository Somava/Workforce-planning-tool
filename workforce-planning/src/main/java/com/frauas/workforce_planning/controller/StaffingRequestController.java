package com.frauas.workforce_planning.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.frauas.workforce_planning.dto.WorkforceRequestDTO;
import com.frauas.workforce_planning.model.entity.StaffingRequest;
import com.frauas.workforce_planning.security.JwtAuthFilter;
import com.frauas.workforce_planning.services.StaffingRequestService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/requests")
public class StaffingRequestController {

    @Autowired
    private StaffingRequestService staffingService;

    /**
     * Creates a new staffing request.
     * Note: Added a temporary header to simulate the logged-in manager ID.
     */
    @PostMapping("/create")
    public ResponseEntity<StaffingRequest> createRequest(
            @RequestBody WorkforceRequestDTO dto) {
        
        JwtAuthFilter.JwtPrincipal p = (JwtAuthFilter.JwtPrincipal) SecurityContextHolder
        .getContext()
        .getAuthentication()
        .getPrincipal();   
        String role = p.selectedRole();
        if(!"ROLE_MANAGER".equals(role)) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You are not authorized to perform this action"
            );
        }
        Long currentUserId = p.userId();
        // Passing the DTO and the manager ID to the service
        StaffingRequest savedRequest = staffingService.createAndStartRequest(dto, currentUserId);
        return ResponseEntity.ok(savedRequest);
    }
}