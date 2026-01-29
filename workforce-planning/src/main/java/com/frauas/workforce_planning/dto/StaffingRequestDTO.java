package com.frauas.workforce_planning.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class StaffingRequestDTO {
    // Basic Request Info
    private Long requestId;
    private String title;
    private String description;
    private String status;
    private BigDecimal wagePerHour;
    private Integer experienceYears;
    private Integer availabilityHoursPerWeek;
    private List<String> requiredSkills;
    private OffsetDateTime createdAt;

    // Project & Location Info (For the "i" button)
    private String projectName;
    private String projectContext;
    private String projectLocation;
    private String workLocation;
    private LocalDate projectStartDate;
    private LocalDate projectEndDate;

    // Assigned Person Info (The name fix)
    private String firstName;
    private String lastName;
    private Long assignedUserId;

    // Manager/Department Info (For the "i" button)
    private String departmentName;
    private String managerName;
    private String managerEmail;
    
    // Process/Error Info
    private Long processInstanceKey;
    private String rejectionReason;
}