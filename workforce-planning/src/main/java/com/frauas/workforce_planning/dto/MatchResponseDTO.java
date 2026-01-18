package com.frauas.workforce_planning.dto;

import java.util.List;

public record MatchResponseDTO(
        String message,
        List<MatchedEmployeeDTO> matches
) {}
