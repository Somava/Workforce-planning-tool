package com.frauas.workforce_planning.controller;

import com.frauas.workforce_planning.dto.LeadershipEmployeeDTO;
import com.frauas.workforce_planning.services.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.frauas.workforce_planning.dto.SuccessDashboardDTO;
import com.frauas.workforce_planning.services.StaffingRequestService;
import org.springframework.web.bind.annotation.RequestParam;

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

    @Autowired
    private StaffingRequestService staffingRequestService;

    @GetMapping("/all-employees")
    public ResponseEntity<List<LeadershipEmployeeDTO>> getGlobalEmployeePool() {
        // Calls the service logic to filter for Role ID 4 and exclude department info
        List<LeadershipEmployeeDTO> employeePool = employeeService.getEmployeePoolForLeadership();
        return ResponseEntity.ok(employeePool);
    }

    /**
     * Endpoint representing the "Notify All Parties" outcome in the BPMN diagram.
     * Returns successful assignments where the user is the Manager, Dept Head, or Planner.
     */
    @GetMapping("/success-notifications")
    public ResponseEntity<List<SuccessDashboardDTO>> getSuccessNotifications(@RequestParam String email) {
        // Calls the logic that matches your BPMN 'Notify All Parties' step
        List<SuccessDashboardDTO> notifications = staffingRequestService.getSuccessDashboardNotifications(email);
        
        if (notifications.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        
        return ResponseEntity.ok(notifications);
    }
}