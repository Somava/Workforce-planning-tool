package com.frauas.workforce_planning.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.frauas.workforce_planning.dto.StaffingRequestDTO;
import com.frauas.workforce_planning.dto.StaffingRequestUpdateDTO;
import com.frauas.workforce_planning.dto.WorkforceRequestDTO;
import com.frauas.workforce_planning.model.entity.StaffingRequest;
import com.frauas.workforce_planning.model.enums.RequestStatus;
import com.frauas.workforce_planning.repository.StaffingRequestRepository;
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
    private final StaffingRequestService staffingService;

    public ManagerController(StaffingRequestRepository staffingRequestRepository,
                             ManagerDecisionService managerDecisionService,
                             StaffingRequestService staffingService) {
        this.staffingRequestRepository = staffingRequestRepository;
        this.managerDecisionService = managerDecisionService;
        this.staffingService = staffingService;
    }

    @PostMapping("/create-staffing-requests")
    public ResponseEntity<StaffingRequestDTO> createRequest(@RequestBody WorkforceRequestDTO dto) {
        JwtAuthFilter.JwtPrincipal p = getPrincipal();
        validateRole(p, "ROLE_MANAGER");

        StaffingRequest savedRequest = staffingService.createAndStartRequest(dto, p.userId());
        // Map to DTO for the response to keep things consistent and recursion-free
        return ResponseEntity.ok(staffingService.mapToDTO(savedRequest));
    }

    @GetMapping("/all-staffing-requests")
    public ResponseEntity<List<StaffingRequestDTO>> getManagerRequests() {
        JwtAuthFilter.JwtPrincipal p = getPrincipal();
        validateRole(p, "ROLE_MANAGER");

        log.info("Fetching requests created by manager: {}", p.email());
        
        List<StaffingRequestDTO> dtos = staffingService.getRequestsByManagerEmail(p.email())
                .stream()
                .map(staffingService::mapToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/staffing-request/review-decision")
    public ResponseEntity<String> handleManagerDecision(
            @RequestParam Long requestId,
            @RequestParam boolean isResubmit,
            @RequestBody(required = false) StaffingRequestUpdateDTO updateData) {
        
        validateRole(getPrincipal(), "ROLE_MANAGER");

        if (isResubmit) {
            log.info("Manager is resubmitting request ID: {}", requestId);
            staffingService.updateRequestDetails(requestId, updateData);
            managerDecisionService.completeReviewTask(requestId, "resubmit");
            return ResponseEntity.ok("Request " + requestId + " updated and resubmitted.");
        } else {
            log.info("Manager is cancelling request ID: {}", requestId);
            staffingService.updateStatus(requestId, "CANCELLED");
            managerDecisionService.completeReviewTask(requestId, "cancel");
            return ResponseEntity.ok("Request " + requestId + " has been cancelled.");
        }
    }

    @GetMapping("/rejected-requests")
    public ResponseEntity<List<StaffingRequestDTO>> getRejectedRequests() {
        validateRole(getPrincipal(), "ROLE_MANAGER");
        log.info("Fetching all rejected staffing requests for manager review.");

        List<RequestStatus> targetStatuses = List.of(
            RequestStatus.INT_EMPLOYEE_REJECTED_BY_DH,
            RequestStatus.EXT_EMPLOYEE_REJECTED_BY_DH,
            RequestStatus.INT_EMPLOYEE_REJECTED_BY_EMP,
            RequestStatus.REQUEST_REJECTED,
            RequestStatus.NO_EXT_EMPLOYEE_FOUND
        );

        List<StaffingRequestDTO> rejectedDtos = staffingRequestRepository.findByStatusIn(targetStatuses)
                .stream()
                .map(staffingService::mapToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(rejectedDtos);
    }

    // --- Helper Methods ---

    private JwtAuthFilter.JwtPrincipal getPrincipal() {
        return (JwtAuthFilter.JwtPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }

    private void validateRole(JwtAuthFilter.JwtPrincipal p, String requiredRole) {
        if (!requiredRole.equals(p.selectedRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized role");
        }
    }
}