package com.frauas.workforce_planning.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.frauas.workforce_planning.dto.StaffingRequestUpdateDTO;
import com.frauas.workforce_planning.model.entity.StaffingRequest;
import com.frauas.workforce_planning.model.enums.RequestStatus;
import com.frauas.workforce_planning.repository.StaffingRequestRepository;
import com.frauas.workforce_planning.services.ManagerDecisionService;
import com.frauas.workforce_planning.services.StaffingRequestService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/manager")
@CrossOrigin(origins = "http://localhost:3000")
public class ManagerController {

    @Autowired
    private StaffingRequestService staffingRequestService;

    @Autowired
    private ManagerDecisionService managerDecisionService;

    @Autowired
    private StaffingRequestRepository staffingRequestRepository;

    /**
     * 1. GET all requests for a specific manager
     * Used for the Manager Dashboard view.
     */
    @GetMapping("/manager-requests")
    public ResponseEntity<List<StaffingRequest>> getManagerRequests(@RequestParam String email) {
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
        log.info("Fetching request details for revision. ID: {}", requestId);
        return staffingRequestService.getById(requestId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/staffing-request/review-decision")
    public ResponseEntity<String> handleManagerDecision(
            @RequestParam Long requestId,
            @RequestParam String email,
            @RequestParam boolean isResubmit,
            @RequestBody(required = false) StaffingRequestUpdateDTO updateData) {
        
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
    
    @GetMapping("/manager/rejected-requests")
    public ResponseEntity<List<StaffingRequest>> getRejectedRequests(@RequestParam String email) {
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