package com.frauas.workforce_planning.dto;

import java.math.BigDecimal;
import java.util.List;

public record MatchedEmployeeDTO(
        Long requestId,
        Long employeeDbId,
        String employeeId,
        String firstName,
        String lastName,
        String email,
        String location,
        Integer availableHoursPerWeek,  
        BigDecimal wagePerHour,
        Integer experienceYears,
        double score,
        boolean applied,
        String seniorityLevel,
        String performanceGrade,
        String emergencyContact,           
        List<String> skills,               
       List<EmployeeLanguageDTO> languages
         
) {}
