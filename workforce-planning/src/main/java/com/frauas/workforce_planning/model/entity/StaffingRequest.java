package com.frauas.workforce_planning.model.entity;

import com.frauas.workforce_planning.model.enums.RequestStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Set;

@Entity
@Table(name = "staffing_requests")
@Data
public class StaffingRequest {

    // =========================
    // PRIMARY KEY
    // =========================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long requestId;

    // =========================
    // BASIC INFO
    // =========================
    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "project_name", length = 200)
    private String projectName;

    // =========================
    // RELATIONSHIPS
    // =========================
    @ManyToOne(optional = false)
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne
    @JoinColumn(name = "created_by_employee_id")
    private Employee createdBy;

    @ManyToOne
    @JoinColumn(name = "assigned_user_id")
    private User assignedUser;

    // =========================
    // TIME & AVAILABILITY
    // =========================
    @Column(name = "availability_hours_per_week")
    private Integer availabilityHoursPerWeek;

    @Column(name = "project_start_date")
    private LocalDate projectStartDate;

    @Column(name = "project_end_date")
    private LocalDate projectEndDate;

    // =========================
    // STATUS & WORKFLOW
    // =========================
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private RequestStatus status = RequestStatus.DRAFT;

    @Column(name = "process_instance_key")
    private Long processInstanceKey;

    // =========================
    // SKILLS & EXPERIENCE
    // =========================
    @Column(name = "required_skills", columnDefinition = "jsonb")
    private String requiredSkills; // JSON stored as String

    @Column(name = "experience_years")
    private Integer experienceYears;

    // =========================
    // PAYMENT & LOCATION
    // =========================
    @Column(name = "wage_per_hour", precision = 10, scale = 2)
    private BigDecimal wagePerHour;

    @Column(name = "project_location", length = 200)
    private String projectLocation;

    @Column(name = "work_location", length = 200)
    private String workLocation;

    @Column(name = "project_context", columnDefinition = "TEXT")
    private String projectContext;

    // =========================
    // AUDIT
    // =========================
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    // =========================
    // CHILD RELATIONS
    // =========================
    @OneToMany(mappedBy = "staffingRequest")
    private Set<EmployeeApplication> applications;

    @OneToMany(mappedBy = "staffingRequest")
    private Set<Assignment> assignments;
} 