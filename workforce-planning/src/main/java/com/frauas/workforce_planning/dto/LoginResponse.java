package com.frauas.workforce_planning.dto;

import java.util.List;

public record LoginResponse(
        String token,
        Long userId,
        Long employeeDbId,
        String employeeHrId,
        String firstName,
        String lastName,
        String selectedRole,
        List<String> roles
) {}
