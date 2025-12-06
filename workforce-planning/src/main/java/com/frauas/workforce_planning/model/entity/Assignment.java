package com.frauas.workforce_planning.model.entity;

import com.frauas.workforce_planning.model.enums.AssignmentStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(
    name = "assignments",
    uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "staffing_request_id"})
)
@Data
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // who is assigned
    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @ManyToOne
    @JoinColumn(name = "staffing_request_id")
    private StaffingRequest staffingRequest;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AssignmentStatus status; // WAITING_APPROVAL, REJECTED, ACTIVE, COMPLETED

    @Column(name = "period_start")
    private LocalDate periodStart;

    @Column(name = "period_end")
    private LocalDate periodEnd;

    @Column(name = "performance_rating")
    private Short performanceRating;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "created_by_employee_id")
    private Employee createdBy;
}
