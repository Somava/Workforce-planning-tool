package com.frauas.workforce_planning.model.entity;

import com.frauas.workforce_planning.model.enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.OffsetDateTime;

@Entity
@Table(
    name = "employee_applications",
    uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "staffing_request_id"})
)
@Data
public class EmployeeApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // who applied
    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @ManyToOne(optional = false)
    @JoinColumn(name = "staffing_request_id")
    private StaffingRequest staffingRequest;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ApplicationStatus status = ApplicationStatus.APPLIED;

    @Column(name = "applied_at", nullable = false)
    private OffsetDateTime appliedAt;

    @Column(name = "decision_at")
    private OffsetDateTime decisionAt;

    @ManyToOne
    @JoinColumn(name = "decision_by_employee_id")
    private Employee decisionBy;

    @Column(columnDefinition = "TEXT")
    private String comment;
}
