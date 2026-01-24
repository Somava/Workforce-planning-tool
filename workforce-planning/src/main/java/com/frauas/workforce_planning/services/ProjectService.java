package com.frauas.workforce_planning.services;

import com.frauas.workforce_planning.dto.ProjectCreateDTO;
import com.frauas.workforce_planning.model.entity.Project;
import com.frauas.workforce_planning.model.entity.Employee;
import com.frauas.workforce_planning.repository.ProjectRepository;
import com.frauas.workforce_planning.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    /**
     * Creates a new project for a Manager and sets it to ACTIVE status immediately.
     * Maps the Employee's linked User to the Project's managerUser field.
     */
    @Transactional
    public Project createProject(ProjectCreateDTO dto, String managerEmail) {
        // 1. Find the manager employee by email
        Employee manager = employeeRepository.findByEmail(managerEmail)
                .orElseThrow(() -> new RuntimeException("Manager not found with email: " + managerEmail));

        // 2. Ensure the employee has a linked user account
        if (manager.getUser() == null) {
            throw new RuntimeException("This employee does not have a linked User account to be assigned as a manager.");
        }

        Project project = new Project();
        project.setName(dto.name());
        project.setDescription(dto.description());
        project.setStartDate(dto.startDate());
        project.setEndDate(dto.endDate());
        project.setLocation(dto.location());
        
        // Requirements: Always ACTIVE and Published
        project.setStatus("ACTIVE");
        project.setPublished(true);
        
        // 3. Link the project to the User entity (NOT just the ID)
        project.setManagerUser(manager.getUser());

        return projectRepository.save(project);
    }

    /**
     * Retrieves all projects with the status "ACTIVE".
     */
    public List<Project> getAllActiveProjects() {
        return projectRepository.findAll().stream()
                .filter(p -> "ACTIVE".equalsIgnoreCase(p.getStatus()))
                .toList();
    }
}