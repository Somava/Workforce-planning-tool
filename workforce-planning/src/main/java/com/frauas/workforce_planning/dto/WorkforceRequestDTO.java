package com.frauas.workforce_planning.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Updated to match the JSON keys sent from Swagger/Frontend.
 * Field names must match the JSON keys exactly for Spring to map them.
 */
public record WorkforceRequestDTO(
    String title,
    String description,
    Long projectId,
    Long departmentId,          
    Integer experienceYears,
    
    // Changed from availabilityHours to match JSON "availabilityHoursPerWeek"
    Integer availabilityHoursPerWeek,
    
    // Changed from startDate to match JSON "projectStartDate"
    LocalDate projectStartDate,
    
    // Changed from endDate to match JSON "projectEndDate"
    LocalDate projectEndDate,
    
    BigDecimal wagePerHour,
    String projectContext,
    String projectLocation,
    String workLocation,
    List<String> requiredSkills
) {}