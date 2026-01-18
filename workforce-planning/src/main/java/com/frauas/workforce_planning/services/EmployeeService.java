package com.frauas.workforce_planning.services;

import com.frauas.workforce_planning.dto.EmployeeProfileDTO; 
import com.frauas.workforce_planning.model.entity.Employee;
import com.frauas.workforce_planning.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    // 3. Department Head Logic (Bob/Charlie/Diana)
    String headName = "N/A";
    String headEmail = "N/A";
    if (emp.getDepartment() != null && emp.getDepartment().getDepartmentHead() != null) {
        var headUser = emp.getDepartment().getDepartmentHead();
        headEmail = headUser.getEmail();
        // Assuming User is linked back to an Employee for the name
        if (headUser.getEmployee() != null) {
            headName = headUser.getEmployee().getFirstName() + " " + headUser.getEmployee().getLastName();
        }
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
        emp.getJobRole() != null ? emp.getJobRole().getName() : "N/A",
        supervisorName,
        supervisorEmail,
        headName,
        headEmail
    );
}
}