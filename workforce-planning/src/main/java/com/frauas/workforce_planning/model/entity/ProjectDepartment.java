package com.frauas.workforce_planning.model.entity;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "project_departments",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_project_department", columnNames = {"project_id", "department_id"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectDepartment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- Relations ---
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_head_user_id")
    @JsonIgnoreProperties({"employee", "externalEmployee", "roles", "passwordHash", "password", "hibernateLazyInitializer", "handler"})
    private User departmentHeadUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_planner_user_id")
    @JsonIgnoreProperties({"employee", "externalEmployee", "roles", "passwordHash", "password", "hibernateLazyInitializer", "handler"})
    private User resourcePlannerUser;

    // --- Metadata ---
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
