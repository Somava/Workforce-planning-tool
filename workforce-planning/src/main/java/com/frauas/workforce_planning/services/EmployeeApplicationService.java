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
    public void apply(Long requestId, String email) { // Changed parameter to String email
        // 0. Resolve Employee by Email
        Employee emp = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employee not found with email: " + email));

        // 1. Check for duplicate application, ignoring withdrawn ones
        boolean alreadyApplied = applicationRepository.findByEmployee_Id(emp.getId()).stream()
                .anyMatch(app -> app.getStaffingRequest().getRequestId().equals(requestId) 
                          && app.getStatus() != ApplicationStatus.WITHDRAWN);

        if (alreadyApplied) {
            throw new RuntimeException("You have an active application for this position!");
        }

        // 2. Fetch Logic (Only APPROVED requests)
        StaffingRequest req = requestRepository.findByRequestIdAndStatus(requestId, RequestStatus.APPROVED)
                .orElseThrow(() -> new RuntimeException("Access Denied: Position not approved."));

        // 4. Save Application
        EmployeeApplication app = new EmployeeApplication();
        app.setStaffingRequest(req);
        app.setEmployee(emp);
        app.setStatus(ApplicationStatus.APPLIED);
        app.setAppliedAt(OffsetDateTime.now());

        applicationRepository.save(app);
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