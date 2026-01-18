package com.frauas.workforce_planning.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record EmployeeProfileDTO(
    String employeeId,
    String firstName,
    String lastName,
    String email,
    String primaryLocation,
    String contractType,
    Integer experienceYears,
    BigDecimal wagePerHour,
    Integer totalHoursPerWeek,
    Integer remainingHoursPerWeek,
    Double performanceRating,
    String emergencyContact,
    LocalDate availabilityStart,
    LocalDate availabilityEnd,
    String matchingAvailability,
    String projectPreferences,
    String interests,
    List<String> skills,
    java.util.List<String> languages,
    String departmentName,
    String jobRoleName,
    String supervisorName,
    String supervisorEmail,
    String departmentHeadName,
    String departmentHeadEmail
) {}