package com.frauas.workforce_planning.services;

import com.frauas.workforce_planning.dto.EmployeeProfileDTO; 
import com.frauas.workforce_planning.model.entity.Employee;
import com.frauas.workforce_planning.repository.EmployeeRepository;
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
// NEW Method for Alice/Heads/Planners (Directory View)
    @Transactional(readOnly = true)
    public List<LeadershipEmployeeDTO> getEmployeePoolForLeadership() {
        return employeeRepository.findAll().stream()
            // Filter: Only return those with Role ID 4 (ROLE_EMPLOYEE)
            .filter(emp -> emp.getDefaultRole() != null && emp.getDefaultRole().getId() == 4)
            .map(emp -> new LeadershipEmployeeDTO(
                emp.getId(),
                emp.getEmployeeId(),
                emp.getFirstName() + " " + emp.getLastName(),
                emp.getEmail(),
                emp.getSkills(),
                emp.getLanguages().stream()
                   .map(l -> l.getLanguage().getName() + " (" + l.getProficiencyLevel() + ")")
                   .collect(Collectors.toList()),
                emp.getExperienceYears(),
                emp.getPerformanceRating(),
                emp.getMatchingAvailability() != null ? emp.getMatchingAvailability().name() : "N/A"
            ))
            .collect(Collectors.toList());
    }
}
