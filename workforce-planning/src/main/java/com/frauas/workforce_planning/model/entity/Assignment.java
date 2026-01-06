package com.frauas.workforce_planning.model.entity;

import com.frauas.workforce_planning.model.enums.AssignmentStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "assignments")
// ✅ Note: The unique constraint is removed from @Table because the new schema 
// uses a conditional index (WHERE staffing_request_id IS NOT NULL)
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

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AssignmentStatus status; // WAITING_APPROVAL, APPROVED, REJECTED, ACTIVE, COMPLETED

    @Column(name = "period_start")
    private LocalDate periodStart;

    @Column(name = "period_end")
    private LocalDate periodEnd;

    // ✅ SMALLINT in Postgres maps to Short or Integer
    @Column(name = "performance_rating")
    private Short performanceRating;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    // The planner or PM who created the assignment
    @ManyToOne
    @JoinColumn(name = "created_by_employee_id")
    private Employee createdBy;
}