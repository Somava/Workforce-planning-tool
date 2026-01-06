package com.frauas.workforce_planning.dto;

import java.time.LocalDate;
import java.util.List;
import java.math.BigDecimal;

public record WorkforceRequestDTO(
    String title,
    String description,
    Integer experienceYears,       
    Long projectId,
    Integer availabilityHours,
    LocalDate startDate,
    LocalDate endDate,
    Long departmentId,             
    BigDecimal wagePerHour,        
    List<String> requiredSkills,
    String projectContext,
    String projectLocation,        
    String workLocation
) {}