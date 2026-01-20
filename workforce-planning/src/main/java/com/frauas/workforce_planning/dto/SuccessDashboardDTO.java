package com.frauas.workforce_planning.dto;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;


public record SuccessDashboardDTO(
    Long requestId,               // 1
    String projectName,           // 2
    String jobTitle,              // 3
    String jobDescription,        // 4
    String employeeName,          // 5
    String employeeId,            // 6
    LocalDate startDate,          // 7
    LocalDate endDate,            // 8
    String projectLocation,       // 9
    String managerName,           // 10
    BigDecimal wagePerHour,       // 11
    String primaryLocation,       // 12
    String contractType,          // 13
    Double performanceRating,     // 14
    List<String> employeeSkills,  // 15
    String congratsMessage        // 16
) {}