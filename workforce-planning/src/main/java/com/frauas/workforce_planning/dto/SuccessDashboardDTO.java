package com.frauas.workforce_planning.dto;


public record SuccessDashboardDTO(
    Long requestId,
    String projectName,
    String jobTitle,
    String jobDescription,
    String employeeName,
    String employeeId,
    String congratsMessage
) {}