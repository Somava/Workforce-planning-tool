package com.frauas.workforce_planning.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.frauas.workforce_planning.model.entity.RequestEntity;
import com.frauas.workforce_planning.model.enums.RequestStatus;
import com.frauas.workforce_planning.repository.RequestRepository;
import com.frauas.workforce_planning.repository.StaffingRequestRepository;
import com.frauas.workforce_planning.model.entity.StaffingRequest;

import io.camunda.zeebe.client.ZeebeClient;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "originPatterns = \"*\", allowCredentials = \"true\"")
public class TaskController {

    @Autowired
    private ZeebeClient zeebeClient;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private StaffingRequestRepository staffingRequestRepository;


    /**
     * Approves a request with authorization check.
     */
    @PostMapping("/approve/{userTaskId}")
    public ResponseEntity<String> approveRequest(
            @PathVariable Long userTaskId,
            @RequestParam Long requestId,
            @RequestParam Long deptHeadId) { // Added deptHeadId

        RequestEntity entity = requestRepository.findByRequestId(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        // AUTHORIZATION: Verify this user is the head of the request's department
        if (!entity.getDepartment().getDepartmentHeadUserId().equals(deptHeadId)) {
            return ResponseEntity.status(403).body("You are not authorized to approve this request.");
        }

        // 1. Update the Database Status
        entity.setStatus(RequestStatus.PUBLISHED);
        requestRepository.save(entity);

        // 2. Tell Camunda the Human is done
        Map<String, Object> variables = new HashMap<>();
        variables.put("deptHeadApproved", true);
        zeebeClient.newCompleteCommand(userTaskId)
                .variables(variables)
                .send()
                .join();
        
        return ResponseEntity.ok("Request " + requestId + " approved by " + deptHeadId);
    }

    /**
     * Rejects a request with authorization check.
     */
    @PostMapping("/reject/{userTaskId}")
    public ResponseEntity<String> rejectRequest(
            @PathVariable Long userTaskId,
            @RequestParam Long requestId,
            @RequestParam String deptHeadId) { // Added deptHeadId

        RequestEntity entity = requestRepository.findByRequestId(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        // AUTHORIZATION: Verify this user is the head of the request's department
        if (!entity.getDepartment().getDepartmentHeadUserId().equals(deptHeadId)) {
            return ResponseEntity.status(403).body("You are not authorized to reject this request.");
        }

        // 1. Update Database
        entity.setStatus(RequestStatus.DRAFT);
        entity.setValidationError("Rejected by Dept Head: " + deptHeadId);
        requestRepository.save(entity);

        // 2. Tell Camunda to move back
        Map<String, Object> variables = new HashMap<>();
        variables.put("deptHeadApproved", false);
        zeebeClient.newCompleteCommand(userTaskId)
                .variables(variables)
                .send()
                .join();
        
        return ResponseEntity.ok("Request " + requestId + " rejected and sent back to Manager.");
    }

    /**
     * Filters pending approvals based on the logged-in Department Head.
     */
    // @GetMapping("/dept-head")
    // public ResponseEntity<List<RequestEntity>> getPendingApprovals(@RequestParam Long departmentHeadUserId) {
    //     // You need to create this method in your RequestRepository
    //     // List<RequestEntity> pendingRequests = requestRepository
    //     //     .findAllByStatusAndDepartment_DepartmentHeadUserId(RequestStatus.PENDING_APPROVAL, departmentHeadUserId);

    //     List<RequestEntity> pendingRequests = requestRepository
    //         .findPendingRequestsByDeptHead(RequestStatus.PENDING_APPROVAL, departmentHeadUserId);
        
    //     return ResponseEntity.ok(pendingRequests);
    // }

    @GetMapping("/dept-head")
    public ResponseEntity<List<StaffingRequest>> getPendingApprovals(@RequestParam Long departmentHeadUserId) {
        var pending = staffingRequestRepository.findPendingByDeptHead(RequestStatus.PENDING_APPROVAL, departmentHeadUserId);
        return ResponseEntity.ok(pending);
    }

}