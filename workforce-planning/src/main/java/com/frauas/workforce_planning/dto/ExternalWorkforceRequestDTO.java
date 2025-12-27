package com.frauas.workforce_planning.dto;

public record ExternalWorkforceRequestDTO(
    String internalRequestId,   // mandatory
    String title,               // mandatory
    String description,         // mandatory
    String requiredSkills,      // optional
    String startDate,           // optional (YYYY-MM-DD)
    String endDate,             // optional (YYYY-MM-DD)
    String projectContext,      // optional
    String performanceLoc       // optional (Onshore/Offshore/Nearshore)
) {}
