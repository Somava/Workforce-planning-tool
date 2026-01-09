package com.frauas.workforce_planning.services;

import com.frauas.workforce_planning.model.entity.Employee;
import com.frauas.workforce_planning.dto.EmployeeApplicationDTO;
import com.frauas.workforce_planning.model.entity.EmployeeApplication;
import com.frauas.workforce_planning.model.entity.StaffingRequest;
import com.frauas.workforce_planning.model.enums.ApplicationStatus;
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
    // 1. Check for duplicate application
    boolean alreadyApplied = applicationRepository.findByEmployee_Id(employeeId).stream()
            .anyMatch(app -> app.getStaffingRequest().getRequestId().equals(requestId));

    if (alreadyApplied) {
        throw new RuntimeException("You have already applied for this position!");
    }

    // This method only returns the request if it is APPROVED and its project is PUBLISHED
    StaffingRequest req = requestRepository.findByRequestIdAndStatusAndProject_PublishedTrue(requestId, com.frauas.workforce_planning.model.enums.RequestStatus.APPROVED)
            .orElseThrow(() -> new RuntimeException("Access Denied: You can only apply for Approved and Published positions."));

    // 3. Fetch the Employee (Applicant)
    Employee emp = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new RuntimeException("Employee not found"));

    // 4. Create and Save Application
    EmployeeApplication app = new EmployeeApplication();
    app.setStaffingRequest(req);
    app.setEmployee(emp);
    app.setStatus(ApplicationStatus.APPLIED);
    app.setAppliedAt(OffsetDateTime.now());

    applicationRepository.save(app);
}
/**
     * UPDATED: Now returns a list of DTOs instead of Entities.
     */
    @Transactional(readOnly = true)
    public List<EmployeeApplicationDTO> getApplicationsForEmployee(Long employeeId) {
        // 1. Get entities from DB
        List<EmployeeApplication> entities = applicationRepository.findByEmployee_Id(employeeId);
        if (entities.isEmpty()) {
            log.info("No applications found for employee ID: {}", employeeId);
        }
        
        // 2. Map Entities to DTOs
        return entities.stream()
                .map(app -> new EmployeeApplicationDTO(
                        app.getId(),                          // applicationId
                        app.getStaffingRequest().getTitle(),  // projectTitle
                        app.getStatus().toString(),           // status
                        app.getAppliedAt()                    // appliedAt
                ))
                .collect(Collectors.toList());
    }
    /**
     * Allows an employee to withdraw their application.
     * Checks if the application exists and belongs to the employee before deleting.
     */
    @Transactional
    public void withdrawApplication(Long applicationId, Long employeeId) {
        // 1. Find the application by ID
        EmployeeApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found with ID: " + applicationId));

        // 2. Security Check: Ensure the employee withdrawing is the one who applied
        // Note: Using getId() assuming your Employee entity primary key is 'id'
        if (!app.getEmployee().getId().equals(employeeId)) {
            throw new RuntimeException("Access Denied: You can only withdraw your own applications.");
        }

        // 3. Delete from the database
        applicationRepository.delete(app);
        
        log.info("Application ID {} was successfully withdrawn by Employee ID {}", applicationId, employeeId);
    }
}