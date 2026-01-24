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
    @JsonProperty("experienceYears") Float experienceYears,
    @JsonProperty("skills") java.util.List<String> skills // Change from String to List<String>
) {}
