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
    // 1) validate mandatory fields from 3B
    if (dto.internalRequestId() == null) {
      return ResponseEntity.badRequest().body(Map.of("error", "internalRequestId is mandatory"));
    }
    if (dto.status() == null || dto.status().isBlank()) {
      return ResponseEntity.badRequest().body(Map.of("error", "status is mandatory"));
    }

    boolean hired = "EXTERNAL_HIRED".equalsIgnoreCase(dto.status())
        && dto.expertDetails() != null;

    // 2) Variables to continue BPMN
    Map<String, Object> vars = new HashMap<>();
    vars.put("requestId", String.valueOf(dto.internalRequestId())); // must match BPMN correlation key variable
    vars.put("externalResourceFound", hired);

    if (hired) {
      vars.put("externalExpertName", dto.expertDetails().name());
      vars.put("externalExpertSupplier", dto.expertDetails().supplier());
      vars.put("externalExpertDailyRate", dto.expertDetails().dailyRate());
    }

    // 3) Publish message to release "Await External Response"
    zeebeClient.newPublishMessageCommand()
        .messageName("ExternalResourceResponse")               // must match BPMN message name
        .correlationKey(String.valueOf(dto.internalRequestId())) // must match requestId value in process
        .variables(vars)
        .send()
        .join();

    return ResponseEntity.ok(Map.of(
        "status", "PUBLISHED_TO_CAMUNDA",
        "internalRequestId", dto.internalRequestId(),
        "externalResourceFound", hired
    ));
  }
}
