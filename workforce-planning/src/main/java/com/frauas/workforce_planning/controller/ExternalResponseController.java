package com.frauas.workforce_planning.controller;

import com.frauas.workforce_planning.dto.ExternalWorkforceResponseDTO;
import io.camunda.zeebe.client.ZeebeClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/group1")
public class ExternalResponseController {

  private final ZeebeClient zeebeClient;

  public ExternalResponseController(ZeebeClient zeebeClient) {
    this.zeebeClient = zeebeClient;
  }

  @PostMapping("/workforce-response")
  public ResponseEntity<Map<String, Object>> receiveExternalResponse(
      @RequestBody ExternalWorkforceResponseDTO dto
  ) {
    // 1) Validate mandatory fields (based on your latest contract)
    if (dto.internalRequestId() == null) {
      return ResponseEntity.badRequest().body(Map.of("error", "internalRequestId is mandatory"));
    }

    // "found" if externalEmployeeId exists (you can make this stricter if you want)
    boolean found = dto.externalEmployeeId() != null && !dto.externalEmployeeId().isBlank();

    // 2) Variables to continue BPMN
    Map<String, Object> vars = new HashMap<>();
    vars.put("internalRequestId", dto.internalRequestId());   // keep for traceability
    vars.put("externalResourceFound", found);

    // If found, pass the external employee details into the process
    if (found) {
      vars.put("externalEmployeeId", dto.externalEmployeeId());
      vars.put("externalProvider", dto.provider());
      vars.put("externalFirstName", dto.firstName());
      vars.put("externalLastName", dto.lastName());
      vars.put("externalEmail", dto.email());
      vars.put("externalWagePerHour", dto.wagePerHour());
      vars.put("externalSkills", dto.skills());
      vars.put("externalExperienceYears", dto.experienceYears());
      vars.put("externalProjectId", dto.projectId());
    }

    // 3) Publish message to release "Await External Response"
    zeebeClient.newPublishMessageCommand()
        .messageName("ExternalResourceResponse") // must match BPMN
        .correlationKey(String.valueOf(dto.internalRequestId())) // must match BPMN correlation key variable value
        .variables(vars)
        .send()
        .join();

    return ResponseEntity.ok(Map.of(
        "status", "PUBLISHED_TO_CAMUNDA",
        "internalRequestId", dto.internalRequestId(),
        "externalResourceFound", found
    ));
  }
}
