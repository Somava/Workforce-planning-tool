package com.frauas.workforce_planning.workers;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class TriggerExternalProviderWorker {

  // Put Team 3B base URL in application.yaml (for now you can keep localhost mock)
  @Value("${team3b.base-url:http://localhost:8080}")
  private String team3bBaseUrl;

  private final RestTemplate restTemplate = new RestTemplate();

  @JobWorker(type = "serviceMgmtConnector")
  public void triggerExternalProvider(final JobClient client, final ActivatedJob job) {

    Map<String, Object> vars = job.getVariablesAsMap();

    // 1) Correlation key MUST match BPMN: correlationKey="=requestId"
    String requestId = (String) vars.get("requestId");
    if (requestId == null || requestId.isBlank()) {
      requestId = "REQ-" + job.getProcessInstanceKey();
    }

    //  3B identify which subteam (1B) sent the request
    String internalRequestId = "G1B-" + requestId;

    // 2) Build payload EXACTLY in 3B names (latest contract)
    Map<String, Object> payloadFor3b = new HashMap<>();
    payloadFor3b.put("internalRequestId", internalRequestId);

    // These must exist (mandatory in their table). If your vars differ, update the key names below.
    payloadFor3b.put("title", safeString(vars.get("title"), safeString(vars.get("positionName"), "External resource needed")));
    payloadFor3b.put("description", safeString(vars.get("description"), "Internal search failed. Requesting external expert."));

    // Optional fields
    payloadFor3b.put("requiredSkills", safeString(vars.get("requiredSkills"), safeString(vars.get("employeeSkills"), null)));
    payloadFor3b.put("startDate", safeString(vars.get("startDate"), null)); // "YYYY-MM-DD"
    payloadFor3b.put("endDate", safeString(vars.get("endDate"), null));     // "YYYY-MM-DD"
    payloadFor3b.put("projectContext", safeString(vars.get("projectContext"), safeString(vars.get("targetProject"), null)));
    payloadFor3b.put("Performance Loc", safeString(vars.get("performanceLoc"), "Onshore"));

    // 3) POST to 3B endpoint (contract)
    String url = team3bBaseUrl + "/api/group1/workforce-request";
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payloadFor3b, headers);

      ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);

      System.out.println("[TriggerExternalProvider] Sent request to 3B: " + url);
      System.out.println("[TriggerExternalProvider] Payload: " + payloadFor3b);
      System.out.println("[TriggerExternalProvider] 3B response: " + response.getStatusCode() + " body=" + response.getBody());

    } catch (Exception ex) {
      // For sprint/demo: don't crash process; just log error and continue waiting (or you can throw to fail job)
      System.out.println("[TriggerExternalProvider] ERROR calling 3B endpoint: " + ex.getMessage());
    }

    // 4) Save correlation + flags back into Camunda variables so process continues to Await External Response
    Map<String, Object> out = new HashMap<>();
    out.put("requestId", requestId);                 // MUST exist for message correlation
    out.put("externalRequestSent", true);
    out.put("internalRequestId", internalRequestId); // helpful for traceability in your process

    client.newCompleteCommand(job.getKey())
        .variables(out)
        .send()
        .join();
  }

  private String safeString(Object value, String fallback) {
    if (value == null) return fallback;
    String s = String.valueOf(value);
    return s.isBlank() ? fallback : s;
  }
}
