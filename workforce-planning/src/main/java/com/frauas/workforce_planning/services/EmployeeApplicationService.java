package com.frauas.workforce_planning.services;

import com.frauas.workforce_planning.model.entity.Employee;
import com.frauas.workforce_planning.dto.EmployeeApplicationDTO;
import com.frauas.workforce_planning.model.entity.EmployeeApplication;
import com.frauas.workforce_planning.model.entity.StaffingRequest;
import com.frauas.workforce_planning.model.enums.ApplicationStatus;
import com.frauas.workforce_planning.model.enums.RequestStatus; // Added this import
import com.frauas.workforce_planning.repository.EmployeeApplicationRepository;
import com.frauas.workforce_planning.repository.EmployeeRepository;
import com.frauas.workforce_planning.repository.StaffingRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Optional;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.time.OffsetDateTime;

@Slf4j
@Service
public class EmployeeApplicationService {

    @Autowired 
    private EmployeeApplicationRepository applicationRepository;
    
    @Autowired 
    private StaffingRequestRepository requestRepository;
    
    @Autowired 
    private EmployeeRepository employeeRepository;

   @Transactional
public void apply(Long requestId, String email) {
    Employee emp = employeeRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Employee not found"));

    // 1. Look for ANY existing application (including withdrawn ones)
    Optional<EmployeeApplication> existingApp = applicationRepository
            .findByEmployee_Id(emp.getId()).stream()
            .filter(app -> app.getStaffingRequest().getRequestId().equals(requestId))
            .findFirst();

    if (existingApp.isPresent()) {
        EmployeeApplication app = existingApp.get();
        
        // 2. If it's already active, block it
        if (app.getStatus() != ApplicationStatus.WITHDRAWN) {
            throw new RuntimeException("You have an active application for this position!");
        }
        
        // 3. If it was withdrawn, RE-ACTIVATE it instead of creating a new one
        app.setStatus(ApplicationStatus.APPLIED);
        app.setAppliedAt(OffsetDateTime.now());
        applicationRepository.save(app);
        return; // Exit here
    }

    // 4. If no record exists at all, create a new one (your existing logic)
    StaffingRequest req = requestRepository.findByRequestIdAndStatus(requestId, RequestStatus.APPROVED)
            .orElseThrow(() -> new RuntimeException("Position not found or approved."));

    EmployeeApplication newApp = new EmployeeApplication();
    newApp.setStaffingRequest(req);
    newApp.setEmployee(emp);
    newApp.setStatus(ApplicationStatus.APPLIED);
    newApp.setAppliedAt(OffsetDateTime.now());
    applicationRepository.save(newApp);
}

    @Transactional(readOnly = true)
    public List<EmployeeApplicationDTO> getApplicationsForEmployee(String email) { // Changed parameter to String email
        // Resolve Employee by Email
        Employee emp = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employee not found: " + email));

        List<EmployeeApplication> entities = applicationRepository.findByEmployee_Id(emp.getId());
        
        if (entities.isEmpty()) {
            log.info("No applications found for employee: {}", email);
        }
        
        return entities.stream()
                .map(app -> new EmployeeApplicationDTO(
                        app.getId(),
                        app.getStaffingRequest().getTitle(),
                        app.getStatus().toString(),
                        app.getAppliedAt(),
                        app.getStatus() == ApplicationStatus.APPLIED
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public void withdrawApplication(Long applicationId, String email) { // Changed parameter to String email
        EmployeeApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found with ID: " + applicationId));

        // 0. Resolve Employee to check ownership
        Employee emp = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employee not found: " + email));

        if (!app.getEmployee().getId().equals(emp.getId())) {
            throw new RuntimeException("Access Denied: Not your application.");
        }

        if (app.getStatus() != ApplicationStatus.APPLIED) {
            throw new RuntimeException("Withdrawal failed: Already processed or rejected.");
        }

        app.setStatus(ApplicationStatus.WITHDRAWN);
        applicationRepository.save(app);
        
        log.info("Application ID {} changed to WITHDRAWN by {}", applicationId, email);
    }
}