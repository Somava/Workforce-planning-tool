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
public class ExternalNotificationWorker {

    private final Team3bConfig team3bConfig;
    private final RestTemplate restTemplate;

    public ExternalNotificationWorker(Team3bConfig team3bConfig, RestTemplate restTemplate) {
        this.team3bConfig = team3bConfig;
        this.restTemplate = restTemplate;
    }

    @JobWorker(type = "notify-external-group")
    public void handleNotification(final JobClient client, final ActivatedJob job) {
        // 1. Get variables from the process
        Map<String, Object> variables = job.getVariablesAsMap();
        
        // Ensure we use the correct variable name "requestId" as saved in the process
        Long requestId = Long.valueOf(variables.get("requestId").toString());
        
        // 2. Determine decision based on the BPMN Element ID
        // This allows you to avoid Modeler changes like Input Mappings or Headers.
        // It checks if the Task ID in your BPMN contains the word "Accepted"
        String elementId = job.getElementId(); 
        String decision = elementId.toLowerCase().contains("accepted") ? "ACCEPTED" : "REJECTED";

        // 3. Prepare the JSON payload for Group 3b
        Map<String, Object> payload = new HashMap<>();
        payload.put("requestId", requestId);
        payload.put("decision", decision);

        // 4. Send POST request using the URL from your Team3bConfig/application.yml
        String url = team3bConfig.getDecisionUrl();
        
        try {
            restTemplate.postForEntity(url, payload, String.class);
            System.out.println(">>> [WORKER] Sent decision " + decision + " for Request ID: " + requestId);
            
            // 5. Complete the job in Camunda
            client.newCompleteCommand(job.getKey()).send().join();
        } catch (Exception e) {
            // Fail the job so it retries based on Camunda settings
            client.newFailCommand(job.getKey())
                .retries(job.getRetries() - 1)
                .errorMessage("Failed to notify Group 3b of decision: " + e.getMessage())
                .send().join();
        }
    }
}