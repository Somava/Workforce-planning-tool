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

    // 2. Create and Save the Project
    Project project = new Project();
    project.setName(dto.name());
    project.setDescription(dto.description());
    project.setStartDate(dto.startDate());
    project.setEndDate(dto.endDate());
    project.setLocation(dto.location());
    project.setStatus("ACTIVE");
    project.setManagerUser(manager.getUser());

    Project savedProject = projectRepository.save(project);

    // 3. Mapping Logic for Department Head AND Resource Planner
    if (dto.departmentId() != null) {
        Department dept = departmentRepository.findById(dto.departmentId())
                .orElseThrow(() -> new RuntimeException("Department not found"));

        // DYNAMIC LOOKUP: Find Head and Planner using your new Repository methods
        User deptHead = userRepository.findDepartmentHeadByDeptId(dto.departmentId())
                .orElseThrow(() -> new RuntimeException("Department Head not found for this department"));

        User resourcePlanner = userRepository.findResourcePlannerByDeptId(dto.departmentId())
                .orElseThrow(() -> new RuntimeException("Resource Planner not found for this department"));

        // Create the mapping record
        ProjectDepartment mapping = new ProjectDepartment();
        mapping.setProject(savedProject);
        mapping.setDepartment(dept);
        
        // Now BOTH can see the project/requests
        mapping.setDepartmentHeadUser(deptHead);
        mapping.setResourcePlannerUser(resourcePlanner); 
        
        projectDepartmentRepository.save(mapping);
    }
    
    return savedProject;
}

    public List<Project> getAllActiveProjects() {
        return projectRepository.findAll().stream()
                .filter(p -> "ACTIVE".equalsIgnoreCase(p.getStatus()))
                .toList();
    }
}