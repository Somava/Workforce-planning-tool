package com.frauas.workforce_planning.controller;

import java.util.HashMap;
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

        // 3. Update Status in Supabase (Staffing Request Table)
        staffingRequestRepository.findByRequestId(dto.staffingRequestId())
            .ifPresent(request -> {
                request.setStatus(RequestStatus.EXTERNAL_RESPONSE_RECEIVED);
                staffingRequestRepository.save(request);
            });

        boolean found = dto.externalEmployeeId() != null && !dto.externalEmployeeId().isBlank();

        // 4. Save Directly to external_employee Table
        if (found) {
            ExternalEmployee employee = ExternalEmployee.builder()
                .externalId(dto.externalEmployeeId())
                .provider(dto.provider())
                .firstName(dto.firstName())
                .lastName(dto.lastName())
                .email(dto.email())
                .wagePerHour(dto.wagePerHour())
                .skills(dto.skills())
                .experienceYears(dto.experienceYears())
                .staffingRequestId(dto.staffingRequestId())
                .build();
            
            externalEmployeeRepository.save(employee);
        }

        // 5. Prepare Variables for Camunda
        Map<String, Object> vars = new HashMap<>();
        vars.put("requestId", dto.staffingRequestId()); 
        vars.put("externalResourceFound", found);

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