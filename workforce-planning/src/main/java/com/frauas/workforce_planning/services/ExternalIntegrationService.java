// package com.frauas.workforce_planning.services;

// import java.util.Map;

// import org.springframework.http.ResponseEntity;
// import org.springframework.stereotype.Service;
// import org.springframework.web.client.RestTemplate;

// import io.camunda.zeebe.client.api.response.ActivatedJob;
// import io.camunda.zeebe.client.api.worker.JobClient;
// import io.camunda.zeebe.spring.client.annotation.JobWorker;

// @Service
// public class ExternalIntegrationService {

//     private final RestTemplate restTemplate = new RestTemplate();
    
//     // Replace with the actual URL provided by Group 3b
//     private final String GROUP_3B_ENDPOINT = "https://group3b-api.com/v1/external-requests";

//     @JobWorker(type = "notify-group-3b") // Matches the Type in your BPMN
//     public void handleExternalHandoff(final JobClient client, final ActivatedJob job) {
        
//         // 1. Fetch the data variables you published in StaffingDecisionService
//         Map<String, Object> variables = job.getVariablesAsMap();

//         try {
//             // 2. Practically sending the data to Group 3b
//             System.out.println("Forwarding Internal Request " + variables.get("internalRequestId") + " to Group 3b...");
            
//             ResponseEntity<String> response = restTemplate.postForEntity(GROUP_3B_ENDPOINT, variables, String.class);

//             if (response.getStatusCode().is2xxSuccessful()) {
//                 // 3. Complete the task in Camunda so the flow continues to "Await External Response"
//                 client.newCompleteCommand(job.getKey())
//                     .variables(Map.of("externalNotificationStatus", "SENT"))
//                     .send()
//                     .join();
//                 System.out.println("Group 3b successfully notified.");
//             } else {
//                 throw new RuntimeException("Group 3b API error: " + response.getStatusCode());
//             }

//         } catch (Exception e) {
//             // If the external API is down, Zeebe will handle retries based on your BPMN settings
//             System.err.println("Failed to reach Group 3b: " + e.getMessage());
            
//             client.newFailCommand(job.getKey())
//                 .retries(job.getRetries() - 1)
//                 .errorMessage(e.getMessage())
//                 .send()
//                 .join();
//         }
//     }
// }