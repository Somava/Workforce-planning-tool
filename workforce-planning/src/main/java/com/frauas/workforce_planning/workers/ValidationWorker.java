// package com.frauas.workforce_planning.workers;

// import java.time.LocalDate;
// import java.util.Map;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Component;
// import com.frauas.workforce_planning.model.entity.RequestEntity;
// import com.frauas.workforce_planning.model.enums.RequestStatus; // Added Enum import
// import com.frauas.workforce_planning.repository.ProjectRepository;
// import com.frauas.workforce_planning.repository.RequestRepository;
// import io.camunda.zeebe.client.api.response.ActivatedJob;
// import io.camunda.zeebe.spring.client.annotation.JobWorker;

// @Component
// public class ValidationWorker {

//     @Autowired
//     private RequestRepository repository;
    
//     @Autowired 
//     private ProjectRepository projectRepository; 

//     @JobWorker(type = "validate-request")
//     public Map<String, Object> validateRequest(final ActivatedJob job) {
//         Long id = ((Number) job.getVariable("id")).longValue();
//         RequestEntity entity = repository.findById(id).orElseThrow();

//         StringBuilder errors = new StringBuilder();
//         boolean isValid = true;

//         // 1. Rule: Start Date Check
//         if (entity.getStartDate() == null || entity.getStartDate().isBefore(LocalDate.now())) {
//             errors.append("Start date cannot be in the past. ");
//             isValid = false;
//         }

//         // 2. Rule: Availability Check (Entity uses getAvailabilityHours for headcount)
//         if (entity.getAvailabilityHours() == null || entity.getAvailabilityHours() <= 0) {
//             errors.append("Availability must be greater than 0. ");
//             isValid = false;
//         }

//         // 3. Rule: Project existence
//         if (entity.getProjectId() == null || !projectRepository.existsById(entity.getProjectId())) {
//             errors.append("Invalid Project ID. ");
//             isValid = false;
//         }

//         // --- Update Database ---
//         entity.setValidationError(isValid ? null : errors.toString());
        
//         if (!isValid) {
//             // FIXED: Uses RequestStatus Enum instead of String
//             entity.setStatus(RequestStatus.REJECTED);
//         }

//         repository.save(entity);

//         return Map.of("isValid", isValid);
//     }
// }