package com.frauas.workforce_planning.services;

import com.frauas.workforce_planning.dto.EmployeeProfileDTO; 
import com.frauas.workforce_planning.model.entity.Employee;
import com.frauas.workforce_planning.repository.EmployeeRepository;
import com.frauas.workforce_planning.repository.ProjectDepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import com.frauas.workforce_planning.dto.LeadershipEmployeeDTO;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ProjectDepartmentRepository projectDepartmentRepository;

  @Transactional(readOnly = true)
public EmployeeProfileDTO getProfile(String email) {
    Employee emp = employeeRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Employee profile not found: " + email));

    // 1. Format Languages: Converts entities to "LanguageName (Level)"
    java.util.List<String> languageList = emp.getLanguages().stream()
            .map(el -> el.getLanguage().getName() + " (" + el.getProficiencyLevel() + ")")
            .toList();

    // 2. Supervisor Logic (Alice)
    String supervisorName = "None";
    String supervisorEmail = "N/A";
    if (emp.getSupervisor() != null) {
        supervisorName = emp.getSupervisor().getFirstName() + " " + emp.getSupervisor().getLastName();
        supervisorEmail = emp.getSupervisor().getEmail();
    }


    return new EmployeeProfileDTO(
        emp.getEmployeeId(),
        emp.getFirstName(),
        emp.getLastName(),
        emp.getEmail(),
        emp.getPrimaryLocation(),
        emp.getContractType() != null ? emp.getContractType().toString() : null,
        emp.getExperienceYears(),
        emp.getWagePerHour(),
        emp.getTotalHoursPerWeek(),
        emp.getRemainingHoursPerWeek(),
        emp.getPerformanceRating(),
        emp.getEmergencyContact(),
        emp.getAvailabilityStart(),
        emp.getAvailabilityEnd(),
        emp.getMatchingAvailability() != null ? emp.getMatchingAvailability().toString() : null,
        emp.getProjectPreferences(),
        emp.getInterests(),
        emp.getSkills(),
        languageList, // PASS THE LANGUAGES HERE
        emp.getDepartment() != null ? emp.getDepartment().getName() : "N/A",
        supervisorName,
        supervisorEmail

    );
}
// UPDATED Method for Alice/Heads/Planners based on Project-Department Mapping
@Transactional(readOnly = true)
public List<LeadershipEmployeeDTO> getEmployeePoolForLeadership(String requesterEmail) {
    // 1. Find the person making the request
    Employee requester = employeeRepository.findByEmail(requesterEmail)
            .orElseThrow(() -> new RuntimeException("Requester not found"));

    // 2. Identify Alice (Role ID 3) - She sees everything
    boolean isAlice = requester.getUser().getRoles().stream()
            .anyMatch(role -> role.getId().equals(3L));

    List<Employee> pool;

    if (isAlice) {
        pool = employeeRepository.findAll();
    } else {
        // 3. For Bob, Charlie, etc., find their Department IDs from the mapping table
        Long userId = requester.getUser().getId();
        List<Long> managedDeptIds = projectDepartmentRepository.findDepartmentIdsByUserId(userId);

        if (managedDeptIds.isEmpty()) {
            return List.of(); // Return empty if user is not a Head or Planner
        }

        // 4. Get only employees belonging to those specific department IDs
        pool = employeeRepository.findByDepartment_IdIn(managedDeptIds);
    }

    // 5. Shared Filter & Mapping: Only return Role ID 4 (Specialists)
 return pool.stream()
            .filter(emp -> emp.getDefaultRole() != null && emp.getDefaultRole().getId() == 4L)
            .map(emp -> new LeadershipEmployeeDTO(
                emp.getId(),                                              // 1. id
                emp.getEmployeeId(),                                      // 2. employeeId
                emp.getFirstName() + " " + emp.getLastName(),             // 3. fullName
                emp.getDefaultRole() != null ? emp.getDefaultRole().getName() : "N/A", // 4. jobRole
                emp.getEmail(),                                           // 5. email
                emp.getSkills(),                                          // 6. skills
                emp.getLanguages().stream()                               // 7. languages
                   .map(l -> l.getLanguage().getName() + " (" + l.getProficiencyLevel() + ")")
                   .collect(Collectors.toList()),
                emp.getExperienceYears(),                                 // 8. experienceYears
                emp.getPerformanceRating(),                               // 9. performanceRating
                emp.getMatchingAvailability() != null ? emp.getMatchingAvailability().name() : "N/A" // 10. availabilityStatus
            ))
            .collect(Collectors.toList());
}
}
