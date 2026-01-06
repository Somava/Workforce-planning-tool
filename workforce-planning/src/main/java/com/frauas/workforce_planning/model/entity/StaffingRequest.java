package com.frauas.workforce_planning.model.entity;

import com.frauas.workforce_planning.model.enums.RequestStatus;
import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "staffing_requests")
@Data
public class StaffingRequest {

    /**
     * NEW PRIMARY KEY
     * Maps to staffing_requests.request_id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long requestId;

    /**
     * Normal column named "id" (NOT primary key)
     */
    @Column(name = "id")
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Keep relation – project table unchanged
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(name = "project_name", length = 200)
    private String projectName;

    @Column(name = "availability_hours_per_week")
    private Integer availabilityHoursPerWeek;

    @Column(name = "project_start_date")
    private LocalDate projectStartDate;

    @Column(name = "project_end_date")
    private LocalDate projectEndDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private RequestStatus status = RequestStatus.DRAFT;

    @ManyToOne
    @JoinColumn(name = "created_by_employee_id")
    private Employee createdBy;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    /**
     * FK to departments table
     * (kept as ID for now to reduce refactoring)
     */
    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "wage_per_hour")
    private BigDecimal wagePerHour;

    /**
     * JSONB column
     */
    @Type(JsonType.class)
    @Column(name = "required_skills", columnDefinition = "jsonb")
    private List<String> requiredSkills;

    @Column(name = "project_context", columnDefinition = "TEXT")
    private String projectContext;

    @Column(name = "project_location", length = 200)
    private String projectLocation;

    @Column(name = "work_location", length = 200)
    private String workLocation;

    @Column(name = "process_instance_key")
    private Long processInstanceKey;

    /**
     * FK to users table
     */
    @Column(name = "assigned_user_id")
    private Long assignedUserId;

    /**
     * These relations still reference staffing_requests(request_id)
     * and will continue to work after the PK rename.
     */
    @OneToMany(mappedBy = "staffingRequest")
    private Set<EmployeeApplication> applications;

    @OneToMany(mappedBy = "staffingRequest")
    private Set<Assignment> assignments;
}
