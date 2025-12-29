package com.frauas.workforce_planning.workers;

import java.time.Instant;
import java.util.Map;

import org.springframework.stereotype.Component;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;

@Component
public class SaveFinalAssignmentWorker {

    @JobWorker(type = "save-final-assignment")
    public void saveFinalAssignment(final JobClient client, final ActivatedJob job) {

        Map<String, Object> variables = job.getVariablesAsMap();

        // 1. Read process variables
        String projectName = (String) variables.getOrDefault("projectName", "Unnamed Project");
        String assignedEmployeeId = (String) variables.get("assignedEmployeeId");
        String assignmentNotes = (String) variables.getOrDefault("assignmentNotes", "");
        String assignmentRole = (String) variables.getOrDefault("assignmentRole", "");
        String employeeDecision = (String) variables.get("employeeDecision");

        // 2. Safe numeric parsing for allocationPercent
        Integer allocationPercent = null;
        Object allocationObj = variables.get("allocationPercent");
        if (allocationObj instanceof Number n) {
            allocationPercent = n.intValue();
        } else if (allocationObj instanceof String s && !s.isBlank()) {
            try {
                allocationPercent = Integer.parseInt(s);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid allocationPercent format: " + s);
            }
        }

        // Optional: fallback if null
        if (allocationPercent == null) {
            allocationPercent = 0; // or throw an error depending on business rules
        }

        // 3. Safety check for employee decision
        if (!"confirmed".equalsIgnoreCase(employeeDecision)) {
            throw new RuntimeException("Employee has not confirmed the assignment");
        }

        // 4. Save assignment (placeholder)
        System.out.println("===== FINAL ASSIGNMENT SAVED =====");
        System.out.println("Project: " + projectName);
        System.out.println("Employee ID: " + assignedEmployeeId);
        System.out.println("Role: " + assignmentRole);
        System.out.println("Allocation: " + allocationPercent + "%");
        System.out.println("Notes: " + assignmentNotes);
        System.out.println("Saved At: " + Instant.now());

        // 5. Complete job
        client.newCompleteCommand(job.getKey())
                .variables(Map.of(
                        "finalAssignmentSaved", true,
                        "assignmentSavedAt", Instant.now().toString()
                ))
                .send()
                .join();
    }
}
