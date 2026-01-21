package com.frauas.workforce_planning.workers;

import com.frauas.workforce_planning.dto.ExternalWorkforce3BRequestDTO;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class TriggerExternalProviderWorker {

  //@Value("${team3b.base-url:http://localhost:8080}")
 @Value("${team3b.base-url:}")
private String team3bBaseUrl;

  private final RestTemplate restTemplate = new RestTemplate();

  @JobWorker(type = "serviceMgmtConnector")
  public void triggerExternalProvider(final JobClient client, final ActivatedJob job) {
    if (team3bBaseUrl == null || team3bBaseUrl.isBlank()) {
  throw new RuntimeException(
      "team3b.base-url is not configured (3B URL missing)"
  );
}


    Map<String, Object> vars = job.getVariablesAsMap();

    // ✅ 1) Use numeric internalRequestId (must match BPMN correlation key and 3B response)
    Long internalRequestId = toLong(vars.get("internalRequestId"));
    if (internalRequestId == null) {
      // fallback: still allow demo, but it's better to set internalRequestId earlier in process
      internalRequestId = job.getProcessInstanceKey(); // unique number
    }

    Long projectId = toLong(vars.get("projectId"));
    String projectName = safeString(vars.get("projectName"), "Unknown Project");
    String jobTitle = safeString(vars.get("jobTitle"), safeString(vars.get("title"), "External resource needed"));
    String description = safeString(vars.get("description"), "Internal search failed. Requesting external expert.");

    Integer availabilityHoursPerWeek = toInt(vars.get("availabilityHoursPerWeek"));
    if (availabilityHoursPerWeek == null) availabilityHoursPerWeek = toInt(vars.get("availabilityHours"));
    if (availabilityHoursPerWeek == null) availabilityHoursPerWeek = 40;

    Double wagePerHour = toDouble(vars.get("wagePerHour"));
    if (wagePerHour == null) wagePerHour = 0.0;

    Integer experienceYears = toInt(vars.get("experienceYears"));
    if (experienceYears == null) experienceYears = 1;

    String location = safeString(vars.get("location"), "Remote");
    String projectContext = safeString(vars.get("projectContext"), null);
    String startDate = safeString(vars.get("startDate"), null);
    String endDate = safeString(vars.get("endDate"), null);

    List<String> skills = toSkillsList(vars.get("skills"));
    if (skills.isEmpty()) skills = toSkillsList(vars.get("requiredSkills"));

    ExternalWorkforce3BRequestDTO payload = new ExternalWorkforce3BRequestDTO(
        internalRequestId,
        projectId,
        projectName,
        jobTitle,
        description,
        availabilityHoursPerWeek,
        wagePerHour,
        skills,
        experienceYears,
        location,
        projectContext,
        startDate,
        endDate
    );

    String url = team3bBaseUrl + "/api/group1/workforce-request";
HttpHeaders headers = new HttpHeaders();
headers.setContentType(MediaType.APPLICATION_JSON);

HttpEntity<ExternalWorkforce3BRequestDTO> requestEntity =
    new HttpEntity<>(payload, headers);

ResponseEntity<String> response;
try {
  response = restTemplate.postForEntity(url, requestEntity, String.class);
} catch (Exception ex) {
  // ✅ IMPORTANT: do NOT complete the job
  // Zeebe will retry / raise incident
  throw new RuntimeException(
      "[TriggerExternalProvider] ERROR calling 3B endpoint: " + url, ex
  );
}

// ✅ Accept ANY 2xx (200 / 201 / 202)
if (!response.getStatusCode().is2xxSuccessful()) {
  throw new RuntimeException(
      "[TriggerExternalProvider] 3B returned non-2xx: "
          + response.getStatusCode()
          + " body=" + response.getBody()
  );
}

System.out.println("[TriggerExternalProvider] Sent request to 3B: " + url);
System.out.println("[TriggerExternalProvider] Payload: " + payload);
System.out.println("[TriggerExternalProvider] 3B response: "
    + response.getStatusCode()
    + " body=" + response.getBody());

// ✅ ONLY NOW complete the job
Map<String, Object> out = new HashMap<>();
out.put("internalRequestId", internalRequestId);
out.put("externalRequestSent", true);
out.put("externalAckStatus", response.getStatusCodeValue());

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

  private Long toLong(Object v) {
    if (v == null) return null;
    if (v instanceof Number n) return n.longValue();
    try { return Long.parseLong(String.valueOf(v)); } catch (Exception e) { return null; }
  }

  private Integer toInt(Object v) {
    if (v == null) return null;
    if (v instanceof Number n) return n.intValue();
    try { return Integer.parseInt(String.valueOf(v)); } catch (Exception e) { return null; }
  }

  private Double toDouble(Object v) {
    if (v == null) return null;
    if (v instanceof Number n) return n.doubleValue();
    try { return Double.parseDouble(String.valueOf(v)); } catch (Exception e) { return null; }
  }

  private List<String> toSkillsList(Object v) {
    if (v == null) return List.of();
    if (v instanceof List<?> list) {
      return list.stream().map(String::valueOf).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
    }
    String s = String.valueOf(v);
    if (s.isBlank()) return List.of();
    return Arrays.stream(s.split(",")).map(String::trim).filter(x -> !x.isEmpty()).collect(Collectors.toList());
  }
}
