package com.frauas.workforce_planning.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.frauas.workforce_planning.dto.StaffingRequestUpdateDTO;
import com.frauas.workforce_planning.dto.SuccessDashboardDTO;
import com.frauas.workforce_planning.dto.WorkforceRequestDTO;
import com.frauas.workforce_planning.model.entity.Department;
import com.frauas.workforce_planning.model.entity.Employee;
import com.frauas.workforce_planning.model.entity.Project;
import com.frauas.workforce_planning.model.entity.StaffingRequest;
import com.frauas.workforce_planning.model.enums.MatchingAvailability;
import com.frauas.workforce_planning.model.enums.RequestStatus;
import com.frauas.workforce_planning.repository.DepartmentRepository;
import com.frauas.workforce_planning.repository.EmployeeApplicationRepository;
import com.frauas.workforce_planning.repository.EmployeeRepository;
import com.frauas.workforce_planning.repository.ProjectRepository;
import com.frauas.workforce_planning.repository.StaffingRequestRepository;

import io.camunda.zeebe.client.ZeebeClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class StaffingRequestService {

    @Autowired
    private StaffingRequestRepository repository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmployeeApplicationRepository applicationRepository;

    @Autowired
    private ZeebeClient zeebeClient;

    /**
     * Entry Point: No @Transactional here. 
     * This ensures the DB commit is finished before Camunda starts.
     */
    @Transactional
    public StaffingRequest createAndStartRequest(WorkforceRequestDTO dto, Long currentManagerId) {
        // 1. Create and map the entity
        StaffingRequest entity = new StaffingRequest();
        mapDtoToEntity(dto, entity);

        // 2. Load relationships (Fast Lookups)
        Project project = projectRepository.findById(dto.projectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));
        
        Department dept = departmentRepository.findById(dto.departmentId())
                .orElseThrow(() -> new RuntimeException("Department not found"));

        Employee manager = employeeRepository.findById(currentManagerId)
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        // 3. Set properties and FORCE status to PENDING_APPROVAL
        entity.setProject(project);
        entity.setProjectName(project.getName());
        entity.setDepartment(dept);
        entity.setCreatedBy(manager);
        entity.setStatus(RequestStatus.PENDING_APPROVAL);

        // 4. Save to DB first to get the ID
        StaffingRequest saved = repository.saveAndFlush(entity);

        // 5. Start Camunda Process
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("requestId", saved.getRequestId());
            variables.put("deptHeadUserId", dept.getDepartmentHeadUserId());
            variables.put("managerName", manager.getFirstName() + " " + manager.getLastName());
            variables.put("requesterEmail", manager.getEmail());

            var event = zeebeClient.newCreateInstanceCommand()
                    .bpmnProcessId("Process_ResourceAllocation")
                    .latestVersion()
                    .variables(variables)
                    .send()
                    .join();

            // Store the process key back in the entity
            saved.setProcessInstanceKey(event.getProcessInstanceKey());
            return repository.save(saved);

        } catch (Exception e) {
            log.error("Camunda failed to start for request {}. Error: {}", saved.getRequestId(), e.getMessage());
            // We return the saved entity anyway so the UI knows the DB record exists
            return saved;
        }
    }

    // This is now the ONLY method for employees to see open jobs
    public List<WorkforceRequestDTO> getOpenPositionsForEmployee(String email) {
        // 1. Get all APPROVED requests
        List<StaffingRequest> approvedRequests = repository.findByStatus(RequestStatus.APPROVED);

        // 2. Get IDs already applied for by this specific email
        List<Long> appliedIds = applicationRepository.findRequestIdsByEmployeeEmail(email);

        // 3. Filter: Only show what has NOT been applied for
        return approvedRequests.stream()
            .filter(req -> !appliedIds.contains(req.getRequestId()))
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    // public List<WorkforceRequestDTO> getApprovedRequestsForEmployees() {
    //     // Changed from findByStatusAndProject_PublishedTrue to findByStatus
    //     List<StaffingRequest> entities = repository.findByStatus(RequestStatus.APPROVED);
    //     return entities.stream().map(this::convertToDTO).toList();
    // }

    private WorkforceRequestDTO convertToDTO(StaffingRequest entity) {
        return new WorkforceRequestDTO(
            entity.getRequestId(),
            entity.getTitle(), entity.getDescription(),
            entity.getProject() != null ? entity.getProject().getId() : null,
            entity.getDepartment() != null ? entity.getDepartment().getId() : null,
            entity.getExperienceYears(), entity.getAvailabilityHoursPerWeek(),
            entity.getProjectStartDate(), entity.getProjectEndDate(),
            entity.getWagePerHour(), entity.getProjectContext(),
            entity.getProjectLocation(), entity.getWorkLocation(),
            entity.getRequiredSkills()
        );
    }
    @Transactional
    public void rejectRequestByDepartmentHead(Long requestId) {
        // This hardcoded string is what the Manager will see on their dashboard
        String autoReason = "Rejected by Department Head (No specific reason provided).";
        
        // Call the original method with the hardcoded reason
        this.rejectRequestByDepartmentHead(requestId, autoReason);
    }

   @Transactional
    public void rejectRequestByDepartmentHead(Long requestId, String reason) {
        // 1. Fetch the request
        StaffingRequest request = repository.findByRequestId(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found: " + requestId));
        
        // 2. Update Database (The "Pull" part for the Manager's Dashboard)
        request.setStatus(RequestStatus.REQUEST_REJECTED);
        request.setRejectionType("DEPT_HEAD_REJECTED_INITIALLY");
        request.setRejectionReason(reason); // This maps directly to your entity field
        repository.save(request);

        // 3. Prepare variables for Camunda
        Map<String, Object> variables = new HashMap<>();
        variables.put("deptHeadApproved", false);
        variables.put("dataValid", true);
        variables.put("rejectionReason", reason); // Pass it to the process engine

        // 4. Signal Camunda
        zeebeClient.newPublishMessageCommand()
                .messageName("DeptHeadDecision")
                .correlationKey(requestId.toString()) 
                .variables(variables)
                .send()
                .join();
        
        log.info("Request {} rejected. Reason saved to DB and signaled to Camunda.", requestId, reason);
    }

    @Transactional
    public void approveRequestByDepartmentHead(Long requestId) {
        StaffingRequest request = repository.findByRequestId(requestId).orElseThrow();
        request.setStatus(RequestStatus.APPROVED);
        repository.save(request);

        zeebeClient.newPublishMessageCommand()
            .messageName("DeptHeadDecision")
            .correlationKey(requestId.toString()) // Matches the Modeler key
            .variable("deptHeadApproved", true)
            .send()
            .join();
        
    }

    @Transactional
    public void markInternalEmployeeApproved(Long requestId) {
    
        StaffingRequest request = repository.findByRequestId(requestId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Request not found: " + requestId
        ));

        // Safety: must have someone reserved/assigned
        if (request.getAssignedUser() == null || request.getAssignedUser().getEmployee() == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "No internal employee is linked to this request (assignedUser missing): " + requestId
            );
        }

        Employee employee = request.getAssignedUser().getEmployee();

        // Employee accepted -> lock them in
        employee.setMatchingAvailability(MatchingAvailability.ASSIGNED);
        employeeRepository.save(employee);

        // Update request status
        request.setStatus(RequestStatus.INT_EMPLOYEE_APPROVED_BY_DH); // use your exact enum value
        repository.save(request);

        // Signal Camunda
        zeebeClient.newPublishMessageCommand()
            .messageName("InternalEmployeeDecision") // must match BPMN
            .correlationKey(requestId.toString())
            .variables(Map.of("intEmployeeApproved", true))
            .send()
            .join();
    }

    @Transactional
    public void markInternalEmployeeRejected(Long requestId,  String reason) {
        StaffingRequest request = repository.findByRequestId(requestId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Request not found: " + requestId
            ));

        // Safety: must have someone reserved/assigned
        if (request.getAssignedUser() == null || request.getAssignedUser().getEmployee() == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "No internal employee is linked to this request (assignedUser missing): " + requestId
            );
        }

        Employee employee = request.getAssignedUser().getEmployee();

        // Employee rejected -> free them
        employee.setMatchingAvailability(MatchingAvailability.AVAILABLE);
        employeeRepository.save(employee);

        // Update request status
        request.setStatus(RequestStatus.INT_EMPLOYEE_REJECTED_BY_DH);
        request.setRejectionType("DEPT_HEAD_ASSIGNMENT_REJECTED"); // ✅ Hardcoded Type
        request.setRejectionReason(reason);
        repository.save(request);

        Map<String, Object> variables = Map.of(
            "intEmployeeApproved", false,
            "rejectionReason", reason
        );

        zeebeClient.newPublishMessageCommand()
            .messageName("InternalEmployeeDecision") // must match BPMN
            .correlationKey(requestId.toString())
            .variables(Map.of("intEmployeeApproved", false))
            .send()
            .join();
        
        log.info("Dept Head rejected internal assignment for Request {}. Reason: {}", requestId, reason);
    }
    

    private void mapDtoToEntity(WorkforceRequestDTO dto, StaffingRequest entity) {
        entity.setTitle(dto.title());
        entity.setDescription(dto.description());
        entity.setExperienceYears(dto.experienceYears());
        entity.setAvailabilityHoursPerWeek(dto.availabilityHoursPerWeek());
        entity.setProjectStartDate(dto.projectStartDate());
        entity.setProjectEndDate(dto.projectEndDate());
        entity.setWagePerHour(dto.wagePerHour());
        entity.setRequiredSkills(dto.requiredSkills());
        entity.setProjectContext(dto.projectContext());
        entity.setProjectLocation(dto.projectLocation());
        entity.setWorkLocation(dto.workLocation());
    }

    public List<StaffingRequest> getAllRequests() {
        return repository.findAll();
    }
    
    public List<StaffingRequest> getRequestsByManagerEmail(String email) {
        // Calling the repository method that performs the triple-table join
        return repository.findByCreatedBy_User_Email(email);
    }   

    @Transactional
    public void resubmitRequestByProjectManager(Long requestId) {
    StaffingRequest request = repository.findByRequestId(requestId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Request not found: " + requestId
        ));

    // Update DB status (use your actual enum value)
    request.setStatus(RequestStatus.PM_RESUBMITTED);
    repository.save(request);

    // Signal Camunda (must match BPMN)
    zeebeClient.newPublishMessageCommand()
        .messageName("Review Request Failure") // <-- match BPMN exactly
        .correlationKey(requestId.toString())
        .variables(Map.of("externalDecision", "resubmit"))
        .send()
        .join();

    log.info("PM resubmitted request {} and signaled Camunda.", requestId);
}

    @Transactional
    public void cancelRequestByProjectManager(Long requestId) {
        StaffingRequest request = repository.findByRequestId(requestId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Request not found: " + requestId
            ));

        // Update DB status (use your actual enum value)
        request.setStatus(RequestStatus.PM_CANCELLED);
        repository.save(request);

        // Signal Camunda (must match BPMN)
        zeebeClient.newPublishMessageCommand()
            .messageName("Review Request Failure") // <-- same receive task, decision differs by variable
            .correlationKey(requestId.toString())
            .variables(Map.of("externalDecision", "cancel"))
            .send()
            .join();

        log.info("PM cancelled request {} and signaled Camunda.", requestId);
    }
    @Transactional
    public void confirmAssignmentByEmployee(Long requestId) {
        StaffingRequest request = repository.findByRequestId(requestId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Request not found: " + requestId
            ));   

        Employee employee = request.getAssignedUser().getEmployee();
        employee.setMatchingAvailability(MatchingAvailability.ASSIGNED);
        employeeRepository.save(employee);

        request.setStatus(RequestStatus.INT_EMPLOYEE_ASSIGNED);
        repository.save(request);

        zeebeClient.newPublishMessageCommand()
            .messageName("Employee_Confirmation") // MUST match BPMN exactly
            .correlationKey(requestId.toString())
            .variables(Map.of("confirm", true))
            .send()
            .join();
    }

    @Transactional
    public void rejectAssignmentByEmployee(Long requestId, String reason) {
        StaffingRequest request = repository.findByRequestId(requestId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Request not found: " + requestId
            ));

        if (request.getAssignedUser() == null || request.getAssignedUser().getEmployee() == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Assigned internal employee missing for request: " + requestId
            );
        }

        Employee employee = request.getAssignedUser().getEmployee();
        employee.setMatchingAvailability(MatchingAvailability.AVAILABLE);
        employeeRepository.save(employee);

        request.setStatus(RequestStatus.INT_EMPLOYEE_REJECTED_BY_EMP);
        request.setRejectionType("EMPLOYEE_DECLINED"); // Hardcoded Type
        request.setRejectionReason(reason); 
        repository.save(request);

        Map<String, Object> variables = Map.of(
            "confirm", false,
            "rejectionReason", reason
        );

        zeebeClient.newPublishMessageCommand()
            .messageName("Employee_Confirmation") // MUST match BPMN exactly
            .correlationKey(requestId.toString())
            .variables(Map.of("confirm", false))
            .send()
            .join();

        log.info("Employee rejection recorded for Request ID: {}. Reason: {}", requestId, reason);
    }

    /**
     * Fetches successful assignments for the Congratulations Dashboard.
     * Corrected to navigate User -> Employee for names.
     */
    @Transactional(readOnly = true)
    public List<SuccessDashboardDTO> getSuccessDashboardNotifications(String email) {
        List<StaffingRequest> successRequests = repository.findSuccessDashboardData(email);

        return successRequests.stream().map(req -> {
            var au = req.getAssignedUser();
            
            // 1. Define 'emp' properly inside the map block
            com.frauas.workforce_planning.model.entity.Employee emp = (au != null) ? au.getEmployee() : null;

            String empName = "External/Freelancer";
            String empIdStr = "N/A";
            boolean isSelfAssignment = false;

            if (au != null) {
                if (au.getEmail().equalsIgnoreCase(email)) {
                    isSelfAssignment = true;
                }
                if (emp != null) {
                    empName = emp.getFirstName() + " " + emp.getLastName();
                    empIdStr = emp.getEmployeeId();
                }
            }

            String managerName = (req.getCreatedBy() != null)
                    ? req.getCreatedBy().getFirstName() + " " + req.getCreatedBy().getLastName()
                    : "N/A";

            String displayMessage = isSelfAssignment 
                ? String.format("Congratulations! You have been officially assigned to the project '%s' as '%s'.", req.getProjectName(), req.getTitle())
                : String.format("Success! %s (ID: %s) has accepted the offer for '%s' in project '%s'.", empName, empIdStr, req.getTitle(), req.getProjectName());

            // 2. Return the DTO with careful attention to types
            return new SuccessDashboardDTO(
                req.getRequestId(),
                req.getProjectName(),
                req.getTitle(),
                req.getDescription(),
                empName,
                empIdStr,
                req.getProjectStartDate(),
                req.getProjectEndDate(),
                req.getProjectLocation(),
                managerName,
                req.getWagePerHour(),
                // --- Employee Specifics ---
                (emp != null && emp.getPrimaryLocation() != null) ? emp.getPrimaryLocation() : "N/A",
                (emp != null && emp.getContractType() != null) ? emp.getContractType().name() : "N/A",
                (emp != null && emp.getPerformanceRating() != null) ? emp.getPerformanceRating() : 0.0,
                // FIX: If emp or skills is null, return an empty List
                (emp != null && emp.getSkills() != null) ? emp.getSkills() : java.util.Collections.emptyList(),
                displayMessage 
            );
        }).collect(Collectors.toList());
    }

    @Transactional
    public void updateRequestDetails(Long requestId, StaffingRequestUpdateDTO dto) {
        // Use the 'repository' variable consistently as per your createAndStartRequest method
        StaffingRequest request = repository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Request not found: " + requestId));

        // Update fields only if they are present in the DTO
        if (dto.title() != null) request.setTitle(dto.title());
        if (dto.description() != null) request.setDescription(dto.description());
        if (dto.requiredSkills() != null) request.setRequiredSkills(dto.requiredSkills());
        if (dto.experienceYears() != null) request.setExperienceYears(dto.experienceYears());
        if (dto.wagePerHour() != null) request.setWagePerHour(dto.wagePerHour());
        if (dto.workLocation() != null) request.setWorkLocation(dto.workLocation());
        if (dto.availabilityHoursPerWeek() != null) request.setAvailabilityHoursPerWeek(dto.availabilityHoursPerWeek());

        /* * Logic Alignment: Set status to PENDING_APPROVAL. 
         * Since it's re-submitting, it goes back to the 'Validate' and 'Dept Head' steps.
         */
        request.setStatus(RequestStatus.PENDING_APPROVAL); 
        
        repository.save(request);
    }

    @Transactional
    public void updateStatus(Long requestId, String status) {
        repository.findById(requestId).ifPresent(r -> {
            // Note: If you want to use the String status directly, 
            // ensure your Entity supports String or map it to RequestStatus.valueOf(status)
            try {
                r.setStatus(RequestStatus.valueOf(status.toUpperCase()));
            } catch (IllegalArgumentException e) {
                log.error("Invalid status provided: {}", status);
            }
            repository.save(r);
        });
    }

    public Optional<StaffingRequest> getById(Long requestId) {
        return repository.findById(requestId);
    }

}