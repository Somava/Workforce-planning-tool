package com.frauas.workforce_planning.model.entity;

import com.frauas.workforce_planning.model.enums.ProjectStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "projects")
@Data
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "task_description", columnDefinition = "TEXT")
    private String taskDescription;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(length = 200)
    private String location;

    @Column(columnDefinition = "TEXT")
    private String links;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ProjectStatus status = ProjectStatus.PLANNED;

    @Column(nullable = false)
    private Boolean published = false;

    // ✅ AUTOMATED: Uses Hibernate annotations to handle the TIMESTAMPTZ logic from schema
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    // ✅ AUTOMATED: Updates every time the entity is saved
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    // ✅ RELATIONSHIPS
    @OneToMany(mappedBy = "project")
    private Set<StaffingRequest> staffingRequests;

    @OneToMany(mappedBy = "project")
    private Set<Assignment> assignments;

    // Link to external employees if they are assigned to this project
    @OneToMany(mappedBy = "project")
    private Set<ExternalEmployee> externalEmployees;
}