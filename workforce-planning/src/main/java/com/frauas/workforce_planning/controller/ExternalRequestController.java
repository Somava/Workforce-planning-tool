package com.frauas.workforce_planning.controller;

import com.frauas.workforce_planning.dto.ExternalWorkforceRequestDTO;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/group1")
public class ExternalRequestController {

  @PostMapping("/workforce-request")
  public Map<String, Object> receiveExternalWorkforceRequest(@RequestBody ExternalWorkforceRequestDTO dto) {

    // Minimal validation (mandatory fields)
    if (dto.internalRequestId() == null || dto.internalRequestId().isBlank()
        || dto.title() == null || dto.title().isBlank()
        || dto.description() == null || dto.description().isBlank()) {
      throw new IllegalArgumentException("internalRequestId, title, description are mandatory");
    }

    // For now: just acknowledge receipt (later you can save to DB / start Camunda process)
    Map<String, Object> ack = new HashMap<>();
    ack.put("status", "RECEIVED");
    ack.put("internalRequestId", dto.internalRequestId());
    return ack;
  }
}
