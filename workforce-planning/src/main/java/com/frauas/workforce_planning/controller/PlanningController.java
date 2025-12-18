package com.frauas.workforce_planning.controller;

import com.frauas.workforce_planning.model.WorkforcePlan;
import com.frauas.workforce_planning.repository.WorkforcePlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
// Ensure this matches exactly what you typed in the browser
@RequestMapping("/api") 
@CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*") 
public class PlanningController {

    @Autowired
    private WorkforcePlanRepository repository;

    @GetMapping("/plans")
    public List<WorkforcePlan> getAllPlans() {
        return repository.findAll();
    }
}