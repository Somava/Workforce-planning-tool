package com.frauas.workforce_planning.model.entity;

import com.frauas.workforce_planning.model.enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(
    name = "employee_applications",
    // ✅ The unique constraint names remain the same, 
    // but they now point to the renamed PK column in the requests table
    uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "staffing_request_id"})
)
@Getter
@Setter
@NoArgsConstructor
public class EmployeeApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // who applied
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "staffing_request_id", nullable = false)
    private StaffingRequest staffingRequest;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ApplicationStatus status = ApplicationStatus.APPLIED;

    @Column(name = "applied_at", nullable = false)
    private OffsetDateTime appliedAt = OffsetDateTime.now();

    @Column(name = "decision_at")
    private OffsetDateTime decisionAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decision_by_employee_id")
    private Employee decisionBy;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @PrePersist
    void prePersist() {
        if (appliedAt == null) appliedAt = OffsetDateTime.now();
        if (status == null) status = ApplicationStatus.APPLIED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmployeeApplication other = (EmployeeApplication) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
