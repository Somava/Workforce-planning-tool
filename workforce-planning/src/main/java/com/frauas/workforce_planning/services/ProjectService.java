package com.frauas.workforce_planning.services;


import com.frauas.workforce_planning.dto.ProjectCreateDTO;
import com.frauas.workforce_planning.model.entity.*;
import com.frauas.workforce_planning.repository.*;
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

   @Autowired
   private ProjectDepartmentRepository projectDepartmentRepository;
    
    @Autowired
    private com.frauas.workforce_planning.repository.DepartmentRepository departmentRepository;

    @Autowired
    private com.frauas.workforce_planning.repository.UserRepository userRepository;

    @Transactional
public Project createProject(ProjectCreateDTO dto, String managerEmail) {
    // 1. Find the manager (Alice)
    Employee manager = employeeRepository.findByEmail(managerEmail)
            .orElseThrow(() -> new RuntimeException("Manager not found: " + managerEmail));

    if (manager.getUser() == null) {
        throw new RuntimeException("Employee has no linked User account.");
    }

    // 2. Create and Save the Project - No Department logic here anymore!
    Project project = new Project();
    project.setName(dto.name());
    project.setDescription(dto.description());
    project.setStartDate(dto.startDate());
    project.setEndDate(dto.endDate());
    project.setLocation(dto.location());
    project.setStatus("ACTIVE");
    project.setManagerUser(manager.getUser());

    return projectRepository.save(project);
}

    public List<Project> getAllActiveProjects() {
        return projectRepository.findAll().stream()
                .filter(p -> "ACTIVE".equalsIgnoreCase(p.getStatus()))
                .toList();
    }
}