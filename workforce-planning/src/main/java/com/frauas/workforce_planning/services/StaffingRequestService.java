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
import com.frauas.workforce_planning.model.entity.JobRole;
import com.frauas.workforce_planning.model.entity.Project;
import com.frauas.workforce_planning.model.entity.StaffingRequest;
import com.frauas.workforce_planning.model.enums.RequestStatus;
import com.frauas.workforce_planning.repository.DepartmentRepository;
import com.frauas.workforce_planning.repository.EmployeeRepository;
import com.frauas.workforce_planning.repository.JobRoleRepository;
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
    private JobRoleRepository jobRoleRepository;

    @Autowired
    private ZeebeClient zeebeClient;

    /**
     * Entry Point: No @Transactional here. 
     * This ensures the DB commit is finished before Camunda starts.
     */
    public StaffingRequest createAndStartRequest(WorkforceRequestDTO dto, Long currentManagerId) {
        // 1. Save to DB and COMMIT the transaction immediately
        StaffingRequest saved = saveToDatabase(dto, currentManagerId);
        
        // 2. Trigger Camunda now that the record is visible in the DB
        return triggerCamundaProcess(saved);
    }

    @Transactional
    public StaffingRequest saveToDatabase(WorkforceRequestDTO dto, Long currentManagerId) {
        StaffingRequest entity = new StaffingRequest();
        mapDtoToEntity(dto, entity);

        Project project = projectRepository.findById(dto.projectId())
                .orElseThrow(() -> new RuntimeException("Project not found: " + dto.projectId()));
        entity.setProject(project);
        entity.setProjectName(project.getName()); 

        Department dept = departmentRepository.findById(dto.departmentId())
                .orElseThrow(() -> new RuntimeException("Department not found: " + dto.departmentId()));
        
        if (!dept.getProject().getId().equals(project.getId())) {
            throw new RuntimeException("Inconsistency: Dept does not belong to Project");
        }
        entity.setDepartment(dept);

        Employee manager = employeeRepository.findById(currentManagerId)
                .orElseThrow(() -> new RuntimeException("Manager not found"));
        entity.setCreatedBy(manager); 

        entity.setStatus(RequestStatus.SUBMITTED);

        // Force write to DB so the Worker can find it
        return repository.saveAndFlush(entity);
    }

    private StaffingRequest triggerCamundaProcess(StaffingRequest saved) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("requestId", saved.getRequestId()); 
        variables.put("projectId", saved.getProject().getId());
        variables.put("departmentId", saved.getDepartment().getId());
        variables.put("deptHeadUserId", saved.getDepartment().getDepartmentHeadUserId());
        variables.put("managerName", saved.getCreatedBy().getFirstName() + " " + saved.getCreatedBy().getLastName());
        variables.put("requesterEmail", saved.getCreatedBy().getEmail());

        try {
        var event = zeebeClient.newCreateInstanceCommand()
                .bpmnProcessId("Process_ResourceAllocation")
                .latestVersion()
                .variables(variables)
                .send()
                .join(); 

        long key = event.getProcessInstanceKey();

        // CRITICAL STEP: Fetch the LATEST version from the DB.
        // This version contains the "PENDING_APPROVAL" status from the worker.
        StaffingRequest latest = repository.findByRequestId(saved.getRequestId())
                .orElse(saved);

        // Update ONLY the key
        latest.setProcessInstanceKey(key);

        // Save this version. It now has BOTH the key AND the correct status.
        return repository.save(latest);
        
        } catch (Exception e) {
            log.error("Zeebe failed for request {}: {}", saved.getRequestId(), e.getMessage());
            return saved; 
        }
    }

    @Transactional
    public StaffingRequest updateExistingRequest(Long requestId, WorkforceRequestDTO dto) {
        StaffingRequest existing = repository.findByRequestId(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found ID: " + requestId));

        mapDtoToEntity(dto, existing);
        
        return repository.save(existing);
    }

    public List<WorkforceRequestDTO> getApprovedRequestsForEmployees() {
        // Changed from findByStatusAndProject_PublishedTrue to findByStatus
        List<StaffingRequest> entities = repository.findByStatus(RequestStatus.APPROVED);
        return entities.stream().map(this::convertToDTO).toList();
    }
    private WorkforceRequestDTO convertToDTO(StaffingRequest entity) {
        return new WorkforceRequestDTO(
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