package com.frauas.workforce_planning.services;

import com.frauas.workforce_planning.model.entity.Employee;
import com.frauas.workforce_planning.model.entity.EmployeeApplication;
import com.frauas.workforce_planning.model.entity.StaffingRequest;
import com.frauas.workforce_planning.model.enums.ApplicationStatus;
import com.frauas.workforce_planning.repository.EmployeeApplicationRepository;
import com.frauas.workforce_planning.repository.EmployeeRepository;
import com.frauas.workforce_planning.repository.StaffingRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

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
        // Note: Make sure your repository has this method name or use your findByEmployee_Id logic
        boolean alreadyApplied = applicationRepository.findByEmployee_Id(employeeId).stream()
                .anyMatch(app -> app.getStaffingRequest().getRequestId().equals(requestId));

        if (alreadyApplied) {
            throw new RuntimeException("You have already applied for this position!");
        }

        // 2. Fetch the Staffing Request
        StaffingRequest req = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Staffing Request not found"));

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
}