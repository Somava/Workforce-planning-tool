package com.frauas.workforce_planning.services;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.frauas.workforce_planning.dto.WorkforceRequestDTO; // Import Zeebe Client
import com.frauas.workforce_planning.model.entity.RequestEntity;
import com.frauas.workforce_planning.model.enums.RequestStatus;
import com.frauas.workforce_planning.repository.RequestRepository;

import io.camunda.zeebe.client.ZeebeClient;

@Service
public class StaffingRequestService {

    @Autowired
    private RequestRepository repository;
    @Autowired
    private ZeebeClient zeebeClient; // This is the "Brain" connector

    public RequestEntity createAndStartRequest(WorkforceRequestDTO dto) {
        // 1. Map DTO to Entity
        RequestEntity entity = new RequestEntity();
        entity.setPositionName(dto.title());
        entity.setProjectId(dto.projectId());
        entity.setAvailabilityHours(dto.availabilityHours()); // Maps to availability hours
        entity.setStartDate(dto.startDate());
        entity.setEndDate(dto.endDate());
        entity.setDescription(dto.description());
        entity.setRequiredSkills(dto.requiredSkills());
        entity.setProjectContext(dto.projectContext());
        entity.setPerformanceLoc(dto.performanceLocation());

        // 2. Auto-Generate System Fields
        Long generatedId = (long) (LocalDate.now().getYear() * 10000 + (Math.random() * 10000));
        entity.setRequestId(generatedId);
        entity.setCreatedById(3); // Example Manager ID

        if (entity.getPositionName() == null || entity.getPositionName().isEmpty()) {
            entity.setValidationError("Title is missing");
        } else if (entity.getStartDate() == null) {
            entity.setValidationError("Start date is required");
        } else if (entity.getAvailabilityHours() == null || entity.getAvailabilityHours() <= 0){
             entity.setValidationError("Availability hours must be greater than 0. ");
        } else if (entity.getStartDate().isAfter(entity.getEndDate())){
             entity.setValidationError("Start date must be before the end date. ");
        } else if (entity.getStartDate().isBefore(LocalDate.now())){
             entity.setValidationError("Start date cannot be in the past. ");
        }

        if (entity.getValidationError() == null || entity.getValidationError().isEmpty()) {
            entity.setStatus(RequestStatus.SUBMITTED);
        } else {
            entity.setStatus(RequestStatus.DRAFT);
        }

        // 3. Save to PostgreSQL
        RequestEntity saved = repository.save(entity);

        Map<String, Object> variables = new HashMap<>();
        variables.put("requestId", saved.getRequestId()); // Use getRequestId() as we discussed
        variables.put("projectId", saved.getProjectId());
        variables.put("managerId", "manager123");
        variables.put("status", saved.getStatus().toString());
        variables.put("isValid", true); // Initial trigger for the gateway
        
        // 4. START CAMUNDA 
        // Changed condition to include SUBMITTED so the process actually starts
        if (saved.getStatus() == RequestStatus.PENDING_APPROVAL || saved.getStatus() == RequestStatus.SUBMITTED) {
            try {
                var response = zeebeClient.newCreateInstanceCommand()
                    .bpmnProcessId("Process_ResourceAllocation") 
                    .latestVersion()
                    .variables(variables)
                    .send()
                    .join(); 

                // Update the DB with the real ID from Camunda
                saved.setProcessInstanceKey(response.getProcessInstanceKey());
                
                // Use 'repository' instead of 'staffingRequestRepository' to avoid type mismatch
                repository.save(saved); 
                
                System.out.println(">>> Instance Started: " + response.getProcessInstanceKey());
            } catch (Exception e) {
                System.err.println(">>> Camunda Start Failed: " + e.getMessage());
                e.printStackTrace(); // This is the 'e.printStackTrace()' from your screenshot
            }
        } else {
            System.out.println(">>> Camunda NOT started because status was: " + saved.getStatus());
        }

        return saved;
    }

    public RequestEntity updateExistingRequest(Long id, WorkforceRequestDTO dto) {
    RequestEntity existing = repository.findById(id)
        .orElseThrow(() -> new RuntimeException("Request not found"));

    // Update with new data from Manager
    existing.setPositionName(dto.title());
    existing.setAvailabilityHours(dto.availabilityHours());
    existing.setRequiredSkills(dto.requiredSkills());
    existing.setStartDate(dto.startDate());
    existing.setEndDate(dto.endDate());
    existing.setDescription(dto.description());
    
    // Clear the error message since it's being "fixed"
    existing.setValidationError(null);

    return repository.save(existing);
}
}