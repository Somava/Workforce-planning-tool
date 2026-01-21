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
  // 400
  if (dto.internalRequestId() == null) {
    return ResponseEntity.badRequest()
        .body(Map.of("error", "internalRequestId is mandatory"));
  }

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
    vars.put("externalProjectId", dto.projectId());
  }

  // 500 (explicit)
  try {
    zeebeClient.newPublishMessageCommand()
        .messageName("ExternalResourceResponse")
        .correlationKey(String.valueOf(dto.internalRequestId()))
        .variables(vars)
        .send()
        .join();
  } catch (Exception e) {
    return ResponseEntity.internalServerError()
        .body(Map.of("error", "FAILED_TO_PUBLISH_MESSAGE"));
  }

  // 200
  return ResponseEntity.ok(Map.of(
      "status", "PUBLISHED_TO_CAMUNDA",
      "internalRequestId", dto.internalRequestId(),
      "externalResourceFound", found
  ));
}
}
