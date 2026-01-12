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

    @JobWorker(type = "validate-request-data")
    @Transactional 
    public Map<String, Object> handleValidation(final ActivatedJob job, @Variable Long requestId) {
        System.out.println("!!! WORKER TRIGGERED FOR REQUEST: " + requestId);
        logger.info("Starting Business Validation for Request ID: {}", requestId);

        // 1. Fetch the entity
        StaffingRequest entity = requestRepository.findByRequestId(requestId).orElse(null);

        if (entity == null) {
            logger.error("CRITICAL: Record {} not exist.", requestId);
            return Map.of("isValid", false, "businessErrorCode", "DATABASE_MISSING_RECORD");
        }

        boolean isValid = true;
        StringBuilder errorLog = new StringBuilder();
        
        // --- BUSINESS LOGIC RULES ---
        
        // Rule A: Project Validation
        if (entity.getProject() == null || !projectRepository.existsById(entity.getProject().getId())) { 
            isValid = false;
            errorLog.append("[Project Invalid] ");
        }

        // Rule B: Capacity Validation (Max 40h)
        if (entity.getAvailabilityHoursPerWeek() != null && entity.getAvailabilityHoursPerWeek() > 40) {
            isValid = false;
            errorLog.append("[Hours > 40h] ");
        }

        // Rule C: Job Role Validation
        if (entity.getJobRole() == null) {
            isValid = false;
            errorLog.append("[Missing Job Role] ");
        }

        // --- DATABASE UPDATE ---
        
        if (isValid) {
            entity.setStatus(com.frauas.workforce_planning.model.enums.RequestStatus.PENDING_APPROVAL);
            entity.setValidationError(null); // Clear previous errors if fixed
            entity.setProcessInstanceKey(job.getProcessInstanceKey()); 
            logger.info("Validation successful for Request {}.", requestId);
        } else {
            // Set status back to DRAFT so the manager can fix it
            entity.setStatus(com.frauas.workforce_planning.model.enums.RequestStatus.DRAFT);
            // PERSIST THE REASON: This maps to your 'validation_error' column
            entity.setValidationError(errorLog.toString().trim()); 
            logger.warn("Validation failed for Request {}: {}", requestId, errorLog);
        }

        // Save immediately so the UI/Operate can see the error
        requestRepository.saveAndFlush(entity);

        // --- RETURN TO CAMUNDA ---
        Map<String, Object> variables = new HashMap<>();
        variables.put("isValid", isValid); // Used by Gateway
        variables.put("validationReason", errorLog.toString().trim()); // Useful for debugging in Operate
        
        return variables;
    }
}