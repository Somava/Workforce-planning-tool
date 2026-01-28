package com.frauas.workforce_planning.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.frauas.workforce_planning.dto.StaffingRequestUpdateDTO;
import com.frauas.workforce_planning.dto.WorkforceRequestDTO;
import com.frauas.workforce_planning.model.entity.StaffingRequest;
import com.frauas.workforce_planning.model.enums.RequestStatus;
import com.frauas.workforce_planning.repository.StaffingRequestRepository;
import com.frauas.workforce_planning.repository.UserRepository;
import com.frauas.workforce_planning.security.JwtAuthFilter;
import com.frauas.workforce_planning.services.ManagerDecisionService;
import com.frauas.workforce_planning.services.StaffingRequestService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/manager")
public class ManagerController {

    private final StaffingRequestRepository staffingRequestRepository;
    private final ManagerDecisionService managerDecisionService;
    private final StaffingRequestService staffingRequestService;
    private final StaffingRequestService staffingService;

    public ManagerController(UserRepository userRepository,
                             StaffingRequestRepository staffingRequestRepository,
                             ManagerDecisionService managerDecisionService,
                             StaffingRequestService staffingRequestService,
                             StaffingRequestService staffingService) {
        this.staffingRequestRepository = staffingRequestRepository;
        this.managerDecisionService = managerDecisionService;
        this.staffingRequestService = staffingRequestService;
        this.staffingService = staffingService;
    }

    @PostMapping("/create-staffing-requests")
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
    

    /**
     * 1. GET all requests for a specific manager
     * Used for the Manager Dashboard view.
     */
    @GetMapping("/all-staffing-requests")
    public ResponseEntity<List<StaffingRequest>> getManagerRequests() {
        
        JwtAuthFilter.JwtPrincipal p = (JwtAuthFilter.JwtPrincipal) SecurityContextHolder
        .getContext()
        .getAuthentication()
        .getPrincipal();

        String role = p.selectedRole();
        if(!"ROLE_MANAGER".equals(role)) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You are not authorized to view this resource"
            );
        }

        String email = p.email();

        log.info("Fetching requests created by manager: {}", email);
        // Ensure this method exists in your StaffingRequestService
        List<StaffingRequest> requests = staffingRequestService.getRequestsByManagerEmail(email);
        return ResponseEntity.ok(requests);
    }

    @PostMapping("/staffing-request/review-decision")
    public ResponseEntity<String> handleManagerDecision(
            @RequestParam Long requestId,
            @RequestParam boolean isResubmit,
            @RequestBody(required = false) StaffingRequestUpdateDTO updateData) 
    {
        
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
        
        if (isResubmit) {
            log.info("Manager is resubmitting request ID: {}", requestId);
            // 1. Update DB with new data for the validation loop
            staffingRequestService.updateRequestDetails(requestId, updateData);
            
            // 2. Correlate Message with 'resubmit' decision
            managerDecisionService.completeReviewTask(requestId, "resubmit");
            return ResponseEntity.ok("Request " + requestId + " updated and resubmitted.");
        } else {
            log.info("Manager is cancelling request ID: {}", requestId);
            // 1. Update DB status to CANCELLED
            staffingRequestService.updateStatus(requestId, "CANCELLED");
            
            // 2. Correlate Message with 'cancel' decision
            managerDecisionService.completeReviewTask(requestId, "cancel");
            return ResponseEntity.ok("Request " + requestId + " has been cancelled.");
        }
    }
    
    @GetMapping("/rejected-requests")
    public ResponseEntity<List<StaffingRequest>> getRejectedRequests() {
        
        JwtAuthFilter.JwtPrincipal p = (JwtAuthFilter.JwtPrincipal) SecurityContextHolder
        .getContext()
        .getAuthentication()
        .getPrincipal();

        String role = p.selectedRole();
        if(!"ROLE_MANAGER".equals(role)) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You are not authorized to view this resource"
            );
        }
        log.info("Fetching all rejected staffing requests for manager review.");

        // Define the list of statuses that count as "Rejected/Needs Revision"
        List<RequestStatus> targetStatuses = List.of(
            RequestStatus.INT_EMPLOYEE_REJECTED_BY_DH,
            RequestStatus.EXT_EMPLOYEE_REJECTED_BY_DH,
            RequestStatus.INT_EMPLOYEE_REJECTED_BY_EMP,
            RequestStatus.REQUEST_REJECTED,
            RequestStatus.NO_EXT_EMPLOYEE_FOUND
        );

        List<StaffingRequest> rejectedRequests = staffingRequestRepository.findByStatusIn(targetStatuses);

        return ResponseEntity.ok(rejectedRequests);
    }

}