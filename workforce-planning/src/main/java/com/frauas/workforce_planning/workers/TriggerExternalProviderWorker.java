package com.frauas.workforce_planning.workers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.frauas.workforce_planning.config.Team3bConfig;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;

@Component
public class TriggerExternalProviderWorker {

    private final Team3bConfig team3bConfig;
    private final RestTemplate restTemplate;

    // Spring automatically injects your Team3bConfig and RestTemplate bean
    public TriggerExternalProviderWorker(Team3bConfig team3bConfig, RestTemplate restTemplate) {
        this.team3bConfig = team3bConfig;
        this.restTemplate = restTemplate;
    }

    @JobWorker(type = "notify-group-3b")
    public void triggerExternalProvider(final JobClient client, final ActivatedJob job) {
        // 1. Get the target URL from our new YAML config
        String targetUrl = team3bConfig.getFullUrl();

        // 2. Extract process variables sent from the Resource Planner / BPMN
        Map<String, Object> variables = job.getVariablesAsMap();
        
        Object idObj = variables.get("requestId");
        Long requestId = (idObj instanceof Number) ? ((Number) idObj).longValue() : Long.valueOf(idObj.toString());
        String jobTitle = (String) variables.get("jobTitle");
        Object skills = variables.get("requiredSkills");
        Object wage = variables.get("wagePerHour");

        System.out.println(">>> [WORKER] Triggering External Search for Request ID: " + requestId);
        System.out.println(">>> [WORKER] Sending to Group 3b URL: " + targetUrl);

        // 3. Prepare the payload for Group 3b
        Map<String, Object> requestPayload = new HashMap<>();
        requestPayload.put("internalRequestId", requestId);
        requestPayload.put("jobTitle", jobTitle);
        requestPayload.put("requiredSkills", skills);
        requestPayload.put("wagePerHour", wage);
        requestPayload.put("status", "OPEN_FOR_EXTERNAL_PROVISION");

        // 4. Perform the REST POST call
        try {
            // This sends the JSON to Group 3b's API
            restTemplate.postForEntity(targetUrl, requestPayload, String.class);
            
            System.out.println(">>> [WORKER] Successfully notified Group 3b for " + jobTitle);
            
            // Task completes automatically because autoComplete=true is default in Spring Zeebe
        } catch (Exception e) {
            System.err.println(">>> [WORKER] Failed to contact Group 3b: " + e.getMessage());
            
            // Throwing an exception here creates an Incident in Camunda Operate for you to see
            throw new RuntimeException("External Provider Communication Failed: " + e.getMessage());
        }
    }
}