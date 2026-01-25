package com.frauas.workforce_planning.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ExternalWorkforceResponseDTO(
    @JsonProperty("staffingRequestId") Long staffingRequestId,
    @JsonProperty("externalEmployeeId") String externalEmployeeId,
    @JsonProperty("provider") String provider,
    @JsonProperty("firstName") String firstName,
    @JsonProperty("lastName") String lastName,
    @JsonProperty("email") String email,
    @JsonProperty("skills") String skills,
    @JsonProperty("contractId") String contractId,
    @JsonProperty("evaluationScore") Double evaluationScore,
    @JsonProperty("experienceYears") Integer experienceYears,
    @JsonProperty("wagePerHour") Double wagePerHour,
    @JsonProperty("projectId") Long projectId
) {}