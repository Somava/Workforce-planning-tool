package com.frauas.workforce_planning.workers;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger; // Assuming you have this
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.frauas.workforce_planning.model.entity.RequestEntity;
import com.frauas.workforce_planning.repository.ProjectRepository;
import com.frauas.workforce_planning.repository.RequestRepository;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.Variable;

@Component
public class RequestValidationWorker {

    private static final Logger logger = LoggerFactory.getLogger(RequestValidationWorker.class);

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private ProjectRepository projectRepository;

    /**
     * Handles the "Validate Request Data" task in BPMN.
     * Logic: Checks if the project is active and if the request doesn't conflict 
     * with existing company business rules.
     */
    @JobWorker(type = "validate-request-data")
    public Map<String, Object> handleValidation(final ActivatedJob job, @Variable Long requestId) {
        logger.info("Starting Business Validation for Request ID: {}", requestId);

        // 1. Fetch the actual entity from Database
        RequestEntity entity = requestRepository.findByRequestId(requestId)
                .orElse(null);

        boolean isValid = true;
        String errorMessage = "";

        // --- BUSINESS LOGIC SECTION ---
        
        if (entity == null) {
            isValid = false;
            errorMessage = "Request entity not found in database.";
        } else {
            // Rule A: Check if Project exists and is ACTIVE
            boolean projectActive = projectRepository.existsById(entity.getProjectId()); 
            if (!projectActive) {
                isValid = false;
                errorMessage = "Project ID " + entity.getProjectId() + " is inactive or deleted.";
            }

            // Rule B: Business constraint - Max 168 hours (Standard Month) per request
            if (entity.getAvailabilityHours() > 40) {
                isValid = false;
                errorMessage = "Request exceeds maximum monthly capacity (168h).";
            }
        }

        // 2. Update the Database based on validation result
        if (entity != null) {
            if (isValid) {
                entity.setStatus(com.frauas.workforce_planning.model.enums.RequestStatus.PENDING_APPROVAL);
                entity.setValidationError(null);
                
                // ADD THIS LINE: Store the process instance key
                // This helps your backend/frontend link the DB record to the Camunda process
                entity.setProcessInstanceKey(job.getProcessInstanceKey()); 

                logger.info("Validation successful for Request {}. Instance Key: {}", requestId, job.getProcessInstanceKey());
            } else {
                entity.setStatus(com.frauas.workforce_planning.model.enums.RequestStatus.DRAFT);
                entity.setValidationError(errorMessage);
            }
            requestRepository.save(entity);
        }

        // 3. Return variables to Camunda for the Gateway (isValid is the key one)
        Map<String, Object> variables = new HashMap<>();
        variables.put("isValid", isValid);
        variables.put("businessErrorCode", isValid ? "NONE" : "VALIDATION_FAILED");
        
        return variables;
    }
}