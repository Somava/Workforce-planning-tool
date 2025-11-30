package com.frauas.workforce_planning;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.spring.client.annotation.JobWorker;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;

@Component
public class ValidateRequestDataWorker {

    @JobWorker(type = "validate-request-data")
    public void validateData(final JobClient client, final ActivatedJob job) {
        
        Map<String, Object> variables = job.getVariablesAsMap();
        boolean isValid = true;
        
        // 1. Retrieve Variables 
        String projectName = (String) variables.get("projectName");
        String requiredSkills = (String) variables.get("requiredSkills"); 
        String startDateString = (String) variables.get("startDate");
        String workloadString = (String) variables.get("workload"); 

        // --- 2. Validation Logic ---
        
        // A. Check for required fields 
        if (projectName == null || projectName.trim().isEmpty() || 
            requiredSkills == null || requiredSkills.trim().isEmpty() || 
            startDateString == null || workloadString == null) {
            
            System.out.println("Validation Failed: Missing required fields (Project Name, Skills, Start Date, or Workload).");
            isValid = false;
        }

        // B. Date and Workload Specific Checks (Only run if required fields are present)
        if (isValid) {
            try {
                // Parse Dates
                LocalDate startDate = LocalDate.parse(startDateString);
                long workload = Long.parseLong(workloadString); // Parse Workload

                // 1. NEW Check: Future Date Check (Start Date must be after today)
                if (!startDate.isAfter(LocalDate.now())) {
                    System.out.println("Validation Failed: Start Date must be in the future (after today).");
                    isValid = false;
                }
                
                // 2. NEW Check: Workload Range Check (Must be between 1 and 40 hours)
                if (workload <= 0 || workload > 40) {
                    System.out.println("Validation Failed: Workload must be between 1 and 40 hours/week.");
                    isValid = false;
                }

            } catch (Exception e) {
                // Catches number parsing errors (for workload) or invalid date formats
                System.out.println("Validation Failed: Invalid number or date format in input fields.");
                isValid = false;
            }
        }
        
        // --- 3. Complete the Job ---

        client.newCompleteCommand(job.getKey())
              .variable("dataValid", isValid) 
              .send()
              .join();
    }
}