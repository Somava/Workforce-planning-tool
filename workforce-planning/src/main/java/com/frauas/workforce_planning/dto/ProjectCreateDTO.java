package com.frauas.workforce_planning.dto;

import java.time.LocalDate;

public record ProjectCreateDTO(
    String name,
    String description,
    LocalDate startDate,
    LocalDate endDate,
    String location,
    Long departmentId
) {}