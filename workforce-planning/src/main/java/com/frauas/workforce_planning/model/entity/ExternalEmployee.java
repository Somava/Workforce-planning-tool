package com.frauas.workforce_planning.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "external_employees")
@Data
@NoArgsConstructor
public class ExternalEmployee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_employee_id", nullable = false, length = 150)
    private String externalEmployeeId;

    @Column(nullable = false, length = 150)
    private String provider;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    // JSONB field for skills (matches new schema)
    @Column(columnDefinition = "jsonb")
    private String skills;

    @ManyToOne
    @JoinColumn(name = "staffing_request_id")
    private StaffingRequest staffingRequest;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
}