package com.frauas.workforce_planning.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.frauas.workforce_planning.dto.WorkforceRequestDTO;
import com.frauas.workforce_planning.model.entity.*;
import com.frauas.workforce_planning.model.enums.RequestStatus;
import com.frauas.workforce_planning.repository.*;

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
     * Creates a new request and starts the Camunda process.
     */
    @Transactional
    public StaffingRequest createAndStartRequest(WorkforceRequestDTO dto, Long currentManagerId) {
        StaffingRequest entity = new StaffingRequest();

        // 1. Map basic fields (Record syntax: dto.field()))
        mapDtoToEntity(dto, entity);

        // 2. Resolve Project
        Project project = projectRepository.findById(dto.projectId())
                .orElseThrow(() -> new RuntimeException("Project not found: " + dto.projectId()));
        entity.setProject(project);
        entity.setProjectName(project.getName()); 

        // 3. Resolve Department
        Department dept = departmentRepository.findById(dto.departmentId())
                .orElseThrow(() -> new RuntimeException("Department not found: " + dto.departmentId()));
        entity.setDepartment(dept);

        // 4. Resolve Job Role
        if (dto.jobRoleId() != null) {
            JobRole jobRole = jobRoleRepository.findById(dto.jobRoleId())
                    .orElseThrow(() -> new RuntimeException("JobRole not found"));
            entity.setJobRole(jobRole);
        }

        // 5. Resolve Manager (Creator)
        Employee manager = employeeRepository.findById(currentManagerId)
                .orElseThrow(() -> new RuntimeException("Manager not found"));
        entity.setCreatedBy(manager); 

        entity.setStatus(RequestStatus.SUBMITTED);

          // Save and Flush to DB first so the Camunda Worker can find it immediately
        
        StaffingRequest saved = repository.saveAndFlush(entity);
        return triggerCamundaProcess(saved);
    }

    /**

     * Updates an existing request.

     */

    @Transactional
    public StaffingRequest updateExistingRequest(Long requestId, WorkforceRequestDTO dto) {
        StaffingRequest existing = repository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Staffing Request not found ID: " + requestId));

        mapDtoToEntity(dto, existing);

        if (dto.jobRoleId() != null) {
            JobRole jobRole = jobRoleRepository.findById(dto.jobRoleId())
                    .orElseThrow(() -> new RuntimeException("JobRole not found"));
            existing.setJobRole(jobRole);
        }
        
        return repository.save(existing);
    }

    @Transactional
    public void rejectRequestByDepartmentHead(Long requestId) {
        StaffingRequest request = repository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found: " + requestId));

        request.setStatus(RequestStatus.REJECTED);
        repository.save(request);

        if (request.getProcessInstanceKey() != null) {
            zeebeClient.newCompleteCommand(request.getProcessInstanceKey()) 
                    .variable("requestApproved", false)
                    .variable("requestId", requestId)
                    .send()
                    .join();
        }
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
        // 'performanceLocation' is removed as it's not in your 25 columns list
    }

    public List<StaffingRequest> getAllRequests() {
        return repository.findAll();
    }

     /**

     * Triggers Zeebe and updates the process instance key.

     */
    private StaffingRequest triggerCamundaProcess(StaffingRequest saved) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("requestId", saved.getRequestId()); 
        variables.put("projectId", saved.getProject().getId());
        variables.put("managerName", saved.getCreatedBy().getFirstName() + " " + saved.getCreatedBy().getLastName());
        variables.put("status", saved.getStatus().toString());

        try {
            var event = zeebeClient.newCreateInstanceCommand()
                    .bpmnProcessId("Process_ResourceAllocation")
                    .latestVersion()
                    .variables(variables)
                    .send()
                    .join(); 

            saved.setProcessInstanceKey(event.getProcessInstanceKey());
            return repository.saveAndFlush(saved);
        } catch (Exception e) {
            log.error("Zeebe failed for request {}. Error: {}", saved.getRequestId(), e.getMessage());
            return saved; 
        }
    }

    public List<WorkforceRequestDTO> getPublishedRequestsForEmployees() {
        List<StaffingRequest> entities = repository.findByProject_PublishedTrue();
        return entities.stream()
                .map(this::convertToDTO)
                .toList();
    }

    private WorkforceRequestDTO convertToDTO(StaffingRequest entity) {
        return new WorkforceRequestDTO(
            entity.getTitle(),
            entity.getDescription(),
            entity.getProject() != null ? entity.getProject().getId() : null,
            entity.getDepartment() != null ? entity.getDepartment().getId() : null,
            entity.getJobRole() != null ? entity.getJobRole().getId() : null,
            entity.getExperienceYears(),
            entity.getAvailabilityHoursPerWeek(),
            entity.getProjectStartDate(),
            entity.getProjectEndDate(),
            entity.getWagePerHour(),
            entity.getProjectContext(),
            entity.getProjectLocation(),
            entity.getWorkLocation(),
            entity.getRequiredSkills(), // NO PARSING NEEDED: already a List<String>
            entity.getCreatedBy() != null ? entity.getCreatedBy().getId() : null
        );
    }
}