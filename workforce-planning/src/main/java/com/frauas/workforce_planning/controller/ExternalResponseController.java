package com.frauas.workforce_planning.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.frauas.workforce_planning.dto.ExternalWorkforceResponseDTO;
import com.frauas.workforce_planning.model.enums.RequestStatus;
import com.frauas.workforce_planning.repository.StaffingRequestRepository;

import io.camunda.zeebe.client.ZeebeClient;

@RestController
@RequestMapping("/api/group1")
public class ExternalResponseController {

    private final ZeebeClient zeebeClient;
    // Inject the repository
    private final StaffingRequestRepository staffingRequestRepository;

    public ExternalResponseController(ZeebeClient zeebeClient, StaffingRequestRepository staffingRequestRepository) {
        this.zeebeClient = zeebeClient;
        this.staffingRequestRepository = staffingRequestRepository;
    }

    @PostMapping("/workforce-response")
    public ResponseEntity<Map<String, Object>> receiveExternalResponse(
        @RequestBody ExternalWorkforceResponseDTO dto
    ) {
        if (dto.internalRequestId() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "internalRequestId is mandatory"));
        }

        // 1. Update Status in Supabase
        staffingRequestRepository.findByRequestId(dto.internalRequestId())
            .ifPresent(request -> {
                request.setStatus(RequestStatus.EXTERNAL_RESPONSE_RECEIVED);
                // Optionally save who the provider was in a notes field if you have one
                staffingRequestRepository.save(request);
            });

        boolean found = dto.externalEmployeeId() != null && !dto.externalEmployeeId().isBlank();

        Map<String, Object> vars = new HashMap<>();
        vars.put("internalRequestId", dto.internalRequestId());
        vars.put("externalResourceFound", found);

        if (found) {
            vars.put("externalEmployeeId", dto.externalEmployeeId());
            vars.put("externalProvider", dto.provider());
            vars.put("externalFirstName", dto.firstName());
            vars.put("externalLastName", dto.lastName());
            vars.put("externalEmail", dto.email());
            vars.put("externalWagePerHour", dto.wagePerHour());
            vars.put("externalSkills", dto.skills());
            vars.put("externalExperienceYears", dto.experienceYears());
        }

        // 2. Publish to Zeebe
        try {
            zeebeClient.newPublishMessageCommand()
                .messageName("ExternalResourceResponse")
                .correlationKey(String.valueOf(dto.internalRequestId()))
                .variables(vars)
                .send()
                .join();
        } catch (Exception e) {
            // Log error but the DB status already reflects that we got the data
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "DB_UPDATED_BUT_BPMN_NOTIFIED_FAILED"));
        }

        return ResponseEntity.ok(Map.of(
            "status", "SUCCESS",
            "internalRequestId", dto.internalRequestId(),
            "dbStatus", "EXTERNAL_RESPONSE_RECEIVED"
        ));
    }
}