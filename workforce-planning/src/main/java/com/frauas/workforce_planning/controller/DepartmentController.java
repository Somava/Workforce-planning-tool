package com.frauas.workforce_planning.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.frauas.workforce_planning.model.entity.Department;
import com.frauas.workforce_planning.repository.DepartmentRepository;

@RestController
@RequestMapping("/api/departments")
@CrossOrigin(origins = "http://localhost:3000")
public class DepartmentController {

    @Autowired
    private DepartmentRepository departmentRepository;

    /**
     * Get all departments (now returns all 12+ depts)
     */
    @GetMapping
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    // /**
    //  * NEW: Get the 3 departments belonging to a specific project.
    //  * Usage: GET /api/departments/project/1
    //  */
    // @GetMapping("/project/{projectId}")
    // public List<Department> getDepartmentsByProject(@PathVariable Long projectId) {
    //     return departmentRepository.findByProjectId(projectId);
    // }

    // /**
    //  * NEW: Find department by its Head.
    //  * Useful for the "Department Head" dashboard.
    //  */
    // @GetMapping("/head/{userId}")
    // public ResponseEntity<Department> getDepartmentByHead(@PathVariable Long userId) {
    //     return departmentRepository.findByDepartmentHeadUserId(userId)
    //             .map(ResponseEntity::ok)
    //             .orElse(ResponseEntity.notFound().build());
    // }
}