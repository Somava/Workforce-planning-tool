package com.frauas.workforce_planning.controller;

import com.frauas.workforce_planning.dto.LeadershipEmployeeDTO;
import com.frauas.workforce_planning.services.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller for Alice (Manager), Dept Heads, and Resource Planners 
 * to view the global pool of Role_Employees.
 */
@RestController
@RequestMapping("/api/workforce-overview")
public class WorkforceOverviewController {

    @Autowired
    private EmployeeService employeeService;

    @GetMapping("/all-employees")
    public ResponseEntity<List<LeadershipEmployeeDTO>> getGlobalEmployeePool() {
        // Calls the service logic to filter for Role ID 4 and exclude department info
        List<LeadershipEmployeeDTO> employeePool = employeeService.getEmployeePoolForLeadership();
        return ResponseEntity.ok(employeePool);
    }
}