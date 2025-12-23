package com.frauas.workforce_planning.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.frauas.workforce_planning.dto.WorkforceRequestDTO;
import com.frauas.workforce_planning.model.entity.RequestEntity;
import com.frauas.workforce_planning.services.StaffingRequestService;

@RestController
@RequestMapping("/api/requests")
public class WorkforceController {

    @Autowired
    private StaffingRequestService staffingService;

    @PostMapping("/create")
    public ResponseEntity<RequestEntity> createRequest(@RequestBody WorkforceRequestDTO dto) {
        // One single call to the service
        RequestEntity savedRequest = staffingService.createAndStartRequest(dto);
        return ResponseEntity.ok(savedRequest);
    }
}