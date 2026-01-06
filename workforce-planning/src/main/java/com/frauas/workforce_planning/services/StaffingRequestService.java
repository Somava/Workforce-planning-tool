package com.frauas.workforce_planning.services;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private EmployeeRepository employeeRepository; // Needed to fetch the Manager entity

    @Autowired
    private ZeebeClient zeebeClient;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Creates a new Staffing Request and links it to the proper Employee entity.
     */
    @Transactional
    public StaffingRequest createAndStartRequest(WorkforceRequestDTO dto, Long currentManagerId) {
        StaffingRequest entity = new StaffingRequest();

        // 1. Manager-filled fields from DTO
        entity.setTitle(dto.title());
        entity.setDescription(dto.description());
        entity.setExperienceYears(dto.experienceYears());
        entity.setAvailabilityHoursPerWeek(dto.availabilityHours());
        entity.setProjectStartDate(dto.startDate());
        entity.setProjectEndDate(dto.endDate());
        entity.setWagePerHour(dto.wagePerHour());
        entity.setProjectContext(dto.projectContext());
        entity.setProjectLocation(dto.projectLocation());
        entity.setWorkLocation(dto.workLocation());

        // 2. Convert List<String> skills to JSONB String
        entity.setRequiredSkills(convertSkillsToJson(dto.requiredSkills()));

        // 3. Automated Related Entity Lookups
        Project project = projectRepository.findById(dto.projectId())
                .orElseThrow(() -> new RuntimeException("Project not found with ID: " + dto.projectId()));
        entity.setProject(project);

        Department dept = departmentRepository.findById(dto.departmentId())
                .orElseThrow(() -> new RuntimeException("Department not found with ID: " + dto.departmentId()));
        entity.setDepartment(dept);

        // OPTION 1: Fetch the actual Employee object for the 'createdBy' relationship
        Employee manager = employeeRepository.findById(currentManagerId)
                .orElseThrow(() -> new RuntimeException("Employee (Manager) not found with ID: " + currentManagerId));
        entity.setCreatedBy(manager); 

        // 4. Set System Fields
        entity.setStatus(RequestStatus.SUBMITTED);
        entity.setCreatedAt(OffsetDateTime.now());
        
        // Note: assignedUser remains null at this stage per your requirement.

        // 5. Persist to Database
        StaffingRequest saved = repository.save(entity);

        // 6. Trigger Camunda
        return triggerCamundaProcess(saved);
    }

    @Transactional
    public StaffingRequest updateExistingRequest(Long requestId, WorkforceRequestDTO dto) {
        StaffingRequest existing = repository.findByRequestId(requestId)
                .orElseThrow(() -> new RuntimeException("Staffing Request not found"));

        existing.setTitle(dto.title());
        existing.setDescription(dto.description());
        existing.setExperienceYears(dto.experienceYears());
        existing.setAvailabilityHoursPerWeek(dto.availabilityHours());
        existing.setProjectStartDate(dto.startDate());
        existing.setProjectEndDate(dto.endDate());
        existing.setWagePerHour(dto.wagePerHour());
        existing.setRequiredSkills(convertSkillsToJson(dto.requiredSkills()));

        return repository.save(existing);
    }

    public List<StaffingRequest> getAllRequests() {
        return repository.findAll();
    }

    private String convertSkillsToJson(List<String> skills) {
        try {
            return objectMapper.writeValueAsString(skills);
        } catch (JsonProcessingException e) {
            log.error("JSON Conversion failed", e);
            return "[]";
        }
    }

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
            return repository.save(saved);
        } catch (Exception e) {
            log.error("Camunda failed to start", e);
            return saved;
        }
    }
}