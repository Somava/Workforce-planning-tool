package com.frauas.workforce_planning.workers;

import com.frauas.workforce_planning.dto.ExternalDecisionRequestDTO;
import com.frauas.workforce_planning.services.DecisionIntegrationService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class NotifyExternalGroupWorker {

  private final DecisionIntegrationService decisionIntegrationService;

  public NotifyExternalGroupWorker(DecisionIntegrationService decisionIntegrationService) {
    this.decisionIntegrationService = decisionIntegrationService;
  }

  @JobWorker(type = "notifyExternalGroup") // MUST match BPMN task type
  public void notifyExternalGroup(final JobClient client, final ActivatedJob job) {

    Map<String, Object> vars = job.getVariablesAsMap();

    // 1) read requestId from Camunda variables and convert to Long
    Long internalRequestId = toLong(vars.get("requestId"));
    if (internalRequestId == null) {
      // If requestId is missing, fail the job (or you can complete with log)
      throw new IllegalArgumentException("Missing process variable: requestId");
    }

    // 2) decide accepted/rejected based on externalResourceFound
    boolean externalFound = Boolean.TRUE.equals(vars.get("externalResourceFound"));

    // 3) Build payload (matches your DTO types)
    ExternalDecisionRequestDTO payload = new ExternalDecisionRequestDTO(
        internalRequestId,
        externalFound ? "ACCEPTED" : "REJECTED",
        externalFound ? null : "No suitable external resource found"
    );

    // 4) Call integration (will POST to Team 3B decision endpoint if configured)
    decisionIntegrationService.sendDecision(payload);

    // 5) Complete job
    client.newCompleteCommand(job.getKey())
        .send()
        .join();
  }

  /**
   * Safely converts Camunda variable to Long.
   * Handles Number (Integer/Long) and String.
   */
  private Long toLong(Object value) {
    if (value == null) return null;
    if (value instanceof Number n) return n.longValue();
    String s = value.toString().trim();
    if (s.isEmpty()) return null;
    return Long.valueOf(s);
  }
}

