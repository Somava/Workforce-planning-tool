package com.frauas.workforce_planning.model.entity;

import com.frauas.workforce_planning.model.enums.RequestStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Set;

@Entity
@Table(name = "staffing_requests")
@Data
public class StaffingRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(optional = false)
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne
    @JoinColumn(name = "job_role_id")
    private JobRole jobRole;

    @Column(name = "availability_hours_per_week")
    private Integer availabilityHoursPerWeek;

    @Column(name = "period_start")
    private LocalDate periodStart;

    @Column(name = "period_end")
    private LocalDate periodEnd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private RequestStatus status = RequestStatus.DRAFT;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @ManyToOne
    @JoinColumn(name = "created_by_employee_id")
    private Employee createdBy;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

    @OneToMany(mappedBy = "staffingRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<StaffingRequestSkill> requiredSkills;

    @OneToMany(mappedBy = "staffingRequest")
    private Set<EmployeeApplication> applications;

    @OneToMany(mappedBy = "staffingRequest")
    private Set<Assignment> assignments;
}
