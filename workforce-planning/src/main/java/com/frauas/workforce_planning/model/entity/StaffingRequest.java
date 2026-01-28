package com.frauas.workforce_planning.model.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.frauas.workforce_planning.model.enums.RequestStatus;

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
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "staffing_requests")
@Data
@NoArgsConstructor
// Prevents Jackson from crashing if Hibernate uses lazy-loading proxies
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class StaffingRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long requestId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id")
    // Stop recursion back into the project's list of requests
    @JsonIgnoreProperties({"staffingRequests", "hibernateLazyInitializer", "handler"})
    private Project project;

    @Column(name = "project_name")
    private String projectName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id")
    // Stop recursion into department's employee lists
    @JsonIgnoreProperties({"employees", "staffingRequests", "hibernateLazyInitializer", "handler"})
    private Department department;

    @Column(name = "availability_hours_per_week")
    private Integer availabilityHoursPerWeek;

    @Column(name = "project_start_date")
    private LocalDate projectStartDate;

    @Column(name = "project_end_date")
    private LocalDate projectEndDate;

    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    @Column(name = "wage_per_hour")
    private BigDecimal wagePerHour;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "required_skills", columnDefinition = "jsonb")
    private List<String> requiredSkills;

    @Column(name = "project_context", columnDefinition = "TEXT")
    private String projectContext;

    @Column(name = "project_location")
    private String projectLocation;

    @Column(name = "work_location")
    private String workLocation;

    @Column(name = "process_instance_key")
    private Long processInstanceKey;

    @Column(name = "validation_error")
    private String validationError;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by_employee_id")
    // CRITICAL: "user" and "applications" must be ignored to stop circular references
    @JsonIgnoreProperties({
        "createdStaffingRequests", 
        "department", 
        "supervisor", 
        "user", 
        "applications", 
        "employeeLanguages",
        "hibernateLazyInitializer", 
        "handler"
    })
    private Employee createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_user_id") 
    // CRITICAL: "employee" must be ignored to stop the User -> Employee -> User loop
    @JsonIgnoreProperties({
        "password", 
        "roles", 
        "employee", 
        "hibernateLazyInitializer", 
        "handler"
    })
    private User assignedUser;

    public void setAssignedUser(User assignedUser) {
        this.assignedUser = assignedUser;
    }

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "rejection_type")
    private String rejectionType;

    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;

    @Transient
    private ExternalEmployee externalEmployee;
}