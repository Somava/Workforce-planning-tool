package com.frauas.workforce_planning.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.frauas.workforce_planning.dto.ProjectCreateDTO;
import com.frauas.workforce_planning.model.entity.Project;
import com.frauas.workforce_planning.repository.ProjectRepository;
import com.frauas.workforce_planning.security.JwtAuthFilter;
import com.frauas.workforce_planning.services.ProjectService;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final ProjectRepository projectRepository;

    public ProjectController(ProjectService projectService,
                                     ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
        this.projectService = projectService;
    }

    @GetMapping
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    /**
     * New Endpoint for Alice to create a project.
     */
    @PostMapping("/create")
    public ResponseEntity<Project> createProject(
            @RequestBody ProjectCreateDTO dto
        ) {
        
        JwtAuthFilter.JwtPrincipal p = (JwtAuthFilter.JwtPrincipal) SecurityContextHolder
        .getContext()
        .getAuthentication()
        .getPrincipal();

        String role = p.selectedRole();
        if(!"ROLE_MANAGER".equals(role)) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You are not authorized to perform this action"
            );
        }
        String managerEmail = p.email();
        // Use the service to handle the "ACTIVE" status logic
        Project newProject = projectService.createProject(dto, managerEmail);
        
        return new ResponseEntity<>(newProject, HttpStatus.CREATED);
    }
}