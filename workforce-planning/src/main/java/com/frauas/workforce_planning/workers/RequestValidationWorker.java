package com.frauas.workforce_planning.workers;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.frauas.workforce_planning.model.entity.StaffingRequest;
import com.frauas.workforce_planning.repository.ProjectRepository;
import com.frauas.workforce_planning.repository.StaffingRequestRepository;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.Variable;

@Component
public class RequestValidationWorker {

    private static final Logger logger = LoggerFactory.getLogger(RequestValidationWorker.class);

    @Autowired
    private StaffingRequestRepository requestRepository;

    @Autowired
    private ProjectRepository projectRepository;

    /**
     * Handles the "Validate Request Data" task in BPMN.
     * Ensures database consistency and business rule validation.
     */
    @JobWorker(type = "validate-request-data")
    @Transactional 
    public Map<String, Object> handleValidation(final ActivatedJob job, @Variable Long requestId) {
        logger.info("Starting Business Validation for Request ID: {}", requestId);

        // 1. Fetch the entity using the repository method mapped to 'request_id'
        // Using findById (JPA default) or findByRequestId based on your repo definition
        StaffingRequest entity = requestRepository.findById(requestId).orElse(null);

        boolean isValid = true;
        StringBuilder errorLog = new StringBuilder();

        if (entity == null) {
            logger.error("Request entity {} not found in database.", requestId);
            return Map.of("isValid", false, "businessErrorCode", "NOT_FOUND");
        }

        // --- BUSINESS LOGIC SECTION ---
        
        // Rule A: Check if Project exists and is linked
        if (entity.getProject() == null || !projectRepository.existsById(entity.getProject().getId())) { 
            isValid = false;
            errorLog.append("Project reference is missing or invalid. ");
        }

        // Rule B: Business constraint - Max 40 hours per week
        if (entity.getAvailabilityHoursPerWeek() != null && entity.getAvailabilityHoursPerWeek() > 40) {
            isValid = false;
            errorLog.append("Request exceeds maximum weekly capacity (40h). ");
        }

        // Rule C: Ensure a Job Role is actually assigned
        if (entity.getJobRole() == null) {
            isValid = false;
            errorLog.append("Job Role is mandatory for validation. ");
        }

        // 2. Update the Database based on validation result
        if (isValid) {
            entity.setStatus(com.frauas.workforce_planning.model.enums.RequestStatus.PENDING_APPROVAL);
            entity.setValidationError(null);
            // Sync the process instance key from the current job
            entity.setProcessInstanceKey(job.getProcessInstanceKey()); 
            logger.info("Validation successful for Request {}.", requestId);
        } else {
            // Revert to DRAFT and record the error
            entity.setStatus(com.frauas.workforce_planning.model.enums.RequestStatus.DRAFT);
            entity.setValidationError(errorLog.toString().trim());
            logger.warn("Validation failed for Request {}: {}", requestId, errorLog);
        }

        // Use saveAndFlush to ensure the DB update is committed before Camunda moves to the next task
        requestRepository.saveAndFlush(entity);

        // 3. Return variables to Camunda
        Map<String, Object> variables = new HashMap<>();
        variables.put("isValid", isValid);
        variables.put("businessErrorCode", isValid ? "NONE" : "VALIDATION_FAILED");
        
        return variables;
    }
}