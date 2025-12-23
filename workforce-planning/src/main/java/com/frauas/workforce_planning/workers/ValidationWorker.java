package com.frauas.workforce_planning.workers;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.frauas.workforce_planning.model.entity.RequestEntity;
import com.frauas.workforce_planning.repository.ProjectRepository;
import com.frauas.workforce_planning.repository.RequestRepository;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;

@Component
public class ValidationWorker {

    @Autowired
    private RequestRepository repository;
    
    // You'll need your ProjectRepository to check if the ID exists
    @Autowired 
    private ProjectRepository projectRepository; 

    @JobWorker(type = "validate-request")
    public Map<String, Object> validateRequest(final ActivatedJob job) {
        Long id = ((Number) job.getVariable("id")).longValue();
        RequestEntity entity = repository.findById(id).orElseThrow();

        StringBuilder errors = new StringBuilder();
        boolean isValid = true;

        // 1. Rule: Start Date not in the past
        if (entity.getStartDate().isBefore(LocalDate.now())) {
            errors.append("Start date cannot be in the past. ");
            isValid = false;
        }

        // 2. Rule: Start Date must be before End Date
        if (entity.getStartDate().isAfter(entity.getEndDate())) {
            errors.append("Start date must be before the end date. ");
            isValid = false;
        }

        // 3. Rule: Availability Hours > 0
        if (entity.getAvailabilityHours() == null || entity.getAvailabilityHours() <= 0) {
            errors.append("Availability hours must be greater than 0. ");
            isValid = false;
        }

        // 4. Rule: Project ID must exist in project table
        boolean projectExists = projectRepository.existsById(entity.getProjectId());
        if (!projectExists) {
            errors.append("Project ID ").append(entity.getProjectId()).append(" does not exist. ");
            isValid = false;
        }

        // --- Update Database ---
        entity.setValidationError(isValid ? null : errors.toString());
        repository.save(entity);

        // --- Return to Camunda Gateway ---
        return Map.of("isValid", isValid);
    }
}