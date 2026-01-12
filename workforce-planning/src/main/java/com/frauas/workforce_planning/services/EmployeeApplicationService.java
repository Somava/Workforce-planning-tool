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
    public void apply(Long requestId, Long employeeId) {
        // 1. UPDATED: Check for duplicate application, but IGNORE withdrawn ones
        // This allows an employee to re-apply if they withdrew previously
        boolean alreadyApplied = applicationRepository.findByEmployee_Id(employeeId).stream()
                .anyMatch(app -> app.getStaffingRequest().getRequestId().equals(requestId) 
                          && app.getStatus() != ApplicationStatus.WITHDRAWN);

        if (alreadyApplied) {
            throw new RuntimeException("You have an active application for this position!");
        }

        // 2. Fetch Logic (Stays the same - only APPROVED requests)
        StaffingRequest req = requestRepository.findByRequestIdAndStatus(requestId, RequestStatus.APPROVED)
                .orElseThrow(() -> new RuntimeException("Access Denied: Position not approved."));

        // 3. Fetch Employee
        Employee emp = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        // 4. Save Application
        EmployeeApplication app = new EmployeeApplication();
        app.setStaffingRequest(req);
        app.setEmployee(emp);
        app.setStatus(ApplicationStatus.APPLIED);
        app.setAppliedAt(OffsetDateTime.now());

        applicationRepository.save(app);
    }

    /**
     * Dashboard: Returns a list of DTOs for the specific employee.
     */
    @Transactional(readOnly = true)
    public List<EmployeeApplicationDTO> getApplicationsForEmployee(Long employeeId) {
        List<EmployeeApplication> entities = applicationRepository.findByEmployee_Id(employeeId);
        
        if (entities.isEmpty()) {
            log.info("No applications found for employee ID: {}", employeeId);
        }
        
        return entities.stream()
                .map(app -> new EmployeeApplicationDTO(
                        app.getId(),
                        app.getStaffingRequest().getTitle(),
                        app.getStatus().toString(),
                        app.getAppliedAt(),
                        // 🔹 Logic: Can only withdraw if the Planner hasn't processed it yet
                        app.getStatus() == ApplicationStatus.APPLIED
                ))
                .collect(Collectors.toList());
    }

    /**
     * Withdraw logic: Blocks withdrawal if status is already REJECTED or SELECTED.
     */
    @Transactional
    public void withdrawApplication(Long applicationId, Long employeeId) {
        EmployeeApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found with ID: " + applicationId));

        if (!app.getEmployee().getId().equals(employeeId)) {
            throw new RuntimeException("Access Denied: Not your application.");
        }

        if (app.getStatus() != ApplicationStatus.APPLIED) {
            throw new RuntimeException("Withdrawal failed: Already processed or rejected.");
        }

        // 🔹 CHANGE: Instead of delete, we update the status
        app.setStatus(ApplicationStatus.WITHDRAWN);
        applicationRepository.save(app);
        
        log.info("Application ID {} status changed to WITHDRAWN by Employee ID {}", applicationId, employeeId);
    }
}