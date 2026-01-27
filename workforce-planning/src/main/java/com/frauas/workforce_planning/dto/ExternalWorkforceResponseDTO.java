package com.frauas.workforce_planning.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ExternalWorkforceResponseDTO(
    @JsonProperty("staffingRequestId") Long staffingRequestId,
    @JsonProperty("externalEmployeeId") String externalEmployeeId,
    @JsonProperty("provider") String provider,
    @JsonProperty("firstName") String firstName,
    @JsonProperty("lastName") String lastName,
    @JsonProperty("email") String email,
    @JsonProperty("wagePerHour") Double wagePerHour,
    @JsonProperty("skills") String skills,
    @JsonProperty("location") String location,
    @JsonProperty("contractId") String contractId,
    @JsonProperty("evaluationScore") Double evaluationScore,
    @JsonProperty("projectId") Long projectId,
    @JsonProperty("experienceYears") Integer experienceYears
) {}