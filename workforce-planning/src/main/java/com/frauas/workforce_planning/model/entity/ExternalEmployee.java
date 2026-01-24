package com.frauas.workforce_planning.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "external_employees")
public class ExternalEmployee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_employee_id", nullable = false)
    private String externalEmployeeId;

    private String provider;
    private String firstName;
    private String lastName;
    private String email;
    private Double wagePerHour;
    private String skills;
    private Float experienceYears;
    private Long staffingRequestId;
    private String status;
}