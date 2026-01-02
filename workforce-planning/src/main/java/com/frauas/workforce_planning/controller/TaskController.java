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

import io.camunda.zeebe.client.ZeebeClient;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")

public class TaskController {

    @Autowired
    private ZeebeClient zeebeClient;

    @Autowired
    private RequestRepository requestRepository;

    /**
     * Approves a request.
     * @param userTaskId This is the internal Camunda Task Key (sent from frontend)
     * @param requestId This is your custom DB Request ID (e.g. 20251234)
     */
    @PostMapping("/approve/{userTaskId}")
    public ResponseEntity<String> approveRequest(
            @PathVariable Long userTaskId,
            @RequestParam Long requestId) {

        // 1. Update the Database Status
        RequestEntity entity = requestRepository.findByRequestId(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        entity.setStatus(com.frauas.workforce_planning.model.enums.RequestStatus.PUBLISHED);
        requestRepository.save(entity);

        // 2. Tell Camunda the Human is done
        Map<String, Object> variables = new HashMap<>();
        variables.put("deptHeadApproved", true);
        zeebeClient.newCompleteCommand(userTaskId)
                .variables(variables)
                .send()
                .join();
        return ResponseEntity.ok("Request " + requestId + " has been approved and moved to Employee Search.");
    }

    /**
     * Rejects a request back to the Manager.
     */
    @PostMapping("/reject/{userTaskId}")
    public ResponseEntity<String> rejectRequest(
            @PathVariable Long userTaskId,
            @RequestParam Long requestId) {

        // 1. Update Database
        RequestEntity entity = requestRepository.findByRequestId(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        entity.setStatus(com.frauas.workforce_planning.model.enums.RequestStatus.DRAFT);
        entity.setValidationError("Rejected by Dept Head. Please revise.");
        requestRepository.save(entity);

        // 2. Tell Camunda to move back (isValid = false will trigger the gateway)
        Map<String, Object> variables = new HashMap<>();
        variables.put("deptHeadApproved", false);
        zeebeClient.newCompleteCommand(userTaskId)
                .variables(variables)
                .send()
                .join();
        return ResponseEntity.ok("Request " + requestId + " rejected and sent back to Manager.");
    }

    @GetMapping("/dept-head")
    public ResponseEntity<List<RequestEntity>> getPendingApprovals() {
        List<RequestEntity> pendingRequests = requestRepository.findAllByStatus(RequestStatus.PENDING_APPROVAL);
        return ResponseEntity.ok(pendingRequests);

    }

}