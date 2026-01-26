package com.frauas.workforce_planning.controller;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.frauas.workforce_planning.dto.ExternalWorkforceResponseDTO;
import com.frauas.workforce_planning.model.entity.ExternalEmployee;
import com.frauas.workforce_planning.model.enums.RequestStatus;
import com.frauas.workforce_planning.repository.ExternalEmployeeRepository;
import com.frauas.workforce_planning.repository.StaffingRequestRepository;

import io.camunda.zeebe.client.ZeebeClient;

@RestController
@RequestMapping("/api/group3b")
public class ExternalResponseController {

    private final ZeebeClient zeebeClient;
    private final StaffingRequestRepository staffingRequestRepository;
    private final ExternalEmployeeRepository externalEmployeeRepository; // 1. Added Repository

    public ExternalResponseController(ZeebeClient zeebeClient, 
                                    StaffingRequestRepository staffingRequestRepository,
                                    ExternalEmployeeRepository externalEmployeeRepository) {
        this.zeebeClient = zeebeClient;
        this.staffingRequestRepository = staffingRequestRepository;
        this.externalEmployeeRepository = externalEmployeeRepository; // 2. Injected Repository
    }

    @PostMapping("/workforce-response")
    public ResponseEntity<Map<String, Object>> receiveExternalResponse(
        @RequestBody ExternalWorkforceResponseDTO dto
    ) {
        if (dto.staffingRequestId() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "staffingRequestId is mandatory"));
        }

        
        boolean found = dto.externalEmployeeId() != null && !dto.externalEmployeeId().isBlank();

        // 3. Update Status in Supabase (Staffing Request Table)
        staffingRequestRepository.findByRequestId(dto.staffingRequestId())
        .ifPresent(request -> {
            if (found) {
                request.setStatus(RequestStatus.EXTERNAL_RESPONSE_RECEIVED);
            } else {
                // Updated to the specific status found in your logs
                request.setStatus(RequestStatus.NO_EXT_EMPLOYEE_FOUND);
                request.setRejectionType("EXT_EMPLOYEE_NOT_FOUND");
            }
            staffingRequestRepository.save(request);
        });

        // 4. Save Directly to external_employee Table
        if (found) {
            List<String> skillsList = (dto.skills() != null && !dto.skills().isBlank())
            ? Arrays.stream(dto.skills().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList()
            : Collections.emptyList();

            ExternalEmployee employee = ExternalEmployee.builder()
            .externalEmployeeId(dto.externalEmployeeId())
            .provider(dto.provider())
            .contractId(dto.contractId())
            .firstName(dto.firstName())
            .lastName(dto.lastName())
            .email(dto.email())
            .skills(skillsList)
            .evaluationScore(dto.evaluationScore())
            .experienceYears(dto.experienceYears())
            .wagePerHour(dto.wagePerHour())
            .staffingRequestId(dto.staffingRequestId())
            .projectId(dto.projectId())
            .status("PENDING")
            .build();
    
        externalEmployeeRepository.saveAndFlush(employee);
        }

        // 5. Prepare Variables for Camunda
        Map<String, Object> vars = new HashMap<>();
        vars.put("requestId", dto.staffingRequestId()); 
        vars.put("externalResourceFound", found);
        vars.put("isExternalCandidate", true);

        if (found) {
            vars.put("externalEmployeeId", dto.externalEmployeeId());
            vars.put("externalFirstName", dto.firstName());
            vars.put("externalLastName", dto.lastName());
            vars.put("externalEmail", dto.email());
        }

        // 6. Publish to Zeebe
        try {
            zeebeClient.newPublishMessageCommand()
                .messageName("ExternalResourceResponse")
                .correlationKey(String.valueOf(dto.staffingRequestId()))
                .variables(vars)
                .send()
                .join();
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "DB_UPDATED_BUT_BPMN_NOTIFICATION_FAILED"));
        }

        return ResponseEntity.ok(Map.of(
            "status", "SUCCESS",
            "staffingRequestId", dto.staffingRequestId(),
            "employeeSaved", found
        ));
    }
}