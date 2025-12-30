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
    // 1) validate mandatory fields
    if (dto.internalRequestId() == null || dto.internalRequestId().isBlank()) {
      return ResponseEntity.badRequest().body(Map.of("error", "internalRequestId is mandatory"));
    }
    if (dto.externalResourceFound() == null) {
      return ResponseEntity.badRequest().body(Map.of("error", "externalResourceFound is mandatory"));
    }

    // 2) Map response -> Camunda variables
    Map<String, Object> vars = new HashMap<>();
    vars.put("requestId", dto.internalRequestId()); // keep consistent
    vars.put("externalResourceFound", dto.externalResourceFound());

    if (Boolean.TRUE.equals(dto.externalResourceFound())) {
      vars.put("externalEmployeeId", dto.externalEmployeeId());
    }

    // 3) Publish message to release "Await External Response"
    zeebeClient.newPublishMessageCommand()
        .messageName("ExternalResourceResponse")     // must match BPMN message name
        .correlationKey(dto.internalRequestId())     // must match =requestId
        .variables(vars)
        .send()
        .join();

    // 4) Return ack to caller (3B or your mock)
    return ResponseEntity.ok(Map.of(
        "status", "PUBLISHED_TO_CAMUNDA",
        "internalRequestId", dto.internalRequestId()
    ));
  }
}
