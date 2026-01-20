package com.frauas.workforce_planning.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Data Transfer Object for updating a Staffing Request during the Manager Revision phase.
 * Fields match the requirements for the 'Re-Submit' loop in the BPMN diagram.
 */
public record StaffingRequestUpdateDTO(
    String title,
    String description,
    List<String> requiredSkills,
    Integer experienceYears,
    BigDecimal wagePerHour,
    String workLocation,
    Integer availabilityHoursPerWeek
) {}