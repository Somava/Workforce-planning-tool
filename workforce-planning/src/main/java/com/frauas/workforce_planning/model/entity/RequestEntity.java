package com.frauas.workforce_planning.model.entity;


import java.time.LocalDate;
import java.time.OffsetDateTime;

import com.frauas.workforce_planning.model.enums.RequestStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "staffing_requests")
public class RequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id; 

    public RequestEntity(String workLocation) {
        this.workLocation = workLocation;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    // 1. Unique Business ID
    @Column(name = "request_id", unique = true)
    private Long requestId;

    public String getPositionName() {
        return positionName;
    }
    public void setPositionName(String positionName) {
        this.positionName = positionName;
    }
    public Long getProjectId() {
        return projectId;
    }
    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }
    public Integer getAvailabilityHours() {
        return headCount;
    }
    public void setAvailabilityHours(Integer headCount) {
        this.headCount = headCount;
    }
    public RequestStatus getStatus() {
        return status;
    }
    public void setStatus(RequestStatus status) {
        this.status = status;
    }
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
    // 2. Title (Position Name)
    @Column(name = "title", nullable = false, length = 200)
    private String positionName;

    // 3. Description
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // 4. Required Skills
    @Column(name = "required_skills", columnDefinition = "TEXT")
    private String requiredSkills;

    // 5. Project ID
    @Column(name = "project_id", nullable = false)
    private Long projectId;    

    // 7. Start Date
    @Column(name = "project_start_date")
    private LocalDate startDate;

    // 8. End Date
    @Column(name = "project_end_date")
    private LocalDate endDate;

    // 9. Availability
    @Column(name = "availability_hours_per_week")
    private Integer headCount;

    // 10. Project Context
    @Column(name = "project_context", columnDefinition = "TEXT")
    private String projectContext;

    // 12. Created By (Employee ID)
    @Column(name = "created_by_employee_id")
    private Integer createdById;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private RequestStatus status = RequestStatus.DRAFT;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private OffsetDateTime createdAt;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "validation_error")
    private String validationError;

    @Column(name = "process_instance_key")
    private Long processInstanceKey;

    @Column(name = "project_location")
    private String projectLocation;

    @Column(name = "work_location")
    private String workLocation;

    @Column(name= "project_name")
    private String projectName;

    // Add these manually if you aren't using Lombok
    public String getValidationError() {
        return validationError;
    }

    public void setValidationError(String validationError) {
        this.validationError = validationError;
    }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(String requiredSkills) { this.requiredSkills = requiredSkills; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getProjectContext() { return projectContext; }
    public void setProjectContext(String projectContext) { this.projectContext = projectContext; }

    public Integer getCreatedById() { return createdById; }
    public void setCreatedById(Integer createdById) { this.createdById = createdById; }

    public Long getProcessInstanceKey() {
        return processInstanceKey;
    }

    public void setProcessInstanceKey(Long processInstanceKey) {
    this.processInstanceKey = processInstanceKey;
    }

    @ManyToOne
    @JoinColumn(name = "department_id", insertable = false, updatable = false)
    private Department department;

    @Column(name = "department_id")
    private Long departmentId;

    // FIX: This must return the ENTITY, not the Long ID
    public Department getDepartment() {
        return department;
    }

    // FIX: This must accept the ENTITY
    public void setDepartment(Department department) {
        this.department = department;
    }

    // Separate getter for just the ID if you need it
    public Long getDepartmentId() {
        return departmentId;
    }

    public String getProjectLocation() {
        return projectLocation;
    }

    public String getWorkLocation() {
        return workLocation;
    }

    public void setWorkLocation(String workLocation) {
        this.workLocation = workLocation;
    }

    public String getProjectName() {
        return projectName;
    }

}