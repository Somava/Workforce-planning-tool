package com.frauas.workforce_planning.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.frauas.workforce_planning.dto.StaffingRequestUpdateDTO;
import com.frauas.workforce_planning.model.entity.StaffingRequest;
import com.frauas.workforce_planning.model.entity.User;
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
@CrossOrigin(origins = "http://localhost:3000")
public class ManagerController {

    private final UserRepository userRepository;
    private final StaffingRequestRepository staffingRequestRepository;
    private final ManagerDecisionService managerDecisionService;
    private final StaffingRequestService staffingRequestService;

    public ManagerController(UserRepository userRepository,
                             StaffingRequestRepository staffingRequestRepository,
                             ManagerDecisionService managerDecisionService,
                             StaffingRequestService staffingRequestService) {
        this.userRepository = userRepository;
        this.staffingRequestRepository = staffingRequestRepository;
        this.managerDecisionService = managerDecisionService;
        this.staffingRequestService = staffingRequestService;
    }

    /**
     * 1. GET all requests for a specific manager
     * Used for the Manager Dashboard view.
     */
    @GetMapping("/requests")
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

    /**
     * 2. GET specific Request Details for Editing
     * Used when the manager clicks "Edit" on a rejected request.
     */
    @GetMapping("/staffing-request")
    public ResponseEntity<StaffingRequest> getRequestForRevision(@RequestParam Long requestId) {

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

        log.info("Fetching request details for revision. ID: {}", requestId);
        return staffingRequestService.getById(requestId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
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

    @GetMapping("/int-employee-rejected")
    public ResponseEntity<List<StaffingRequest>> getAllIntEmployeeRejectedRequests() {

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

        List<StaffingRequest> rejected =
            staffingRequestRepository.findByStatus(RequestStatus.INT_EMPLOYEE_REJECTED_BY_DH);
        
            if (rejected.isEmpty()) {
                return ResponseEntity.ok()
                    .header("X-Info", "No rejected staffing requests found")
                    .body(List.of());
            }

        return ResponseEntity.ok(rejected);
    }

}