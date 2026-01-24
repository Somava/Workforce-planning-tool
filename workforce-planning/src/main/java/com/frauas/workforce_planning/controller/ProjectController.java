package com.frauas.workforce_planning.controller;

import com.frauas.workforce_planning.dto.ProjectCreateDTO;
import com.frauas.workforce_planning.model.entity.Project;
import com.frauas.workforce_planning.services.ProjectService;
import com.frauas.workforce_planning.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "http://localhost:3000")
public class ProjectController {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectService projectService; // Inject the new service logic

    @GetMapping
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    /**
     * New Endpoint for Alice to create a project.
     */
    @PostMapping("/create")
    public ResponseEntity<Project> createProject(
            @RequestBody ProjectCreateDTO dto, 
            @RequestParam String managerEmail) {
        
        // Use the service to handle the "ACTIVE" status logic
        Project newProject = projectService.createProject(dto, managerEmail);
        
        return new ResponseEntity<>(newProject, HttpStatus.CREATED);
    }
}