package com.frauas.workforce_planning.model.entity;

import com.frauas.workforce_planning.model.enums.AssignmentStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;

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
    private Employee employee;

    // ✅ FIXED: Mapping to the new PK 'request_id' in the staffing_requests table
    @ManyToOne
    @JoinColumn(name = "staffing_request_id", referencedColumnName = "request_id")
    private StaffingRequest staffingRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private AssignmentStatus status;

    @Column(name = "period_start")
    private LocalDate periodStart;

    @Column(name = "period_end")
    private LocalDate periodEnd;

    // ✅ SMALLINT in Postgres maps to Short or Integer
    @Column(name = "performance_rating")
    private Short performanceRating;

    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    // nullable because DB uses ON DELETE SET NULL
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_employee_id")
    private Employee createdBy;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }
}
