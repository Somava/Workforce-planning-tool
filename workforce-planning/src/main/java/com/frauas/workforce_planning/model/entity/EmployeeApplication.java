package com.frauas.workforce_planning.model.entity;

import com.frauas.workforce_planning.model.enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.OffsetDateTime;

@Entity
@Table(
    name = "employee_applications",
    // ✅ The unique constraint names remain the same, 
    // but they now point to the renamed PK column in the requests table
    uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "staffing_request_id"})
)
@Data
public class EmployeeApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Who applied
    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    // ✅ FIXED: Mapping to the new PK 'request_id' in the staffing_requests table
    @ManyToOne(optional = false)
    @JoinColumn(name = "staffing_request_id", referencedColumnName = "request_id")
    private StaffingRequest staffingRequest;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ApplicationStatus status = ApplicationStatus.APPLIED;

    @Column(name = "applied_at", nullable = false)
    private OffsetDateTime appliedAt = OffsetDateTime.now();

    @Column(name = "decision_at")
    private OffsetDateTime decisionAt;

    // Who made the decision
    @ManyToOne
    @JoinColumn(name = "decision_by_employee_id")
    private Employee decisionBy;

    @Column(columnDefinition = "TEXT")
    private String comment;
}