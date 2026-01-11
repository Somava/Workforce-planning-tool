package com.frauas.workforce_planning.dto;

import java.math.BigDecimal;

public record MatchedEmployeeDTO(
        Long employeeDbId,
        String employeeId,
        String firstName,
        String lastName,
        String email,
        String location,
        Integer remainingHoursPerWeek,
        BigDecimal wagePerHour,
        Integer experienceYears,
        double score,
        boolean applied
) {}
