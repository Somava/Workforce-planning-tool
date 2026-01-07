package com.frauas.workforce_planning.model.entity;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.frauas.workforce_planning.model.enums.AssignmentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "assignments")
@Data
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The employee who is assigned
    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id")
    @JsonIgnoreProperties({"assignments", "createdStaffingRequests", "department", "supervisor"})
    private Employee employee;

    // Mapping to the new PK 'request_id' in the staffing_requests table
    @ManyToOne
    @JoinColumn(name = "staffing_request_id", referencedColumnName = "request_id")
    @JsonIgnoreProperties({"project", "department", "createdBy", "assignedUser"})
    private StaffingRequest staffingRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    @JsonIgnoreProperties({"assignments", "staffingRequests"})
    private Project project;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private AssignmentStatus status;

    @Column(name = "period_start")
    private LocalDate periodStart;

    @Column(name = "period_end")
    private LocalDate periodEnd;

    @Column(name = "performance_rating")
    private Short performanceRating;

    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    // Creator of the assignment
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_employee_id")
    @JsonIgnoreProperties({"assignments", "createdStaffingRequests", "department"})
    private Employee createdBy;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }
}