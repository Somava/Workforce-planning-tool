package com.frauas.workforce_planning.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.frauas.workforce_planning.dto.WorkforceRequestDTO;
import com.frauas.workforce_planning.model.entity.Department;
import com.frauas.workforce_planning.model.entity.Employee;
import com.frauas.workforce_planning.model.entity.Project;
import com.frauas.workforce_planning.model.entity.StaffingRequest;
import com.frauas.workforce_planning.model.enums.RequestStatus;
import com.frauas.workforce_planning.repository.DepartmentRepository;
import com.frauas.workforce_planning.repository.EmployeeRepository;
import com.frauas.workforce_planning.repository.ProjectRepository;
import com.frauas.workforce_planning.repository.StaffingRequestRepository;
import com.frauas.workforce_planning.repository.EmployeeApplicationRepository;
import java.util.stream.Collectors;

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
        request.setStatus(RequestStatus.REJECTED);
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
        
        log.info("Request {} rejected. Reason saved to DB and signaled to Camunda.", requestId);
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

}